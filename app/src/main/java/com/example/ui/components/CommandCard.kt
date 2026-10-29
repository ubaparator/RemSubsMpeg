package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TermuxPathFormat
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalPink

@Composable
fun CommandCard(
    command: String,
    currentPathFormat: TermuxPathFormat,
    onPathFormatChange: (TermuxPathFormat) -> Unit,
    onRunInTermux: () -> Unit,
    onCopyCommand: () -> Unit,
    onShareCommand: () -> Unit,
    onOpenGuide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = NeonCyan.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(16.dp)
            .testTag("command_card")
    ) {
        // Terminal Window Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TerminalPink))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TerminalAmber))
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(TerminalGreen))
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Termux FFmpeg Bash",
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            }

            // Quick Guide link button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Termux Rehberi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.testTag("btn_termux_guide")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Path format selector chips
        Text(
            text = "Yol Formatı:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TermuxPathFormat.entries.forEach { format ->
                val selected = format == currentPathFormat
                FilterChip(
                    selected = selected,
                    onClick = { onPathFormatChange(format) },
                    label = {
                        Text(
                            text = when (format) {
                                TermuxPathFormat.RELATIVE_CD -> "Klasöre Git (cd)"
                                TermuxPathFormat.TERMUX_SHARED -> "~/storage/shared"
                                TermuxPathFormat.ABSOLUTE_STORAGE -> "/storage/emulated/0"
                            },
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                        selectedLabelColor = NeonCyan
                    ),
                    modifier = Modifier.testTag("chip_format_${format.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Code view box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(TerminalBg)
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                text = command,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                color = if (command.startsWith("#")) Color(0xFF90A4AE) else TerminalGreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("command_text_box")
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onRunInTermux,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = Color(0xFF00363D)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("btn_run_termux")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Termux'ta Çalıştır",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            OutlinedButton(
                onClick = onCopyCommand,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("btn_copy_command")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Kopyala",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Kopyala", fontSize = 12.sp)
            }

            IconButton(
                onClick = onShareCommand,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("btn_share_command")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Paylaş",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
