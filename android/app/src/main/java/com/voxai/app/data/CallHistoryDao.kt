package com.voxai.app.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY startTime DESC")
    fun getAll(): Flow<List<CallHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: CallHistoryEntity): Long

    @Query("DELETE FROM call_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_history")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM call_history")
    fun getCount(): Flow<Int>
}

@Database(entities = [CallHistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(TranscriptConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callHistoryDao(): CallHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "voxai_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
