package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.model.AudioOption
import com.example.model.EncodingPreset
import com.example.model.HardsubParams
import com.example.model.ResolutionScale
import com.example.model.TermuxPathFormat
import com.example.model.VideoCodec
import com.example.ui.components.CommandCard
import com.example.ui.components.FilePickerCard
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NeonCyan

@Composable
fun HardsubScreen(
    params: HardsubParams,
    onParamsChange: ((HardsubParams) -> HardsubParams) -> Unit,
    onVideoSelected: (Uri) -> Unit,
    onSubtitleSelected: (Uri) -> Unit,
    onFontsDirSelected: (Uri) -> Unit,
    pathFormat: TermuxPathFormat,
    onPathFormatChange: (TermuxPathFormat) -> Unit,
    generatedCommand: String,
    onRunInTermux: () -> Unit,
    onCopyCommand: () -> Unit,
    onShareCommand: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // File pickers
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onVideoSelected(it) }
    }

    val subtitlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onSubtitleSelected(it) }
    }

    val fontsFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { onFontsDirSelected(it) }
    }

    var showAdvanced by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step 1: Video File
        FilePickerCard(
            title = "1. Video Dosyası (.mp4, .mkv)",
            subtitle = "Altyazının gömüleceği ana video dosyasını seçin",
            icon = Icons.Default.Movie,
            currentPath = params.videoPath,
            onBrowseClick = { videoPickerLauncher.launch("video/*") },
            onPathChanged = { newPath -> onParamsChange { it.copy(videoPath = newPath) } },
            browseButtonLabel = "Video Dosyası Seç",
            testTag = "picker_hardsub_video"
        )

        // Step 2: Subtitle Source Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Altyazı Kaynağı",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !params.isInternalSubtitle,
                        onClick = { onParamsChange { it.copy(isInternalSubtitle = false) } },
                        label = { Text("Harici Dosya (.ass / .srt)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("chip_external_sub")
                    )
                    FilterChip(
                        selected = params.isInternalSubtitle,
                        onClick = { onParamsChange { it.copy(isInternalSubtitle = true) } },
                        label = { Text("Dahili MKV Altyazısı") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                            selectedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("chip_internal_sub")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (!params.isInternalSubtitle) {
                    FilePickerCard(
                        title = "Altyazı Dosyası",
                        subtitle = ".ass, .srt veya .vtt formatında altyazı",
                        icon = Icons.Default.Subtitles,
                        currentPath = params.subtitlePath,
                        onBrowseClick = { subtitlePickerLauncher.launch("*/*") },
                        onPathChanged = { newPath -> onParamsChange { it.copy(subtitlePath = newPath) } },
                        browseButtonLabel = "Altyazı Seç (.ass/.srt)",
                        testTag = "picker_hardsub_sub",
                        helperNote = "Termux FFmpeg, .ass formatındaki gelişmiş font stillerini eksiksiz işler."
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Altyazı Kanalı Sırası (si=):",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0, 1, 2, 3).forEach { index ->
                                FilterChip(
                                    selected = params.internalStreamIndex == index,
                                    onClick = { onParamsChange { it.copy(internalStreamIndex = index) } },
                                    label = { Text("#$index") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 3: Font Directory (Optional)
        FilePickerCard(
            title = "3. Özel Yazı Tipi Klasörü (fontsdir)",
            subtitle = "Anime/Dizi ASS altyazılarında kullanılan özel .ttf/.otf yazı tipleri",
            icon = Icons.Default.FontDownload,
            currentPath = params.fontsDirPath,
            onBrowseClick = { fontsFolderPickerLauncher.launch(null) },
            onPathChanged = { newPath -> onParamsChange { it.copy(fontsDirPath = newPath) } },
            browseButtonLabel = "Font Klasörü Seç",
            testTag = "picker_fonts_dir",
            optional = true,
            helperNote = "Yazı tipleri video klasöründeyse veya standart fontlar kullanılıyorsa boş bırakabilirsiniz."
        )

        // Step 4: Video Encoding & Quality Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "4. Video & Kodlama Ayarları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Video Codec
                Text(text = "Video Codec:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VideoCodec.entries.forEach { codec ->
                        FilterChip(
                            selected = params.videoCodec == codec,
                            onClick = { onParamsChange { it.copy(videoCodec = codec) } },
                            label = { Text(codec.id) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CRF Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Kalite (CRF):", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${params.crf} ${if (params.crf <= 19) "(Çok Yüksek)" else if (params.crf <= 23) "(Standart)" else "(Düşük Boyut)"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = params.crf.toFloat(),
                    onValueChange = { onParamsChange { current -> current.copy(crf = it.toInt()) } },
                    valueRange = 14f..32f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonCyan,
                        activeTrackColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("slider_hardsub_crf")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preset
                Text(text = "İşlem Hızı (Preset):", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        EncodingPreset.VERYFAST,
                        EncodingPreset.FAST,
                        EncodingPreset.MEDIUM,
                        EncodingPreset.SLOW
                    ).forEach { preset ->
                        FilterChip(
                            selected = params.preset == preset,
                            onClick = { onParamsChange { it.copy(preset = preset) } },
                            label = { Text(preset.id) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Audio
                Text(text = "Ses İşleme:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(AudioOption.COPY, AudioOption.AAC_192, AudioOption.AAC_128).forEach { audio ->
                        FilterChip(
                            selected = params.audioOption == audio,
                            onClick = { onParamsChange { it.copy(audioOption = audio) } },
                            label = {
                                Text(when (audio) {
                                    AudioOption.COPY -> "Sesi Kopyala (Hızlı)"
                                    AudioOption.AAC_192 -> "AAC 192k"
                                    AudioOption.AAC_128 -> "AAC 128k"
                                    else -> audio.label
                                })
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Resolution
                Text(text = "Çözünürlük Boyutlandırma:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ResolutionScale.entries.forEach { res ->
                        FilterChip(
                            selected = params.resolution == res,
                            onClick = { onParamsChange { it.copy(resolution = res) } },
                            label = { Text(res.label.substringBefore(" ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                // Advanced toggle
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAdvanced = !showAdvanced }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showAdvanced) "Gelişmiş Seçenekleri Gizle ▲" else "Gelişmiş Seçenekler (yuv420p vb.) ▼",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberAccent
                    )
                }

                AnimatedVisibility(visible = showAdvanced) {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "10-bit Uyumluluğu (pix_fmt yuv420p)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Anime ve 10-bit videoların tüm telefonlarda sorunsuz açılmasını sağlar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = params.forceYuv420p,
                                onCheckedChange = { checked ->
                                    onParamsChange { it.copy(forceYuv420p = checked) }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = params.outputPath,
                            onValueChange = { newOut -> onParamsChange { it.copy(outputPath = newOut) } },
                            label = { Text("Özel Çıktı Dosya Adı / Yolu (İsteğe bağlı)") },
                            placeholder = { Text("Örn: /storage/emulated/0/Download/cikti.mp4") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Generated Command Card
        CommandCard(
            command = generatedCommand,
            currentPathFormat = pathFormat,
            onPathFormatChange = onPathFormatChange,
            onRunInTermux = onRunInTermux,
            onCopyCommand = onCopyCommand,
            onShareCommand = onShareCommand,
            onOpenGuide = onOpenGuide
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
