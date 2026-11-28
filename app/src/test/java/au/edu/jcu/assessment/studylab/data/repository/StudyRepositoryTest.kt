package au.edu.jcu.assessment.studylab.data.repository

import au.edu.jcu.assessment.studylab.data.local.QuizAttemptDao
import au.edu.jcu.assessment.studylab.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.studylab.data.local.CachedQuestionDao
import au.edu.jcu.assessment.studylab.data.local.CachedQuestionEntity
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesDao
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesEntity
import au.edu.jcu.assessment.studylab.data.remote.TriviaApi
import au.edu.jcu.assessment.studylab.data.remote.TriviaQuestionDto
import au.edu.jcu.assessment.studylab.data.remote.TriviaResponseDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class StudyRepositoryTest {
    @Test
    fun usesTheMatchingCachedSetWhenNetworkIsUnavailable() = runTest {
        val cache = FakeCachedQuestionDao(
            listOf(
                CachedQuestionEntity(
                    topicId = 18,
                    difficulty = "easy",
                    position = 0,
                    category = "Science: Computers",
                    prompt = "Cached question",
                    answersJson = "[\"right\",\"wrong\"]",
                    correctAnswer = "right"
                )
            )
        )
        val repository = StudyRepository(
            api = LambdaTriviaApi { throw IOException("offline") },
            attemptDao = EmptyQuizAttemptDao(),
            preferencesDao = EmptyPreferencesDao(),
            cachedQuestionDao = cache
        )

        val result = repository.fetchQuestions(amount = 5, topicId = 18, difficulty = "easy")

        assertEquals("Cached question", result.questions.single().prompt)
        assertEquals("right", result.questions.single().correctAnswer)
        assertTrue(result.fromCache)
    }

    @Test
    fun mapsNoQuestionsResponseCodeToAUsefulNextStep() = runTest {
        val error = fetchFailure { TriviaResponseDto(responseCode = 1, results = emptyList()) }

        assertEquals("No questions are available for this topic and difficulty. Try another setting.", error.message)
    }

    @Test
    fun mapsApiRateLimitResponseCodeToAUsefulWaitMessage() = runTest {
        val error = fetchFailure { TriviaResponseDto(responseCode = 5, results = emptyList()) }

        assertEquals("The question service is busy. Wait a few seconds, then try again.", error.message)
    }

    @Test
    fun mapsRateLimitToAnActionableMessageWithoutExposingHttpDetails() = runTest {
        val response = Response.error<TriviaResponseDto>(429, "raw server response".toResponseBody("text/plain".toMediaType()))
        val error = fetchFailure { throw HttpException(response) }

        assertEquals("The question service is busy. Wait a few seconds, then try again.", error.message)
        assertTrue(error.cause is HttpException)
    }

    @Test
    fun mapsConnectionFailureWithoutExposingHostOrStackDetails() = runTest {
        val error = fetchFailure { throw IOException("Unable to resolve host opentdb.example") }

        assertEquals("Questions could not be loaded. Check your connection and try again.", error.message)
        assertTrue(error.cause is IOException)
    }

    @Test
    fun mapsMalformedBase64ToAReadableContentError() = runTest {
        val response = TriviaResponseDto(
            responseCode = 0,
            results = listOf(
                TriviaQuestionDto(
                    category = "not base64!",
                    difficulty = "easy",
                    question = "question",
                    correctAnswer = "answer",
                    incorrectAnswers = listOf("wrong")
                )
            )
        )
        val error = fetchFailure { response }

        assertEquals("The question service returned content that could not be read. Please try again.", error.message)
    }
}

private suspend fun fetchFailure(apiCall: suspend () -> TriviaResponseDto): StudyRepositoryException {
    val repository = StudyRepository(
        api = LambdaTriviaApi(apiCall),
        attemptDao = EmptyQuizAttemptDao(),
        preferencesDao = EmptyPreferencesDao()
    )
    return try {
        repository.fetchQuestions(amount = 5, topicId = 18, difficulty = "easy")
        error("Expected the repository to reject this response")
    } catch (failure: StudyRepositoryException) {
        failure
    }
}

private class LambdaTriviaApi(
    private val apiCall: suspend () -> TriviaResponseDto
) : TriviaApi {
    override suspend fun questions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String,
        encode: String
    ): TriviaResponseDto = apiCall()
}

private class EmptyQuizAttemptDao : QuizAttemptDao {
    override suspend fun insert(attempt: QuizAttemptEntity) = Unit
    override fun observeAll(): Flow<List<QuizAttemptEntity>> = flowOf(emptyList())
    override suspend fun deleteAll() = Unit
}

private class EmptyPreferencesDao : UserPreferencesDao {
    override fun observe(): Flow<UserPreferencesEntity?> = flowOf(null)
    override suspend fun save(preferences: UserPreferencesEntity) = Unit
    override suspend fun deleteAll() = Unit
}

private class FakeCachedQuestionDao(
    private var rows: List<CachedQuestionEntity> = emptyList()
) : CachedQuestionDao {
    override suspend fun replaceSet(questions: List<CachedQuestionEntity>) { rows = questions }
    override suspend fun getSet(topicId: Int, difficulty: String) = rows.filter {
        it.topicId == topicId && it.difficulty == difficulty
    }
    override suspend fun deleteAll() { rows = emptyList() }
}
