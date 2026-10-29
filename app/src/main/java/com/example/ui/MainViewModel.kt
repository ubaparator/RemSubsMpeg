package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppRepository
import com.example.data.CommandHistoryEntity
import com.example.data.PresetEntity
import com.example.model.AudioOption
import com.example.model.CompressParams
import com.example.model.EncodingPreset
import com.example.model.HardsubParams
import com.example.model.OperationMode
import com.example.model.ResolutionScale
import com.example.model.TermuxPathFormat
import com.example.model.VideoCodec
import com.example.util.FFmpegCommandBuilder
import com.example.util.PathResolver
import com.example.util.TermuxLaunchResult
import com.example.util.TermuxLauncher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository.getInstance(application)

    private val _currentMode = MutableStateFlow(OperationMode.HARDSUB)
    val currentMode: StateFlow<OperationMode> = _currentMode.asStateFlow()

    private val _pathFormat = MutableStateFlow(TermuxPathFormat.RELATIVE_CD)
    val pathFormat: StateFlow<TermuxPathFormat> = _pathFormat.asStateFlow()

    private val _hardsubParams = MutableStateFlow(HardsubParams())
    val hardsubParams: StateFlow<HardsubParams> = _hardsubParams.asStateFlow()

    private val _compressParams = MutableStateFlow(CompressParams())
    val compressParams: StateFlow<CompressParams> = _compressParams.asStateFlow()

    private val _lastLaunchResult = MutableStateFlow<TermuxLaunchResult?>(null)
    val lastLaunchResult: StateFlow<TermuxLaunchResult?> = _lastLaunchResult.asStateFlow()

    private val _showGuide = MutableStateFlow(false)
    val showGuide: StateFlow<Boolean> = _showGuide.asStateFlow()

    private val _showPresets = MutableStateFlow(false)
    val showPresets: StateFlow<Boolean> = _showPresets.asStateFlow()

    val history = repository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val presets = repository.presets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    init {
        viewModelScope.launch {
            repository.seedDefaultPresetsIfNeeded()
        }
    }

    fun setMode(mode: OperationMode) {
        _currentMode.value = mode
    }

    fun setPathFormat(format: TermuxPathFormat) {
        _pathFormat.value = format
    }

    fun setShowGuide(show: Boolean) {
        _showGuide.value = show
    }

    fun setShowPresets(show: Boolean) {
        _showPresets.value = show
    }

    fun clearLaunchResult() {
        _lastLaunchResult.value = null
    }

    // --- Hardsub actions ---

    fun onHardsubVideoSelected(uri: Uri, context: Context) {
        val resolved = PathResolver.resolveAbsolutePath(context, uri)
        val finalPath = resolved ?: uri.toString()
        _hardsubParams.update { it.copy(videoPath = finalPath) }
    }

    fun setHardsubVideoPath(path: String) {
        _hardsubParams.update { it.copy(videoPath = path) }
    }

    fun onHardsubSubtitleSelected(uri: Uri, context: Context) {
        val resolved = PathResolver.resolveAbsolutePath(context, uri)
        val finalPath = resolved ?: uri.toString()
        _hardsubParams.update { it.copy(subtitlePath = finalPath, isInternalSubtitle = false) }
    }

    fun setHardsubSubtitlePath(path: String) {
        _hardsubParams.update { it.copy(subtitlePath = path, isInternalSubtitle = false) }
    }

    fun onFontsDirSelected(uri: Uri, context: Context) {
        val resolved = PathResolver.resolveAbsolutePath(context, uri)
        val finalPath = resolved ?: uri.toString()
        _hardsubParams.update { it.copy(fontsDirPath = finalPath) }
    }

    fun setFontsDirPath(path: String) {
        _hardsubParams.update { it.copy(fontsDirPath = path) }
    }

    fun updateHardsubParams(transform: (HardsubParams) -> HardsubParams) {
        _hardsubParams.update(transform)
    }

    // --- Compress actions ---

    fun onCompressVideoSelected(uri: Uri, context: Context) {
        val resolved = PathResolver.resolveAbsolutePath(context, uri)
        val finalPath = resolved ?: uri.toString()
        _compressParams.update { it.copy(videoPath = finalPath) }
    }

    fun setCompressVideoPath(path: String) {
        _compressParams.update { it.copy(videoPath = path) }
    }

    fun updateCompressParams(transform: (CompressParams) -> CompressParams) {
        _compressParams.update(transform)
    }

    // --- Command generation & execution ---

    fun getCurrentCommand(): String {
        return when (_currentMode.value) {
            OperationMode.HARDSUB -> FFmpegCommandBuilder.buildHardsubCommand(
                _hardsubParams.value,
                _pathFormat.value
            )
            OperationMode.COMPRESS -> FFmpegCommandBuilder.buildCompressCommand(
                _compressParams.value,
                _pathFormat.value
            )
        }
    }

    fun executeInTermux(context: Context) {
        val cmd = getCurrentCommand()
        if (cmd.startsWith("#")) return

        val result = TermuxLauncher.runInTermux(context, cmd)
        _lastLaunchResult.value = result

        // Save to history
        viewModelScope.launch {
            val title = when (_currentMode.value) {
                OperationMode.HARDSUB -> "Hardsub: " + (_hardsubParams.value.videoPath.substringAfterLast("/"))
                OperationMode.COMPRESS -> "Sıkıştırma: " + (_compressParams.value.videoPath.substringAfterLast("/"))
            }
            repository.saveHistory(_currentMode.value.name, title, cmd)
        }
    }

    fun copyCommand(context: Context) {
        val cmd = getCurrentCommand()
        TermuxLauncher.copyToClipboard(context, cmd)
        viewModelScope.launch {
            val title = when (_currentMode.value) {
                OperationMode.HARDSUB -> "Hardsub: " + (_hardsubParams.value.videoPath.substringAfterLast("/"))
                OperationMode.COMPRESS -> "Sıkıştırma: " + (_compressParams.value.videoPath.substringAfterLast("/"))
            }
            repository.saveHistory(_currentMode.value.name, title, cmd)
        }
    }

    fun shareCommand(context: Context) {
        val cmd = getCurrentCommand()
        TermuxLauncher.shareCommand(context, cmd)
    }

    fun applyPreset(preset: PresetEntity) {
        if (preset.mode == "HARDSUB") {
            _currentMode.value = OperationMode.HARDSUB
            _hardsubParams.update { current ->
                current.copy(
                    videoCodec = VideoCodec.entries.find { it.id == preset.codec } ?: current.videoCodec,
                    preset = EncodingPreset.entries.find { it.id == preset.preset } ?: current.preset,
                    crf = preset.crf,
                    audioOption = AudioOption.entries.find { it.id == preset.audioOption } ?: current.audioOption
                )
            }
        } else {
            _currentMode.value = OperationMode.COMPRESS
            _compressParams.update { current ->
                current.copy(
                    videoCodec = VideoCodec.entries.find { it.id == preset.codec } ?: current.videoCodec,
                    preset = EncodingPreset.entries.find { it.id == preset.preset } ?: current.preset,
                    crf = preset.crf,
                    audioOption = AudioOption.entries.find { it.id == preset.audioOption } ?: current.audioOption
                )
            }
        }
        _showPresets.value = false
    }

    fun savePreset(name: String, desc: String) {
        viewModelScope.launch {
            val mode = _currentMode.value
            val entity = if (mode == OperationMode.HARDSUB) {
                val p = _hardsubParams.value
                PresetEntity(
                    name = name,
                    mode = "HARDSUB",
                    codec = p.videoCodec.id,
                    preset = p.preset.id,
                    crf = p.crf,
                    audioOption = p.audioOption.id,
                    description = desc
                )
            } else {
                val p = _compressParams.value
                PresetEntity(
                    name = name,
                    mode = "COMPRESS",
                    codec = p.videoCodec.id,
                    preset = p.preset.id,
                    crf = p.crf,
                    audioOption = p.audioOption.id,
                    description = desc
                )
            }
            repository.savePreset(entity)
        }
    }

    fun toggleFavorite(item: CommandHistoryEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, item.isFavorite)
        }
    }

    fun deleteHistory(item: CommandHistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(item.id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}
