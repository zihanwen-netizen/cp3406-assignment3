package au.edu.jcu.assessment.studylab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyLabNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeShowsStudyPlanAndPracticeEntry() {
        composeRule.onNodeWithText("StudyLab").assertIsDisplayed()
        composeRule.onNodeWithText("Plan a place to study").assertIsDisplayed()
        composeRule.onNodeWithText("Start practice").assertIsDisplayed()
    }

    @Test
    fun bottomNavigationOpensPracticeAndSettings() {
        assertTabSelected("Home")
        composeRule.onNodeWithText("Practice").performClick()
        assertTabSelected("Practice")
        composeRule.onNodeWithText("Ready for a recall check?").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()
        assertTabSelected("Settings")
        composeRule.onNodeWithText("Larger text").assertIsDisplayed()
        composeRule.onNodeWithText("Progress").performClick()
        assertTabSelected("Progress")
        composeRule.onNodeWithText("Your progress").assertIsDisplayed()
    }

    private fun assertTabSelected(label: String) {
        val selected = composeRule.onAllNodesWithText(label).fetchSemanticsNodes().any { node ->
            SemanticsMatcher.expectValue(SemanticsProperties.Selected, true).matches(node)
        }
        assertTrue("$label navigation item should be selected", selected)
    }
}
