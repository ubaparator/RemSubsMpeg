package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository private constructor(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "remsubs_mpegkit.db"
    ).build()

    private val historyDao = db.historyDao()
    private val presetDao = db.presetDao()

    val history: Flow<List<CommandHistoryEntity>> = historyDao.getAllHistory()
    val presets: Flow<List<PresetEntity>> = presetDao.getAllPresets()

    suspend fun saveHistory(mode: String, title: String, command: String) {
        historyDao.insertHistory(
            CommandHistoryEntity(
                mode = mode,
                title = title,
                commandText = command
            )
        )
    }

    suspend fun toggleFavorite(id: Long, current: Boolean) {
        historyDao.updateFavorite(id, !current)
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteHistory(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
    }

    suspend fun savePreset(preset: PresetEntity) {
        presetDao.insertPreset(preset)
    }

    suspend fun deletePreset(id: Long) {
        presetDao.deletePreset(id)
    }

    suspend fun seedDefaultPresetsIfNeeded() {
        val existing = presets.firstOrNull()
        if (existing.isNullOrEmpty()) {
            presetDao.insertPreset(
                PresetEntity(
                    name = "Anime Yüksek Kalite Hardsub",
                    mode = "HARDSUB",
                    codec = "libx264",
                    preset = "fast",
                    crf = 19,
                    audioOption = "copy",
                    description = "Görüntü kaybı olmadan kristal netlikte ASS altyazı gömer."
                )
            )
            presetDao.insertPreset(
                PresetEntity(
                    name = "Discord 25 MB Sıkıştırma",
                    mode = "COMPRESS",
                    codec = "libx264",
                    preset = "fast",
                    crf = 26,
                    audioOption = "aac_128",
                    description = "Discord ve sohbet uygulamaları için hızlı 25 MB sıkıştırma."
                )
            )
            presetDao.insertPreset(
                PresetEntity(
                    name = "H.265 / HEVC Ultra Verimli",
                    mode = "COMPRESS",
                    codec = "libx265",
                    preset = "fast",
                    crf = 24,
                    audioOption = "copy",
                    description = "%45'e varan yer tasarrufu sağlayan modern HEVC sıkıştırma."
                )
            )
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AppRepository? = null

        fun getInstance(context: Context): AppRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppRepository(context).also { INSTANCE = it }
            }
        }
    }
}
