package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.OperationMode
import com.example.ui.MainViewModel
import com.example.ui.components.PresetDialog
import com.example.ui.components.TermuxGuideDialog
import com.example.ui.screens.CompressScreen
import com.example.ui.screens.HardsubScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.util.TermuxLaunchResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Hardsub, 1: Compress, 2: History

    val hardsubParams by viewModel.hardsubParams.collectAsStateWithLifecycle()
    val compressParams by viewModel.compressParams.collectAsStateWithLifecycle()
    val pathFormat by viewModel.pathFormat.collectAsStateWithLifecycle()
    val showGuide by viewModel.showGuide.collectAsStateWithLifecycle()
    val showPresets by viewModel.showPresets.collectAsStateWithLifecycle()
    val historyList by viewModel.history.collectAsStateWithLifecycle()
    val presetList by viewModel.presets.collectAsStateWithLifecycle()
    val launchResult by viewModel.lastLaunchResult.collectAsStateWithLifecycle()

    // Handle back button for sub-tabs
    if (selectedTab != 0) {
        BackHandler {
            selectedTab = 0
            viewModel.setMode(OperationMode.HARDSUB)
        }
    }

    // React to launch result
    LaunchedEffect(launchResult) {
        when (launchResult) {
            is TermuxLaunchResult.Dispatched -> {
                snackbarHostState.showSnackbar("Komut Termux'a gönderildi!")
                viewModel.clearLaunchResult()
            }
            is TermuxLaunchResult.ClipboardAndLaunched -> {
                snackbarHostState.showSnackbar("Komut panoya kopyalandı ve Termux açıldı! Ekrana basıp yapıştırabilirsiniz.")
                viewModel.clearLaunchResult()
            }
            is TermuxLaunchResult.NotInstalled -> {
                snackbarHostState.showSnackbar("Termux yüklü bulunamadı! Lütfen F-Droid veya GitHub üzerinden Termux yükleyin.")
                viewModel.clearLaunchResult()
            }
            null -> {}
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(NeonCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        androidx.compose.foundation.layout.Column {
                            Text(
                                text = "RemSubsMpegKit",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "FFmpeg • Termux Kit",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowPresets(true) },
                        modifier = Modifier.testTag("top_btn_presets")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Şablonlar (Presets)",
                            tint = NeonCyan
                        )
                    }
                    IconButton(
                        onClick = { viewModel.setShowGuide(true) },
                        modifier = Modifier.testTag("top_btn_guide")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Kurulum Rehberi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        viewModel.setMode(OperationMode.HARDSUB)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = "Hardsub Gömme"
                        )
                    },
                    label = { Text("Hardsub Gömme") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_hardsub")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        viewModel.setMode(OperationMode.COMPRESS)
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = "Video Sıkıştırma"
                        )
                    },
                    label = { Text("Sıkıştırma") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_compress")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Geçmiş Komutlar"
                        )
                    },
                    label = { Text("Geçmiş (${historyList.size})") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF00363D),
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    HardsubScreen(
                        params = hardsubParams,
                        onParamsChange = { transform -> viewModel.updateHardsubParams(transform) },
                        onVideoSelected = { uri -> viewModel.onHardsubVideoSelected(uri, context) },
                        onSubtitleSelected = { uri -> viewModel.onHardsubSubtitleSelected(uri, context) },
                        onFontsDirSelected = { uri -> viewModel.onFontsDirSelected(uri, context) },
                        pathFormat = pathFormat,
                        onPathFormatChange = { viewModel.setPathFormat(it) },
                        generatedCommand = viewModel.getCurrentCommand(),
                        onRunInTermux = { viewModel.executeInTermux(context) },
                        onCopyCommand = {
                            viewModel.copyCommand(context)
                            scope.launch { snackbarHostState.showSnackbar("Komut panoya kopyalandı!") }
                        },
                        onShareCommand = { viewModel.shareCommand(context) },
                        onOpenGuide = { viewModel.setShowGuide(true) }
                    )
                }
                1 -> {
                    CompressScreen(
                        params = compressParams,
                        onParamsChange = { transform -> viewModel.updateCompressParams(transform) },
                        onVideoSelected = { uri -> viewModel.onCompressVideoSelected(uri, context) },
                        pathFormat = pathFormat,
                        onPathFormatChange = { viewModel.setPathFormat(it) },
                        generatedCommand = viewModel.getCurrentCommand(),
                        onRunInTermux = { viewModel.executeInTermux(context) },
                        onCopyCommand = {
                            viewModel.copyCommand(context)
                            scope.launch { snackbarHostState.showSnackbar("Komut panoya kopyalandı!") }
                        },
                        onShareCommand = { viewModel.shareCommand(context) },
                        onOpenGuide = { viewModel.setShowGuide(true) }
                    )
                }
                2 -> {
                    HistoryScreen(
                        historyList = historyList,
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDelete = { viewModel.deleteHistory(it) },
                        onClearAll = { viewModel.clearHistory() }
                    )
                }
            }
        }
    }

    if (showGuide) {
        TermuxGuideDialog(
            onDismiss = { viewModel.setShowGuide(false) }
        )
    }

    if (showPresets) {
        PresetDialog(
            presets = presetList,
            onApplyPreset = { preset ->
                viewModel.applyPreset(preset)
                scope.launch { snackbarHostState.showSnackbar("'${preset.name}' şablonu uygulandı!") }
            },
            onSaveNewPreset = { name, desc ->
                viewModel.savePreset(name, desc)
                scope.launch { snackbarHostState.showSnackbar("Yeni şablon kaydedildi!") }
            },
            onDismiss = { viewModel.setShowPresets(false) }
        )
    }
}
