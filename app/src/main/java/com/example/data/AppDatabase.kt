package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "command_history")
data class CommandHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String, // "HARDSUB" or "COMPRESS"
    val title: String,
    val commandText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val mode: String,
    val codec: String,
    val preset: String,
    val crf: Int,
    val audioOption: String,
    val description: String = ""
)

@Dao
interface HistoryDao {
    @Query("SELECT * FROM command_history ORDER BY timestamp DESC LIMIT 50")
    fun getAllHistory(): Flow<List<CommandHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(item: CommandHistoryEntity): Long

    @Query("DELETE FROM command_history WHERE id = :id")
    suspend fun deleteHistory(id: Long)

    @Query("DELETE FROM command_history")
    suspend fun clearAll()

    @Query("UPDATE command_history SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)
}

@Dao
interface PresetDao {
    @Query("SELECT * FROM presets ORDER BY id ASC")
    fun getAllPresets(): Flow<List<PresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(item: PresetEntity): Long

    @Query("DELETE FROM presets WHERE id = :id")
    suspend fun deletePreset(id: Long)
}

@Database(
    entities = [CommandHistoryEntity::class, PresetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun presetDao(): PresetDao
}
