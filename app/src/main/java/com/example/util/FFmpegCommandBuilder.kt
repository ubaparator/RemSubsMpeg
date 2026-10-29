package com.example.util

import com.example.model.AudioOption
import com.example.model.CompressParams
import com.example.model.CompressionMode
import com.example.model.HardsubParams
import com.example.model.TermuxPathFormat
import java.io.File

object FFmpegCommandBuilder {

    /**
     * Builds the complete FFmpeg command for Hardsub burn-in.
     */
    fun buildHardsubCommand(
        params: HardsubParams,
        pathFormat: TermuxPathFormat
    ): String {
        if (params.videoPath.isBlank()) {
            return "# Lütfen önce bir video dosyası seçin veya yolunu girin."
        }

        val videoFile = File(params.videoPath)
        val parentDir = videoFile.parent ?: "/storage/emulated/0/Download"
        val videoFileName = videoFile.name

        // Determine output path if empty
        val rawOutputPath = if (params.outputPath.isNotBlank()) {
            params.outputPath
        } else {
            val baseName = videoFileName.substringBeforeLast(".")
            File(parentDir, "${baseName}_hardsub.mp4").absolutePath
        }

        val filters = mutableListOf<String>()

        // Subtitle filter
        if (params.isInternalSubtitle) {
            // Built-in stream in MKV/MP4
            val subSource = when (pathFormat) {
                TermuxPathFormat.RELATIVE_CD -> videoFileName
                else -> PathResolver.formatForTermux(params.videoPath, pathFormat)
            }
            val escapedSource = PathResolver.escapeForFFmpegFilter(subSource)
            filters.add("subtitles='${escapedSource}':si=${params.internalStreamIndex}")
        } else if (params.subtitlePath.isNotBlank()) {
            val subFile = File(params.subtitlePath)
            val subFormatted = when (pathFormat) {
                TermuxPathFormat.RELATIVE_CD -> {
                    // Check if subtitle is in the same directory
                    if (subFile.parent == parentDir) {
                        subFile.name
                    } else {
                        PathResolver.formatForTermux(params.subtitlePath, TermuxPathFormat.TERMUX_SHARED)
                    }
                }
                else -> PathResolver.formatForTermux(params.subtitlePath, pathFormat)
            }
            val escapedSub = PathResolver.escapeForFFmpegFilter(subFormatted)

            val fontsArg = if (params.fontsDirPath.isNotBlank()) {
                val fontsFile = File(params.fontsDirPath)
                val fontsFormatted = when (pathFormat) {
                    TermuxPathFormat.RELATIVE_CD -> {
                        if (fontsFile.parent == parentDir) fontsFile.name
                        else PathResolver.formatForTermux(params.fontsDirPath, TermuxPathFormat.TERMUX_SHARED)
                    }
                    else -> PathResolver.formatForTermux(params.fontsDirPath, pathFormat)
                }
                val escapedFonts = PathResolver.escapeForFFmpegFilter(fontsFormatted)
                ":fontsdir='${escapedFonts}'"
            } else ""

            val customOpts = if (params.customFilterOptions.isNotBlank()) {
                ":${params.customFilterOptions.trim().removePrefix(":")}"
            } else ""

            filters.add("subtitles='${escapedSub}'$fontsArg$customOpts")
        }

        // Resolution scale
        if (params.resolution.filterValue != null) {
            filters.add(params.resolution.filterValue)
        }

        // Format paths according to selected TermuxPathFormat
        val inPathFormatted: String
        val outPathFormatted: String
        val cdPrefix: String

        when (pathFormat) {
            TermuxPathFormat.RELATIVE_CD -> {
                cdPrefix = "cd \"${parentDir}\" && \\\n"
                inPathFormatted = videoFileName
                outPathFormatted = File(rawOutputPath).name
            }
            TermuxPathFormat.TERMUX_SHARED -> {
                cdPrefix = ""
                inPathFormatted = PathResolver.formatForTermux(params.videoPath, pathFormat)
                outPathFormatted = PathResolver.formatForTermux(rawOutputPath, pathFormat)
            }
            TermuxPathFormat.ABSOLUTE_STORAGE -> {
                cdPrefix = ""
                inPathFormatted = params.videoPath
                outPathFormatted = rawOutputPath
            }
        }

        val filterArg = if (filters.isNotEmpty()) {
            "-vf \"${filters.joinToString(",")}\" "
        } else ""

        val pixFmtArg = if (params.forceYuv420p) "-pix_fmt yuv420p " else ""

        return "${cdPrefix}ffmpeg -i \"$inPathFormatted\" \\\n" +
                "  $filterArg\\\n" +
                "  -c:v ${params.videoCodec.id} \\\n" +
                "  -preset ${params.preset.id} \\\n" +
                "  -crf ${params.crf} \\\n" +
                "  $pixFmtArg\\\n" +
                "  ${params.audioOption.ffmpegArgs} \\\n" +
                "  \"$outPathFormatted\""
    }

