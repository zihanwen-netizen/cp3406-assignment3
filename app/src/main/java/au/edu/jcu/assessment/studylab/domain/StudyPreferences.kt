package au.edu.jcu.assessment.studylab.domain

data class StudyPreferences(
    val topicId: Int = 18,
    val difficulty: String = "easy",
    val largeText: Boolean = false,
    val studyPlace: String = ""
)

data class StudyAttempt(
    val completedAt: Long,
    val topicId: Int,
    val difficulty: String,
    val correct: Int,
    val total: Int
)
