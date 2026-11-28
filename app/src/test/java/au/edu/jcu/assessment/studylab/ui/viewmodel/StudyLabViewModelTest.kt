package au.edu.jcu.assessment.studylab.ui.viewmodel

import au.edu.jcu.assessment.studylab.data.repository.StudyRepositoryContract
import au.edu.jcu.assessment.studylab.data.repository.StudyRepositoryException
import au.edu.jcu.assessment.studylab.domain.SessionSummary
import au.edu.jcu.assessment.studylab.domain.StudyAttempt
import au.edu.jcu.assessment.studylab.domain.StudyPreferences
import au.edu.jcu.assessment.studylab.domain.StudyQuestion
import au.edu.jcu.assessment.studylab.domain.StudyQuestionSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudyLabViewModelTest {
    @Test
    fun completesPracticeAndPersistsTheSessionSummary() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeStudyRepository()
            val viewModel = StudyLabViewModel(repository)
            advanceUntilIdle()

            viewModel.startSession()
            advanceUntilIdle()
            assertEquals("Which language runs on the JVM?", viewModel.uiState.value.currentQuestion?.prompt)
            viewModel.selectAnswer("Kotlin")
            viewModel.checkAnswer()
            assertTrue(viewModel.uiState.value.lastAnswerCorrect == true)
            viewModel.nextQuestion()
            advanceUntilIdle()

            assertEquals(SessionSummary(correct = 1, total = 1), viewModel.uiState.value.sessionSummary)
            assertEquals(1, repository.savedAttempts.value.size)
            assertEquals(1, repository.savedAttempts.value.single().correct)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun presentsUnavailableQuestionSetAsActionableError() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeStudyRepository(
                fetchFailure = StudyRepositoryException("No questions are available for this topic and difficulty. Try another setting.")
            )
            val viewModel = StudyLabViewModel(repository)
            advanceUntilIdle()

            viewModel.startSession()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.loading)
            assertEquals(
                "No questions are available for this topic and difficulty. Try another setting.",
                viewModel.uiState.value.error
            )
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun openingPracticeAgainKeepsTheLoadedQuestionAndAnswerState() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeStudyRepository()
            val viewModel = StudyLabViewModel(repository)
            advanceUntilIdle()
            viewModel.startSession()
            advanceUntilIdle()
            viewModel.selectAnswer("Kotlin")
            viewModel.checkAnswer()

            viewModel.startOrResumeSession()
            advanceUntilIdle()

            assertEquals(1, repository.fetchCalls)
            assertTrue(viewModel.uiState.value.answerChecked)
            assertEquals("Kotlin", viewModel.uiState.value.selectedAnswer)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun sessionKeepsItsStartingTopicWhenPreferencesChangeMidPractice() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeStudyRepository()
            val viewModel = StudyLabViewModel(repository)
            advanceUntilIdle()
            viewModel.startSession()
            advanceUntilIdle()

            viewModel.setTopic(17)
            advanceUntilIdle()
            viewModel.selectAnswer("Kotlin")
            viewModel.checkAnswer()
            viewModel.nextQuestion()
            advanceUntilIdle()

            assertEquals(18, repository.savedAttempts.value.single().topicId)
            assertEquals(18, repository.requestedTopicIds.single())
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun clearingLocalDataRemovesHistoryAndRestoresDefaultPreferences() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeStudyRepository(
                initialAttempts = listOf(StudyAttempt(100L, 18, "easy", 3, 5)),
                initialPreferences = StudyPreferences(studyPlace = "JCU Library", largeText = true)
            )
            val viewModel = StudyLabViewModel(repository)
            advanceUntilIdle()

            viewModel.clearLocalData()
            advanceUntilIdle()

            assertTrue(repository.savedAttempts.value.isEmpty())
            assertEquals(StudyPreferences(), repository.preferenceState.value)
            assertEquals(StudyPreferences(), viewModel.uiState.value.preferences)
            assertTrue(viewModel.uiState.value.attempts.isEmpty())
        } finally {
            Dispatchers.resetMain()
        }
    }
}

private class FakeStudyRepository(
    questions: List<StudyQuestion> = listOf(
        StudyQuestion(
            category = "Science: Computers",
            prompt = "Which language runs on the JVM?",
            difficulty = "easy",
            answers = listOf("Kotlin", "HTML", "CSS", "SQL"),
            correctAnswer = "Kotlin"
        )
    ),
    private val fetchFailure: Exception? = null,
    initialAttempts: List<StudyAttempt> = emptyList(),
    initialPreferences: StudyPreferences = StudyPreferences()
) : StudyRepositoryContract {
    private val questionSet = questions
    val savedAttempts = MutableStateFlow(initialAttempts)
    val preferenceState = MutableStateFlow<StudyPreferences?>(initialPreferences)
    var fetchCalls: Int = 0
        private set
    val requestedTopicIds = mutableListOf<Int>()

    override val attempts: Flow<List<StudyAttempt>> = savedAttempts
    override val preferences: Flow<StudyPreferences?> = preferenceState

    override suspend fun fetchQuestions(amount: Int, topicId: Int, difficulty: String): StudyQuestionSet {
        fetchCalls += 1
        requestedTopicIds += topicId
        fetchFailure?.let { throw it }
        return StudyQuestionSet(questionSet, fromCache = false)
    }

    override suspend fun saveAttempt(attempt: StudyAttempt) {
        savedAttempts.value = savedAttempts.value + attempt
    }

    override suspend fun savePreferences(preferences: StudyPreferences) {
        preferenceState.value = preferences
    }

    override suspend fun clearLocalData() {
        savedAttempts.value = emptyList()
        preferenceState.value = null
    }
}
