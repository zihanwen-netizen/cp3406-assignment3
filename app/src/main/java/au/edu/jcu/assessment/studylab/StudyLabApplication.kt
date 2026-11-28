package au.edu.jcu.assessment.studylab

import android.app.Application
import au.edu.jcu.assessment.studylab.di.AppContainer

class StudyLabApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
