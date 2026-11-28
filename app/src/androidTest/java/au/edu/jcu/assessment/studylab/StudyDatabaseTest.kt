package au.edu.jcu.assessment.studylab

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import au.edu.jcu.assessment.studylab.data.local.QuizAttemptEntity
import au.edu.jcu.assessment.studylab.data.local.CachedQuestionEntity
import au.edu.jcu.assessment.studylab.data.local.StudyDatabase
import au.edu.jcu.assessment.studylab.data.local.UserPreferencesEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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

    @Test fun deleteAllRemovesAttemptsAndSavedPreferences() = runBlocking {
        database.attemptDao().insert(
            QuizAttemptEntity(
                completedAt = 200L,
                topicName = "Computer Science",
                difficulty = "medium",
                correct = 2,
                total = 5
            )
        )
        database.preferencesDao().save(UserPreferencesEntity(studyPlace = "JCU Library"))

        database.attemptDao().deleteAll()
        database.preferencesDao().deleteAll()

        assertTrue(database.attemptDao().observeAll().first().isEmpty())
        assertEquals(null, database.preferencesDao().observe().first())
    }

    @Test fun cachedQuestionSetIsScopedByTopicAndDifficulty() = runBlocking {
        database.cachedQuestionDao().replaceSet(
            listOf(
                CachedQuestionEntity(
                    topicId = 18,
                    difficulty = "easy",
                    position = 0,
                    category = "Science: Computers",
                    prompt = "Cached question",
                    answersJson = "[\"a\",\"b\"]",
                    correctAnswer = "a"
                )
            )
        )

        assertEquals("Cached question", database.cachedQuestionDao().getSet(18, "easy").single().prompt)
        assertTrue(database.cachedQuestionDao().getSet(17, "easy").isEmpty())
    }
}