    /**
     * Builds the complete FFmpeg command for Video Compression.
     */
    fun buildCompressCommand(
        params: CompressParams,
        pathFormat: TermuxPathFormat
    ): String {
        if (params.videoPath.isBlank()) {
            return "# Lütfen önce bir video dosyası seçin veya yolunu girin."
        }

        val videoFile = File(params.videoPath)
        val parentDir = videoFile.parent ?: "/storage/emulated/0/Download"
        val videoFileName = videoFile.name

        val rawOutputPath = if (params.outputPath.isNotBlank()) {
            params.outputPath
        } else {
            val baseName = videoFileName.substringBeforeLast(".")
            File(parentDir, "${baseName}_compressed.mp4").absolutePath
        }

        val inPathFormatted: String
        val outPathFormatted: String
        val cdPrefix: String

        when (pathFormat) {
            TermuxPathFormat.RELATIVE_CD -> {
                cdPrefix = "cd \"${parentDir}\" && \\\n"
                inPathFormatted = videoFileName
                outPathFormatted = File(rawOutputPath).name
            }
            TermuxPathFormat.TERMUX_SHARED -> {
                cdPrefix = ""
                inPathFormatted = PathResolver.formatForTermux(params.videoPath, pathFormat)
                outPathFormatted = PathResolver.formatForTermux(rawOutputPath, pathFormat)
            }
            TermuxPathFormat.ABSOLUTE_STORAGE -> {
                cdPrefix = ""
                inPathFormatted = params.videoPath
                outPathFormatted = rawOutputPath
            }
        }

        val filterArg = if (params.resolution.filterValue != null) {
            "-vf \"${params.resolution.filterValue}\" "
        } else ""

        return when (params.mode) {
            CompressionMode.CRF_QUALITY -> {
                "${cdPrefix}ffmpeg -i \"$inPathFormatted\" \\\n" +
                        "  $filterArg\\\n" +
                        "  -c:v ${params.videoCodec.id} \\\n" +
                        "  -preset ${params.preset.id} \\\n" +
                        "  -crf ${params.crf} \\\n" +
                        "  -pix_fmt yuv420p \\\n" +
                        "  ${params.audioOption.ffmpegArgs} \\\n" +
                        "  \"$outPathFormatted\""
            }
            CompressionMode.TARGET_FILE_SIZE -> {
                val duration = if (params.estimatedDurationSeconds > 0) params.estimatedDurationSeconds else 60
                val totalBits = params.targetSizeMb.toLong() * 8192L // in kilobits
                val audioRateKbps = when (params.audioOption) {
                    AudioOption.COPY -> 128
                    AudioOption.AAC_192 -> 192
                    AudioOption.AAC_128 -> 128
                    AudioOption.AAC_96 -> 96
                    AudioOption.MUTE -> 0
                }
                val totalRateKbps = (totalBits / duration).toInt()
                val videoRateKbps = (totalRateKbps - audioRateKbps).coerceAtLeast(64)
                val maxRate = (videoRateKbps * 1.5).toInt()
                val bufSize = (videoRateKbps * 2)

                if (params.twoPass) {
                    "${cdPrefix}ffmpeg -y -i \"$inPathFormatted\" \\\n" +
                            "  $filterArg\\\n" +
                            "  -c:v ${params.videoCodec.id} \\\n" +
                            "  -b:v ${videoRateKbps}k \\\n" +
                            "  -pass 1 \\\n" +
                            "  -an -f null /dev/null && \\\n" +
                            "ffmpeg -i \"$inPathFormatted\" \\\n" +
                            "  $filterArg\\\n" +
                            "  -c:v ${params.videoCodec.id} \\\n" +
                            "  -b:v ${videoRateKbps}k \\\n" +
                            "  -pass 2 \\\n" +
                            "  -preset ${params.preset.id} \\\n" +
                            "  -pix_fmt yuv420p \\\n" +
                            "  ${params.audioOption.ffmpegArgs} \\\n" +
                            "  \"$outPathFormatted\""
                } else {
                    "${cdPrefix}ffmpeg -i \"$inPathFormatted\" \\\n" +
                            "  $filterArg\\\n" +
                            "  -c:v ${params.videoCodec.id} \\\n" +
                            "  -b:v ${videoRateKbps}k \\\n" +
                            "  -maxrate ${maxRate}k \\\n" +
                            "  -bufsize ${bufSize}k \\\n" +
                            "  -preset ${params.preset.id} \\\n" +
                            "  -pix_fmt yuv420p \\\n" +
                            "  ${params.audioOption.ffmpegArgs} \\\n" +
                            "  \"$outPathFormatted\""
                }
            }
        }
    }
}
