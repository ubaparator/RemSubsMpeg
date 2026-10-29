package com.example

import com.example.model.AudioOption
import com.example.model.CompressParams
import com.example.model.CompressionMode
import com.example.model.EncodingPreset
import com.example.model.HardsubParams
import com.example.model.TermuxPathFormat
import com.example.model.VideoCodec
import com.example.util.FFmpegCommandBuilder
import com.example.util.PathResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testEscapeForFFmpegFilter() {
        val input = "/storage/emulated/0/Download/My [Special]: File's.ass"
        val escaped = PathResolver.escapeForFFmpegFilter(input)
        assertEquals("/storage/emulated/0/Download/My \\[Special\\]\\: File\\'s.ass", escaped)
    }

    @Test
    fun testFormatForTermuxShared() {
        val path = "/storage/emulated/0/Download/sample.mp4"
        val formatted = PathResolver.formatForTermux(path, TermuxPathFormat.TERMUX_SHARED)
        assertEquals("~/storage/shared/Download/sample.mp4", formatted)
    }

    @Test
    fun testBuildHardsubCommandRelative() {
        val params = HardsubParams(
            videoPath = "/storage/emulated/0/Download/anime.mp4",
            subtitlePath = "/storage/emulated/0/Download/subs.ass",
            fontsDirPath = "/storage/emulated/0/Download/fonts",
            videoCodec = VideoCodec.H264,
            preset = EncodingPreset.FAST,
            crf = 20,
            audioOption = AudioOption.COPY
        )

        val cmd = FFmpegCommandBuilder.buildHardsubCommand(params, TermuxPathFormat.RELATIVE_CD)
        assertTrue(cmd.contains("cd \"/storage/emulated/0/Download\""))
        assertTrue(cmd.contains("-i \"anime.mp4\""))
        assertTrue(cmd.contains("subtitles='subs.ass':fontsdir='fonts'"))
        assertTrue(cmd.contains("-c:v libx264"))
        assertTrue(cmd.contains("-crf 20"))
        assertTrue(cmd.contains("-c:a copy"))
    }

    @Test
    fun testBuildCompressCommand() {
        val params = CompressParams(
            videoPath = "/storage/emulated/0/Movies/clip.mp4",
            mode = CompressionMode.CRF_QUALITY,
            crf = 26,
            videoCodec = VideoCodec.H265,
            preset = EncodingPreset.FAST,
            audioOption = AudioOption.AAC_128
        )

        val cmd = FFmpegCommandBuilder.buildCompressCommand(params, TermuxPathFormat.TERMUX_SHARED)
        assertTrue(cmd.contains("-i \"~/storage/shared/Movies/clip.mp4\""))
        assertTrue(cmd.contains("-c:v libx265"))
        assertTrue(cmd.contains("-crf 26"))
        assertTrue(cmd.contains("-c:a aac -b:a 128k"))
    }
}
