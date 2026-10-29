package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioOption
import com.example.model.CompressParams
import com.example.model.CompressionMode
import com.example.model.EncodingPreset
import com.example.model.ResolutionScale
import com.example.model.TermuxPathFormat
import com.example.model.VideoCodec
import com.example.ui.components.CommandCard
import com.example.ui.components.FilePickerCard
import com.example.ui.theme.NeonCyan

@Composable
fun CompressScreen(
    params: CompressParams,
    onParamsChange: ((CompressParams) -> CompressParams) -> Unit,
    onVideoSelected: (Uri) -> Unit,
    pathFormat: TermuxPathFormat,
    onPathFormatChange: (TermuxPathFormat) -> Unit,
    generatedCommand: String,
    onRunInTermux: () -> Unit,
    onCopyCommand: () -> Unit,
    onShareCommand: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onVideoSelected(it) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step 1: Video File
        FilePickerCard(
            title = "1. Sıkıştırılacak Video",
            subtitle = "Boyutunu küçültmek istediğiniz video dosyasını seçin",
            icon = Icons.Default.VideoFile,
            currentPath = params.videoPath,
            onBrowseClick = { videoPickerLauncher.launch("video/*") },
            onPathChanged = { newPath -> onParamsChange { it.copy(videoPath = newPath) } },
            browseButtonLabel = "Video Dosyası Seç",
            testTag = "picker_compress_video"
        )

        // Step 2: Compression Mode (CRF vs Target Size)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Sıkıştırma Yöntemi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = if (params.mode == CompressionMode.CRF_QUALITY) 0 else 1,
                    containerColor = Color.Transparent,
                    contentColor = NeonCyan
                ) {
                    Tab(
                        selected = params.mode == CompressionMode.CRF_QUALITY,
                        onClick = { onParamsChange { it.copy(mode = CompressionMode.CRF_QUALITY) } },
                        text = { Text("CRF Kalite Odaklı") }
                    )
                    Tab(
                        selected = params.mode == CompressionMode.TARGET_FILE_SIZE,
                        onClick = { onParamsChange { it.copy(mode = CompressionMode.TARGET_FILE_SIZE) } },
                        text = { Text("Hedef Boyut (MB)") }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (params.mode == CompressionMode.CRF_QUALITY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "CRF Değeri (Kalite / Boyut Oranı):", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "${params.crf} ${if (params.crf <= 20) "(Yüksek)" else if (params.crf <= 26) "(Dengeli)" else "(Maksimum Sıkıştırma)"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = params.crf.toFloat(),
                        onValueChange = { onParamsChange { current -> current.copy(crf = it.toInt()) } },
                        valueRange = 18f..35f,
                        steps = 16,
                        colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan),
                        modifier = Modifier.testTag("slider_compress_crf")
                    )
                    Text(
                        text = "💡 CRF 23-28 arası değerler görüntü netliğini korurken dosya boyutunu %50-70 azaltır.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                } else {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Hedef Boyut (MB):", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${params.targetSizeMb} MB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick MB chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(10, 16, 25, 50, 100).forEach { mb ->
                                FilterChip(
                                    selected = params.targetSizeMb == mb,
                                    onClick = { onParamsChange { it.copy(targetSizeMb = mb) } },
                                    label = {
                                        Text(when (mb) {
                                            16 -> "16 MB (WhatsApp)"
                                            25 -> "25 MB (Discord)"
                                            else -> "$mb MB"
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

                        OutlinedTextField(
                            value = params.estimatedDurationSeconds.toString(),
                            onValueChange = { str ->
                                val sec = str.filter { it.isDigit() }.toIntOrNull() ?: 60
                                onParamsChange { it.copy(estimatedDurationSeconds = sec) }
                            },
                            label = { Text("Tahmini Video Süresi (Saniye)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "2-Pass Encoding (Hassas Boyut)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Videoyu iki geçişte analiz ederek hedef boyutu tam tutturur.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            Switch(
                                checked = params.twoPass,
                                onCheckedChange = { checked -> onParamsChange { it.copy(twoPass = checked) } }
                            )
                        }
                    }
                }
            }
        }

        // Step 3: Codec & Resolution
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Format & Çözünürlük",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Codec
                Text(text = "Codec Seçimi:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(VideoCodec.H264, VideoCodec.H265, VideoCodec.AV1).forEach { codec ->
                        FilterChip(
                            selected = params.videoCodec == codec,
                            onClick = { onParamsChange { it.copy(videoCodec = codec) } },
                            label = {
                                Text(when (codec) {
                                    VideoCodec.H264 -> "H.264 (Hızlı & Uyumlu)"
                                    VideoCodec.H265 -> "H.265 / HEVC (%40 Küçük)"
                                    VideoCodec.AV1 -> "AV1 (Yeni Nesil)"
                                    else -> codec.id
                                })
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                selectedLabelColor = NeonCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Resolution
                Text(text = "Çözünürlük Küçültme (Önerilir):", style = MaterialTheme.typography.labelMedium)
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

                Spacer(modifier = Modifier.height(12.dp))

                // Audio
                Text(text = "Ses Sıkıştırma:", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(AudioOption.AAC_128, AudioOption.AAC_96, AudioOption.COPY, AudioOption.MUTE).forEach { audio ->
                        FilterChip(
                            selected = params.audioOption == audio,
                            onClick = { onParamsChange { it.copy(audioOption = audio) } },
                            label = {
                                Text(when (audio) {
                                    AudioOption.AAC_128 -> "AAC 128k"
                                    AudioOption.AAC_96 -> "AAC 96k (Tasarruf)"
                                    AudioOption.COPY -> "Sesi Kopyala"
                                    AudioOption.MUTE -> "Sessiz"
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
