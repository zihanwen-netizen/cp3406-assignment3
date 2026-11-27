package au.edu.jcu.assessment.studylab.di

import android.content.Context
import au.edu.jcu.assessment.studylab.data.local.StudyDatabase
import au.edu.jcu.assessment.studylab.data.remote.TriviaApi
import au.edu.jcu.assessment.studylab.data.repository.StudyRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Small manual dependency container, reusing the simple approach from my A1 project. */
class AppContainer(context: Context) {
    private val database = StudyDatabase.get(context)
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://opentdb.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val repository = StudyRepository(
        api = retrofit.create(TriviaApi::class.java),
        attemptDao = database.attemptDao(),
        preferencesDao = database.preferencesDao()
    )
}
