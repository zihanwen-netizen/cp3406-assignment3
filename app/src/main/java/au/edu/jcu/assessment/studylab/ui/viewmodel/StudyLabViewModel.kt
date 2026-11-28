package au.edu.jcu.assessment.studylab.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import au.edu.jcu.assessment.studylab.data.repository.StudyRepositoryContract
import au.edu.jcu.assessment.studylab.data.repository.StudyRepositoryException
import au.edu.jcu.assessment.studylab.domain.QuizRules
import au.edu.jcu.assessment.studylab.domain.StudyAttempt
import au.edu.jcu.assessment.studylab.domain.StudyPreferences
import au.edu.jcu.assessment.studylab.domain.SessionSummary
import au.edu.jcu.assessment.studylab.domain.StudyQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

data class StudyLabUiState(
    val preferences: StudyPreferences = StudyPreferences(),
    val attempts: List<StudyAttempt> = emptyList(),
    val questions: List<StudyQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: String? = null,
    val answerChecked: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val correctCount: Int = 0,
    val loading: Boolean = false,
    val usingCachedQuestions: Boolean = false,
    val error: String? = null,
    val sessionSummary: SessionSummary? = null
) {
    val currentQuestion: StudyQuestion? get() = questions.getOrNull(currentIndex)
}

class StudyLabViewModel(private val repository: StudyRepositoryContract) : ViewModel() {
    private val _uiState = MutableStateFlow(StudyLabUiState())
    val uiState: StateFlow<StudyLabUiState> = _uiState.asStateFlow()
    private val sessionResults = mutableListOf<Boolean>()
    private var activeTopicId: Int = _uiState.value.preferences.topicId
    private var activeDifficulty: String = _uiState.value.preferences.difficulty

    init {
        viewModelScope.launch {
            repository.preferences.collectLatest { stored ->
                if (stored != null) _uiState.value = _uiState.value.copy(preferences = stored)
            }
        }
        viewModelScope.launch {
            repository.attempts.collectLatest { attempts ->
                _uiState.value = _uiState.value.copy(attempts = attempts)
            }
        }
    }

    fun startSession() {
        if (_uiState.value.loading) return
        sessionResults.clear()
        activeTopicId = _uiState.value.preferences.topicId
        activeDifficulty = _uiState.value.preferences.difficulty
        _uiState.value = _uiState.value.copy(
            loading = true,
            error = null,
            usingCachedQuestions = false,
            questions = emptyList(),
            currentIndex = 0,
            selectedAnswer = null,
            answerChecked = false,
            lastAnswerCorrect = null,
            correctCount = 0,
            sessionSummary = null
        )
        viewModelScope.launch {
            try {
                val questionSet = repository.fetchQuestions(amount = 5, topicId = activeTopicId, difficulty = activeDifficulty)
                _uiState.value = _uiState.value.copy(
                    questions = questionSet.questions,
                    usingCachedQuestions = questionSet.fromCache,
                    loading = false
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: StudyRepositoryException) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = error.message ?: "Questions could not be loaded. Please try again."
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(loading = false, error = "Questions could not be loaded. Please try again.")
            }
        }
    }

    fun startOrResumeSession() {
        val state = _uiState.value
        if (state.questions.isEmpty() || state.sessionSummary != null) startSession()
    }

    fun selectAnswer(answer: String) {
        if (!_uiState.value.answerChecked) {
            _uiState.value = _uiState.value.copy(selectedAnswer = answer)
        }
    }

    fun checkAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val selected = state.selectedAnswer ?: return
        val correct = QuizRules.isCorrect(selected, question.correctAnswer)
        sessionResults += correct
        _uiState.value = state.copy(
            answerChecked = true,
            lastAnswerCorrect = correct,
            correctCount = state.correctCount + if (correct) 1 else 0
        )
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (!state.answerChecked) return
        if (state.currentIndex < state.questions.lastIndex) {
            _uiState.value = state.copy(
                currentIndex = state.currentIndex + 1,
                selectedAnswer = null,
                answerChecked = false,
                lastAnswerCorrect = null
            )
        } else {
            val summary = QuizRules.summary(sessionResults)
            _uiState.value = state.copy(sessionSummary = summary)
            viewModelScope.launch {
                repository.saveAttempt(
                    StudyAttempt(
                        completedAt = System.currentTimeMillis(),
                        topicId = activeTopicId,
                        difficulty = state.questions.firstOrNull()?.difficulty ?: activeDifficulty,
                        correct = summary.correct,
                        total = summary.total
                    )
                )
            }
        }
    }

    fun clearSession() {
        _uiState.value = _uiState.value.copy(
            sessionSummary = null,
            questions = emptyList(),
            error = null,
            usingCachedQuestions = false
        )
    }

    fun setTopic(topicId: Int) = updatePreferences { it.copy(topicId = topicId) }
    fun setDifficulty(difficulty: String) = updatePreferences { it.copy(difficulty = difficulty) }
    fun setLargeText(enabled: Boolean) = updatePreferences { it.copy(largeText = enabled) }
    fun setStudyPlace(place: String) = updatePreferences { it.copy(studyPlace = place) }

    fun clearLocalData() {
        viewModelScope.launch {
            repository.clearLocalData()
            val defaults = StudyPreferences()
            repository.savePreferences(defaults)
            sessionResults.clear()
            _uiState.value = StudyLabUiState(preferences = defaults)
        }
    }

    private fun updatePreferences(transform: (StudyPreferences) -> StudyPreferences) {
        val updated = transform(_uiState.value.preferences)
        _uiState.value = _uiState.value.copy(preferences = updated)
        viewModelScope.launch { repository.savePreferences(updated) }
    }
}

class StudyLabViewModelFactory(private val repository: StudyRepositoryContract) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyLabViewModel::class.java)) {
            return StudyLabViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
