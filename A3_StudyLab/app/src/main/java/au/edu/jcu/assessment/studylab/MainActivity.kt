package au.edu.jcu.assessment.studylab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.studylab.ui.StudyLabApp
import au.edu.jcu.assessment.studylab.ui.theme.StudyLabTheme
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabViewModel
import au.edu.jcu.assessment.studylab.ui.viewmodel.StudyLabViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as StudyLabApplication
        setContent {
            StudyLabTheme {
                val studyViewModel: StudyLabViewModel = viewModel(
                    factory = StudyLabViewModelFactory(app.container.repository)
                )
                StudyLabApp(studyViewModel)
            }
        }
    }
}
