package au.edu.jcu.assessment.studylab

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.assessment.studylab.data.repository.StudyRepositoryContract
import au.edu.jcu.assessment.studylab.domain.StudyAttempt
import au.edu.jcu.assessment.studylab.domain.StudyPreferences
import au.edu.jcu.assessment.studylab.domain.StudyQuestion
import au.edu.jcu.assessment.studylab.domain.StudyQuestionSet
import au.edu.jcu.assessment.studylab.ui.screens.ActivityScreen
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyLabPracticeFlowTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun learnerCanSelectCheckAndFinishAQuestionSet() {
        val question = StudyQuestion(
            category = "Science: Computers",
            prompt = "Which language runs on the JVM?",
            difficulty = "easy",
            answers = listOf("Kotlin", "HTML", "CSS", "SQL"),
            correctAnswer = "Kotlin"
        )
        val repository = PracticeFlowRepository(question)
        val viewModel = StudyLabViewModel(repository)

        composeRule.setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            ActivityScreen(
                state = state,
                onStart = viewModel::startSession,
                onSelect = viewModel::selectAnswer,
                onCheck = viewModel::checkAnswer,
                onNext = viewModel::nextQuestion,
                onTryAgain = viewModel::startSession,
                onDone = viewModel::clearSession
            )
        }

        composeRule.onNodeWithText("Load questions").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Which language runs on the JVM?").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Which language runs on the JVM?").assertIsDisplayed()
        composeRule.onNodeWithText("Kotlin").performClick()
        composeRule.onNodeWithText("Check answer").performClick()
        composeRule.onNodeWithText("Correct. Take a moment to explain why this answer fits.").assertIsDisplayed()
        composeRule.onNodeWithText("Finish session").performClick()
        composeRule.onNodeWithText("Session complete").assertIsDisplayed()
        composeRule.onNodeWithText("1 of 1 correct").assertIsDisplayed()
    }
}

private class PracticeFlowRepository(private val question: StudyQuestion) : StudyRepositoryContract {
    override val attempts: Flow<List<StudyAttempt>> = flowOf(emptyList())
    override val preferences: Flow<StudyPreferences?> = flowOf(StudyPreferences())
    override suspend fun fetchQuestions(amount: Int, topicId: Int, difficulty: String) = StudyQuestionSet(listOf(question), fromCache = false)
    override suspend fun saveAttempt(attempt: StudyAttempt) = Unit
    override suspend fun savePreferences(preferences: StudyPreferences) = Unit
    override suspend fun clearLocalData() = Unit
}
