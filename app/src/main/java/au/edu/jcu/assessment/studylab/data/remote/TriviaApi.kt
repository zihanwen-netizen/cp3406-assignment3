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
        @Query("encode") encode: String = "url3986"
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
