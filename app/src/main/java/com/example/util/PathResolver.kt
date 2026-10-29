package com.example.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import com.example.model.TermuxPathFormat
import java.io.File
import java.net.URLDecoder

object PathResolver {

    /**
     * Attempts to resolve an Android Content URI into an absolute filesystem path.
     */
    fun resolveAbsolutePath(context: Context, uri: Uri): String? {
        try {
            // DocumentProvider
            if (DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    if ("primary".equals(type, ignoreCase = true)) {
                        val path = if (split.size > 1) "/${split[1]}" else ""
                        return "${Environment.getExternalStorageDirectory().path}$path"
                    } else {
                        // Secondary storage / SD card (e.g. 1234-5678)
                        val externalDirs = context.getExternalFilesDirs(null)
                        for (dir in externalDirs) {
                            if (dir != null) {
                                val root = dir.path.split("/Android/data")[0]
                                if (root.contains(type)) {
                                    val path = if (split.size > 1) "/${split[1]}" else ""
                                    return "$root$path"
                                }
                            }
                        }
                        return "/storage/$type/${if (split.size > 1) split[1] else ""}"
                    }
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {
                    val id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        return id.substring(4)
                    }
                    if (id.startsWith("msf:")) {
                        val split = id.split(":")
                        if (split.size > 1) {
                            val contentUri = ContentUris.withAppendedId(
                                Uri.parse("content://downloads/public_downloads"),
                                split[1].toLongOrNull() ?: return null
                            )
                            return getDataColumn(context, contentUri, null, null)
                        }
                    }
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        id.toLongOrNull() ?: return null
                    )
                    return getDataColumn(context, contentUri, null, null)
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val type = split[0]

                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
            // MediaStore (and general content providers)
            else if ("content".equals(uri.scheme, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            }
            // File
            else if ("file".equals(uri.scheme, ignoreCase = true)) {
                return uri.path
            }
        } catch (_: Exception) {
            // Fallback to decode path string
        }

        return fallbackDecodeUri(uri)
    }

    private fun fallbackDecodeUri(uri: Uri): String? {
        try {
            val decoded = URLDecoder.decode(uri.toString(), "UTF-8")
            if (decoded.contains("/storage/emulated/0/")) {
                val index = decoded.indexOf("/storage/emulated/0/")
                return decoded.substring(index)
            }
            if (decoded.contains("primary:")) {
                val after = decoded.substringAfter("primary:")
                return "/storage/emulated/0/$after"
            }
        } catch (_: Exception) {}
        return null
    }

    /**
     * Extracts filename from path or URI
     */
    fun getFileName(context: Context, uri: Uri, fallbackPath: String? = null): String {
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    val name = cursor.getString(nameIndex)
                    if (!name.isNullOrBlank()) return name
                }
            }
        } catch (_: Exception) {}

        if (!fallbackPath.isNullOrBlank()) {
            return File(fallbackPath).name
        }
        return uri.lastPathSegment ?: "file"
    }

    /**
     * Transforms an absolute path (/storage/emulated/0/Download/video.mp4)
     * according to selected TermuxPathFormat.
     */
    fun formatForTermux(path: String, format: TermuxPathFormat): String {
        val cleanPath = path.trim()
        return when (format) {
            TermuxPathFormat.TERMUX_SHARED -> {
                when {
                    cleanPath.startsWith("/storage/emulated/0/") ->
                        "~/storage/shared/" + cleanPath.removePrefix("/storage/emulated/0/")
                    cleanPath.startsWith("/sdcard/") ->
                        "~/storage/shared/" + cleanPath.removePrefix("/sdcard/")
                    else -> cleanPath
                }
            }
            TermuxPathFormat.ABSOLUTE_STORAGE -> {
                cleanPath
            }
            TermuxPathFormat.RELATIVE_CD -> {
                File(cleanPath).name
            }
        }
    }

    /**
     * Escapes paths for FFmpeg filtergraphs like `subtitles='...':fontsdir='...'`.
     * In FFmpeg filters, ':' separates options, '\' escapes, and '\'' delimits values.
     */
    fun escapeForFFmpegFilter(path: String): String {
        return path
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace(":", "\\:")
            .replace("[", "\\[")
            .replace("]", "\\]")
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        if (uri == null) return null
        var cursor: Cursor? = null
        val column = MediaStore.MediaColumns.DATA
        val projection = arrayOf(column)

        try {
            cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } catch (_: Exception) {
            // Ignore if column not found or scoped storage restricts it
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }
}
