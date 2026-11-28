package au.edu.jcu.assessment.studylab.domain

data class StudyQuestion(
    val category: String,
    val prompt: String,
    val difficulty: String,
    val answers: List<String>,
    val correctAnswer: String
)

data class StudyQuestionSet(val questions: List<StudyQuestion>, val fromCache: Boolean)

data class Topic(val id: Int, val name: String) {
    companion object {
        val available = listOf(
            Topic(18, "Computer Science"),
            Topic(17, "Science and Nature"),
            Topic(9, "General Knowledge")
        )

        fun nameFor(id: Int): String = available.firstOrNull { it.id == id }?.name ?: "Computer Science"
    }
}

data class SessionSummary(val correct: Int, val total: Int) {
    val accuracy: Int get() = QuizRules.accuracyPercent(correct, total)
}

/** Pure scoring rules keep quiz decisions independent of Android UI and storage. */
object QuizRules {
    fun isCorrect(selected: String, correct: String): Boolean = selected == correct

    fun accuracyPercent(correct: Int, total: Int): Int {
        if (total <= 0) return 0
        return ((correct.coerceIn(0, total) * 100.0) / total).toInt()
    }

    fun summary(answers: List<Boolean>): SessionSummary =
        SessionSummary(correct = answers.count { it }, total = answers.size)
}
