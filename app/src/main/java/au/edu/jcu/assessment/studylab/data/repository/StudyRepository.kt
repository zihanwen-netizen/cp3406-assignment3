package au.edu.jcu.assessment.studylab.data.repository

import android.text.Html
import android.text.Spanned
import android.os.Build
import au.edu.jcu.assessment.studylab.data.local.QuizAttemptDao
import au.edu.jcu.assessment.studylab.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesDao
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesEntity
import au.edu.jcu.assessment.studylab.data.remote.TriviaApi
import au.edu.jcu.assessment.studylab.data.remote.TriviaQuestionDto
import au.edu.jcu.assessment.studylab.domain.StudyQuestion
import kotlinx.coroutines.flow.Flow
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class StudyRepository(
    private val api: TriviaApi,
    private val attemptDao: QuizAttemptDao,
    private val preferencesDao: UserPreferencesDao
) {
    val attempts: Flow<List<QuizAttemptEntity>> = attemptDao.observeAll()
    val preferences: Flow<UserPreferencesEntity?> = preferencesDao.observe()

    suspend fun fetchQuestions(amount: Int, topicId: Int, difficulty: String): List<StudyQuestion> {
        val response = api.questions(amount, topicId, difficulty)
        if (response.responseCode != 0 || response.results.isEmpty()) {
            throw IllegalStateException(
                when (response.responseCode) {
                    1 -> "No questions are available for this topic and difficulty. Try another setting."
                    5 -> "The question service is busy. Wait a few seconds, then try again."
                    else -> "Questions could not be loaded. Check your connection and try again."
                }
            )
        }
        return response.results.map(::toDomain)
    }

    suspend fun saveAttempt(attempt: QuizAttemptEntity) = attemptDao.insert(attempt)
    suspend fun savePreferences(preferences: UserPreferencesEntity) = preferencesDao.save(preferences)

    private fun toDomain(dto: TriviaQuestionDto): StudyQuestion {
        val correct = decode(dto.correctAnswer)
        return StudyQuestion(
            category = decode(dto.category),
            prompt = decode(dto.question),
            difficulty = dto.difficulty,
            answers = (dto.incorrectAnswers.map(::decode) + correct).shuffled(),
            correctAnswer = correct
        )
    }

    private fun decode(value: String): String {
        val urlDecoded = URLDecoder.decode(value, StandardCharsets.UTF_8.name())
        return if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml(urlDecoded, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(urlDecoded).toString()
        }
    }
}
