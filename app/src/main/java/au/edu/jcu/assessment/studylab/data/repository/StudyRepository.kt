package au.edu.jcu.assessment.studylab.data.repository

import au.edu.jcu.assessment.studylab.data.local.QuizAttemptDao
import au.edu.jcu.assessment.studylab.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.studylab.data.local.CachedQuestionDao
import au.edu.jcu.assessment.studylab.data.local.CachedQuestionEntity
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesDao
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesEntity
import au.edu.jcu.assessment.studylab.data.remote.TriviaApi
import au.edu.jcu.assessment.studylab.data.remote.TriviaContentDecoder
import au.edu.jcu.assessment.studylab.data.remote.TriviaQuestionDto
import au.edu.jcu.assessment.studylab.data.remote.TriviaResponseMessages
import au.edu.jcu.assessment.studylab.domain.StudyQuestion
import au.edu.jcu.assessment.studylab.domain.StudyQuestionSet
import au.edu.jcu.assessment.studylab.domain.StudyAttempt
import au.edu.jcu.assessment.studylab.domain.StudyPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

interface StudyRepositoryContract {
    val attempts: Flow<List<StudyAttempt>>
    val preferences: Flow<StudyPreferences?>
    suspend fun fetchQuestions(amount: Int, topicId: Int, difficulty: String): StudyQuestionSet
    suspend fun saveAttempt(attempt: StudyAttempt)
    suspend fun savePreferences(preferences: StudyPreferences)
    suspend fun clearLocalData()
}

class StudyRepository(
    private val api: TriviaApi,
    private val attemptDao: QuizAttemptDao,
    private val preferencesDao: UserPreferencesDao,
    private val cachedQuestionDao: CachedQuestionDao? = null
) : StudyRepositoryContract {
    override val attempts: Flow<List<StudyAttempt>> = attemptDao.observeAll().map { rows -> rows.map(QuizAttemptEntity::toDomain) }
    override val preferences: Flow<StudyPreferences?> = preferencesDao.observe().map { it?.toDomain() }

    override suspend fun fetchQuestions(amount: Int, topicId: Int, difficulty: String): StudyQuestionSet {
        try {
            val response = api.questions(amount, topicId, difficulty)
            if (response.responseCode != 0 || response.results.isEmpty()) {
                val code = if (response.responseCode == 0) 1 else response.responseCode
                throw StudyRepositoryException(TriviaResponseMessages.forCode(code))
            }
            val questions = response.results.map(::toDomain)
            try {
                cacheQuestions(topicId, difficulty, questions)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // A cache write failure must not discard a valid response.
            }
            return StudyQuestionSet(questions, fromCache = false)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (http: HttpException) {
            return safeCachedQuestions(topicId, difficulty, amount)?.let { StudyQuestionSet(it, fromCache = true) }
                ?: throw StudyRepositoryException(TriviaResponseMessages.forHttpStatus(http.code()), http)
        } catch (io: IOException) {
            return safeCachedQuestions(topicId, difficulty, amount)?.let { StudyQuestionSet(it, fromCache = true) }
                ?: throw StudyRepositoryException(TriviaResponseMessages.CONNECTION_FAILURE, io)
        } catch (known: StudyRepositoryException) {
            return safeCachedQuestions(topicId, difficulty, amount)?.let { StudyQuestionSet(it, fromCache = true) } ?: throw known
        } catch (error: Exception) {
            return safeCachedQuestions(topicId, difficulty, amount)?.let { StudyQuestionSet(it, fromCache = true) }
                ?: throw StudyRepositoryException(TriviaResponseMessages.UNREADABLE_RESPONSE, error)
        }
    }

    override suspend fun saveAttempt(attempt: StudyAttempt) = attemptDao.insert(attempt.toEntity())
    override suspend fun savePreferences(preferences: StudyPreferences) = preferencesDao.save(preferences.toEntity())

    override suspend fun clearLocalData() {
        attemptDao.deleteAll()
        preferencesDao.deleteAll()
        cachedQuestionDao?.deleteAll()
    }

    private suspend fun cacheQuestions(topicId: Int, difficulty: String, questions: List<StudyQuestion>) {
        val dao = cachedQuestionDao ?: return
        dao.replaceSet(questions.mapIndexed { index, question ->
            CachedQuestionEntity(
                topicId = topicId,
                difficulty = difficulty,
                position = index,
                category = question.category,
                prompt = question.prompt,
                answersJson = gson.toJson(question.answers),
                correctAnswer = question.correctAnswer
            )
        })
    }

    private suspend fun cachedQuestions(topicId: Int, difficulty: String, amount: Int): List<StudyQuestion>? {
        val rows = cachedQuestionDao?.getSet(topicId, difficulty)?.take(amount).orEmpty()
        if (rows.isEmpty()) return null
        val answerListType = object : TypeToken<List<String>>() {}.type
        return runCatching {
            rows.map { row ->
                StudyQuestion(
                    category = row.category,
                    prompt = row.prompt,
                    difficulty = difficulty,
                    answers = gson.fromJson<List<String>>(row.answersJson, answerListType),
                    correctAnswer = row.correctAnswer
                )
            }
        }.getOrNull()?.takeIf { it.isNotEmpty() }
    }

    private suspend fun safeCachedQuestions(topicId: Int, difficulty: String, amount: Int): List<StudyQuestion>? =
        try {
            cachedQuestions(topicId, difficulty, amount)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            null
        }

    private fun toDomain(dto: TriviaQuestionDto): StudyQuestion {
        val correct = TriviaContentDecoder.decode(dto.correctAnswer)
        return StudyQuestion(
            category = TriviaContentDecoder.decode(dto.category),
            prompt = TriviaContentDecoder.decode(dto.question),
            difficulty = dto.difficulty,
            answers = (dto.incorrectAnswers.map(TriviaContentDecoder::decode) + correct).shuffled(),
            correctAnswer = correct
        )
    }
}

private val gson = Gson()

class StudyRepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun QuizAttemptEntity.toDomain() = StudyAttempt(completedAt, topicId, difficulty, correct, total)
private fun UserPreferencesEntity.toDomain() = StudyPreferences(topicId, difficulty, largeText, studyPlace)
private fun StudyAttempt.toEntity() = QuizAttemptEntity(
    completedAt = completedAt,
    topicName = "",
    difficulty = difficulty,
    correct = correct,
    total = total,
    topicId = topicId
)
private fun StudyPreferences.toEntity() = UserPreferencesEntity(
    topicId = topicId,
    difficulty = difficulty,
    largeText = largeText,
    studyPlace = studyPlace
)
