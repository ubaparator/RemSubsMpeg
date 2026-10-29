package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

object TermuxLauncher {

    const val TERMUX_PACKAGE_NAME = "com.termux"
    private const val TERMUX_SERVICE_NAME = "com.termux.app.RunCommandService"
    private const val ACTION_RUN_COMMAND = "com.termux.RUN_COMMAND"
    private const val EXTRA_RUN_COMMAND_PATH = "com.termux.RUN_COMMAND_PATH"
    private const val EXTRA_RUN_COMMAND_ARGUMENTS = "com.termux.RUN_COMMAND_ARGUMENTS"
    private const val EXTRA_RUN_COMMAND_WORKDIR = "com.termux.RUN_COMMAND_WORKDIR"
    private const val EXTRA_RUN_COMMAND_BACKGROUND = "com.termux.RUN_COMMAND_BACKGROUND"
    private const val EXTRA_RUN_COMMAND_SESSION_ACTION = "com.termux.RUN_COMMAND_SESSION_ACTION"

    /**
     * Checks if Termux is installed on the device.
     */
    fun isTermuxInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(TERMUX_PACKAGE_NAME, 0)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Copies the command to clipboard.
     */
    fun copyToClipboard(context: Context, command: String, label: String = "FFmpeg Komutu"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, command)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Komut panoya kopyalandı!", Toast.LENGTH_SHORT).show()
            true
        } catch (e: Exception) {
            Toast.makeText(context, "Kopyalama hatası: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    /**
     * Shares the command via Android Sharesheet.
     */
    fun shareCommand(context: Context, command: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "RemSubsMpegKit FFmpeg Komutu")
            putExtra(Intent.EXTRA_TEXT, command)
        }
        val chooser = Intent.createChooser(intent, "FFmpeg Komutunu Paylaş")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    /**
     * Attempts to execute the command directly in Termux using Termux:RunCommandService.
     * Also copies the command to clipboard and opens Termux as an instant fallback.
     */
    fun runInTermux(
        context: Context,
        command: String,
        workDir: String? = null
    ): TermuxLaunchResult {
        // Always copy to clipboard first for safety so user can paste if permission is denied
        copyToClipboard(context, command)

        if (!isTermuxInstalled(context)) {
            return TermuxLaunchResult.NotInstalled
        }

        var intentSent = false
        try {
            // Attempt to dispatch via RunCommandService
            val intent = Intent(ACTION_RUN_COMMAND).apply {
                setComponent(ComponentName(TERMUX_PACKAGE_NAME, TERMUX_SERVICE_NAME))
                putExtra(EXTRA_RUN_COMMAND_PATH, "/data/data/com.termux/files/usr/bin/bash")
                // Single line executable command
                val singleLineCmd = command.replace("\\\n", " ").replace("\n", " ")
                putExtra(EXTRA_RUN_COMMAND_ARGUMENTS, arrayOf("-c", singleLineCmd))
                if (!workDir.isNullOrBlank()) {
                    putExtra(EXTRA_RUN_COMMAND_WORKDIR, workDir)
                }
                putExtra(EXTRA_RUN_COMMAND_BACKGROUND, false)
                putExtra(EXTRA_RUN_COMMAND_SESSION_ACTION, "0") // Open session in foreground
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            intentSent = true
        } catch (_: Exception) {
            intentSent = false
        }

        // Also launch Termux UI so user sees execution or can paste
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(TERMUX_PACKAGE_NAME)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {}

        return if (intentSent) {
            TermuxLaunchResult.Dispatched
        } else {
            TermuxLaunchResult.ClipboardAndLaunched
        }
    }
}

sealed class TermuxLaunchResult {
    data object Dispatched : TermuxLaunchResult()
    data object ClipboardAndLaunched : TermuxLaunchResult()
    data object NotInstalled : TermuxLaunchResult()
}
