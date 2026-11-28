package au.edu.jcu.assessment.studylab.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import au.edu.jcu.assessment.studylab.ui.theme.StudyLabTheme
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabUiState

@Preview(showBackground = true, name = "Home")
@Composable
private fun HomeScreenPreview() {
    StudyLabTheme {
        HomeScreen(StudyLabUiState(), onStartPractice = {}, onSaveStudyPlace = {})
    }
}

@Preview(showBackground = true, name = "Practice")
@Composable
private fun ActivityScreenPreview() {
    StudyLabTheme {
        ActivityScreen(
            state = StudyLabUiState(),
            onStart = {},
            onSelect = {},
            onCheck = {},
            onNext = {},
            onTryAgain = {},
            onDone = {}
        )
    }
}

@Preview(showBackground = true, name = "Progress")
@Composable
private fun StatisticsScreenPreview() {
    StudyLabTheme {
        StatisticsScreen(StudyLabUiState())
    }
}

@Preview(showBackground = true, name = "Settings")
@Composable
private fun SettingsScreenPreview() {
    StudyLabTheme {
        SettingsScreen(
            state = StudyLabUiState(),
            onTopic = {},
            onDifficulty = {},
            onLargeText = {},
            onClearData = {}
        )
    }
}
