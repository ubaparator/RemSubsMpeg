package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalGreen
import java.io.File

@Composable
fun FilePickerCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    currentPath: String,
    onBrowseClick: () -> Unit,
    onPathChanged: (String) -> Unit,
    browseButtonLabel: String = "Dosya Seç",
    testTag: String = "file_picker",
    optional: Boolean = false,
    helperNote: String? = null
) {
    var isEditingManually by remember { mutableStateOf(false) }
    val isSelected = currentPath.isNotBlank()
    val fileName = if (isSelected) File(currentPath).name else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                width = 1.dp,
                color = if (isSelected) NeonCyan.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isSelected) NeonCyan.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) NeonCyan else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (optional) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(İsteğe Bağlı)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Seçildi",
                    tint = TerminalGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isSelected && !isEditingManually) {
            // Selected file info box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.InsertDriveFile,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentPath,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { isEditingManually = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Yolu Düzenle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        AnimatedVisibility(visible = isEditingManually || (!isSelected && currentPath.isNotEmpty())) {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = currentPath,
                    onValueChange = onPathChanged,
                    label = { Text("Dosya Yolu (Absolute veya Termux)") },
                    placeholder = { Text("/storage/emulated/0/Download/...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${testTag}_input"),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onBrowseClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.primary,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("${testTag}_browse_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSelected) "Farklı Dosya Seç" else browseButtonLabel,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!isEditingManually && isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isEditingManually = true }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Yolu Düzenle",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else if (isEditingManually) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isEditingManually = false }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Tamam",
                        style = MaterialTheme.typography.bodySmall,
                        color = TerminalGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (helperNote != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helperNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                fontSize = 11.sp
            )
        }
    }
}
