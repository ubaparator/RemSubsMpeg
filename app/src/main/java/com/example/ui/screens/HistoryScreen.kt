package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CommandHistoryEntity
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.util.TermuxLauncher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyList: List<CommandHistoryEntity>,
    onToggleFavorite: (CommandHistoryEntity) -> Unit,
    onDelete: (CommandHistoryEntity) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Geçmiş Komutlar (${historyList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (historyList.isNotEmpty()) {
                OutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.testTag("btn_clear_history")
                ) {
                    Text("Temizle", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Henüz üretilmiş bir komut yok.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hardsub veya Sıkıştırma sekmesinden komut ürettiğinizde burada listelenecektir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(historyList, key = { it.id }) { item ->
                    HistoryItemCard(
                        item = item,
                        formattedDate = dateFormat.format(Date(item.timestamp)),
                        onRunInTermux = { TermuxLauncher.runInTermux(context, item.commandText) },
                        onCopy = { TermuxLauncher.copyToClipboard(context, item.commandText) },
                        onShare = { TermuxLauncher.shareCommand(context, item.commandText) },
                        onToggleFavorite = { onToggleFavorite(item) },
                        onDelete = { onDelete(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: CommandHistoryEntity,
    formattedDate: String,
    onRunInTermux: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("history_item_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$formattedDate • ${if (item.mode == "HARDSUB") "Hardsub Gömme" else "Video Sıkıştırma"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Row {
                IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favori",
                        tint = if (item.isFavorite) TerminalAmber else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Sil",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Code snippet
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .padding(10.dp)
        ) {
            Text(
                text = item.commandText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                ),
                color = TerminalGreen,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onRunInTermux,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF00363D)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f).height(38.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Termux'ta Çalıştır", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(38.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kopyala", fontSize = 11.sp)
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Paylaş", modifier = Modifier.size(16.dp))
            }
        }
    }
}
