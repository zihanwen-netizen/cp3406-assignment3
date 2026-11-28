package au.edu.jcu.assessment.studylab.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val completedAt: Long,
    val topicName: String,
    val difficulty: String,
    val correct: Int,
    val total: Int,
    val topicId: Int = 18
)

@Entity(tableName = "cached_questions", primaryKeys = ["topicId", "difficulty", "position"])
data class CachedQuestionEntity(
    val topicId: Int,
    val difficulty: String,
    val position: Int,
    val category: String,
    val prompt: String,
    val answersJson: String,
    val correctAnswer: String
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val topicId: Int = 18,
    val difficulty: String = "easy",
    val largeText: Boolean = false,
    val studyPlace: String = ""
)

@Dao
interface QuizAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<QuizAttemptEntity>>

    @Query("DELETE FROM quiz_attempts")
    suspend fun deleteAll()
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserPreferencesEntity?>

    @Upsert
    suspend fun save(preferences: UserPreferencesEntity)

    @Query("DELETE FROM user_preferences")
    suspend fun deleteAll()
}

@Dao
interface CachedQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceSet(questions: List<CachedQuestionEntity>)

    @Query("SELECT * FROM cached_questions WHERE topicId = :topicId AND difficulty = :difficulty ORDER BY position")
    suspend fun getSet(topicId: Int, difficulty: String): List<CachedQuestionEntity>

    @Query("DELETE FROM cached_questions")
    suspend fun deleteAll()
}

@Database(
    entities = [QuizAttemptEntity::class, UserPreferencesEntity::class, CachedQuestionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun attemptDao(): QuizAttemptDao
    abstract fun preferencesDao(): UserPreferencesDao
    abstract fun cachedQuestionDao(): CachedQuestionDao

    companion object {
        @Volatile private var instance: StudyDatabase? = null

        fun get(context: Context): StudyDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "studylab.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE quiz_attempts ADD COLUMN topicId INTEGER NOT NULL DEFAULT 18")
                database.execSQL("UPDATE quiz_attempts SET topicId = 17 WHERE topicName = 'Science and Nature'")
                database.execSQL("UPDATE quiz_attempts SET topicId = 9 WHERE topicName = 'General Knowledge'")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS cached_questions (" +
                        "topicId INTEGER NOT NULL, difficulty TEXT NOT NULL, position INTEGER NOT NULL, " +
                        "category TEXT NOT NULL, prompt TEXT NOT NULL, answersJson TEXT NOT NULL, " +
                        "correctAnswer TEXT NOT NULL, PRIMARY KEY(topicId, difficulty, position))"
                )
            }
        }
    }
}
