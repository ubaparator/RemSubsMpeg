package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TerminalBg
import com.example.ui.theme.TerminalGreen
import com.example.util.TermuxLauncher

@Composable
fun TermuxGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val allSetupCmd = "termux-setup-storage && pkg update -y && pkg install ffmpeg -y && mkdir -p ~/.termux && echo \"allow-external-apps = true\" >> ~/.termux/termux.properties && termux-reload-settings"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Termux Kurulum Rehberi",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Kapat")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "RemSubsMpegKit tarafından üretilen FFmpeg komutlarını Termux üzerinde sorunsuz çalıştırmak için aşağıdaki adımları 1 kez uygulamanız yeterlidir:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Step 1
                StepItem(
                    stepNum = "1",
                    title = "Depolama İznini Verin",
                    description = "Termux'un telefonunuzdaki dosyalara (/storage/emulated/0) erişmesi için:",
                    code = "termux-setup-storage"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Step 2
                StepItem(
                    stepNum = "2",
                    title = "FFmpeg Paketini Kurun",
                    description = "Gerekli video işleme ve altyazı kütüphanelerini yükler:",
                    code = "pkg update -y && pkg install ffmpeg -y"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Step 3
                StepItem(
                    stepNum = "3",
                    title = "Uygulamadan Doğrudan Çalıştırma İzni",
                    description = "RemSubsMpegKit'in 'Termux'ta Çalıştır' butonuna bastığınızda komutu arka planda/doğrudan gönderebilmesi için:",
                    code = "mkdir -p ~/.termux && echo \"allow-external-apps = true\" >> ~/.termux/termux.properties && termux-reload-settings"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "💡 İpucu: Termux RUN_COMMAND izni etkin değilse bile 'Termux'ta Çalıştır'a bastığınızda komut otomatik panoya kopyalanıp Termux açılır. Ekrana basılı tutup 'Paste' (Yapıştır) demeniz yeterlidir!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    TermuxLauncher.copyToClipboard(context, allSetupCmd, "Termux Kurulum Komutları")
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = Color(0xFF00363D)),
                modifier = Modifier.testTag("btn_copy_all_setup")
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tüm Kurulumu Kopyala", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Tamam")
            }
        }
    )
}

@Composable
private fun StepItem(
    stepNum: String,
    title: String,
    description: String,
    code: String
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(NeonCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stepNum,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00363D)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(TerminalBg)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = code,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = TerminalGreen,
                fontSize = 11.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { TermuxLauncher.copyToClipboard(context, code) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Kopyala",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
