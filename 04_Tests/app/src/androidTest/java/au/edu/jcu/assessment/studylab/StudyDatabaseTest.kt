package au.edu.jcu.assessment.studylab

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.assessment.studylab.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.studylab.data.local.StudyDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudyDatabaseTest {
    private lateinit var database: StudyDatabase

    @Before fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, StudyDatabase::class.java).allowMainThreadQueries().build()
    }

    @After fun closeDatabase() = database.close()

    @Test fun completedAttemptIsPersistedAndObservable() = runBlocking {
        database.attemptDao().insert(
            QuizAttemptEntity(
                completedAt = 100L,
                topicName = "Computer Science",
                difficulty = "easy",
                correct = 4,
                total = 5
            )
        )

        val attempts = database.attemptDao().observeAll().first()
        assertEquals(1, attempts.size)
        assertEquals("Computer Science", attempts.single().topicName)
        assertEquals(4, attempts.single().correct)
        assertEquals(5, attempts.single().total)
    }
}
