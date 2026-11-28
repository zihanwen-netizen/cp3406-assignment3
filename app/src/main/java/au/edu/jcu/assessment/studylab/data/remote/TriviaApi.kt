package au.edu.jcu.assessment.studylab.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApi {
    @GET("api.php")
    suspend fun questions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String = "multiple",
        @Query("encode") encode: String = "base64"
    ): TriviaResponseDto
}

data class TriviaResponseDto(
    @SerializedName("response_code") val responseCode: Int,
    val results: List<TriviaQuestionDto>
)

data class TriviaQuestionDto(
    val category: String,
    val difficulty: String,
    val question: String,
    @SerializedName("correct_answer") val correctAnswer: String,
    @SerializedName("incorrect_answers") val incorrectAnswers: List<String>
)

object TriviaResponseMessages {
    const val CONNECTION_FAILURE = "Questions could not be loaded. Check your connection and try again."
    const val SERVICE_FAILURE = "The question service could not respond. Please try again later."
    const val UNREADABLE_RESPONSE = "The question service returned content that could not be read. Please try again."
    const val UNKNOWN_FAILURE = "Questions could not be loaded. Please try again."
    const val RATE_LIMITED = "The question service is busy. Wait a few seconds, then try again."

    fun forCode(code: Int): String = when (code) {
        1 -> "No questions are available for this topic and difficulty. Try another setting."
        5 -> RATE_LIMITED
        else -> SERVICE_FAILURE
    }

    fun forHttpStatus(statusCode: Int): String = when (statusCode) {
        429 -> RATE_LIMITED
        else -> SERVICE_FAILURE
    }
}
