package au.edu.jcu.assessment.studylab

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        composeRule.onNodeWithText("Practice").performClick()
        composeRule.onNodeWithText("Ready for a recall check?").assertIsDisplayed()
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Larger text").assertIsDisplayed()
        composeRule.onNodeWithText("Progress").performClick()
        composeRule.onNodeWithText("Your progress").assertIsDisplayed()
    }
}
