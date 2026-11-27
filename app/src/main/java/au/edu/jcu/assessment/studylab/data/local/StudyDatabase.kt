package au.edu.jcu.assessment.studylab.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
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
    val total: Int
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
}

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1 LIMIT 1")
    fun observe(): Flow<UserPreferencesEntity?>

    @Upsert
    suspend fun save(preferences: UserPreferencesEntity)
}

@Database(
    entities = [QuizAttemptEntity::class, UserPreferencesEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StudyDatabase : RoomDatabase() {
    abstract fun attemptDao(): QuizAttemptDao
    abstract fun preferencesDao(): UserPreferencesDao

    companion object {
        @Volatile private var instance: StudyDatabase? = null

        fun get(context: Context): StudyDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                StudyDatabase::class.java,
                "studylab.db"
            ).build().also { instance = it }
        }
    }
}
