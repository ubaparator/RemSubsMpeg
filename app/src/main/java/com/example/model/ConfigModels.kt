package com.example.model

enum class OperationMode(val title: String, val subtitle: String) {
    HARDSUB("Hardsub Gömme", ".ass / .srt altyazıyı videoya kalıcı olarak işle"),
    COMPRESS("Video Sıkıştırma", "Boyut küçültme, çözünürlük ve bitrate optimizasyonu")
}

enum class TermuxPathFormat(val title: String, val description: String) {
    RELATIVE_CD("Klasöre Git (cd) [Önerilen]", "Dosya klasörüne geçip kısa isimlerle çalıştırır, hata riskini sıfırlar"),
    TERMUX_SHARED("~/storage/shared/...", "Termux standart sembolik depolama bağlantısı"),
    ABSOLUTE_STORAGE("/storage/emulated/0/...", "Android doğrudan mutlak depolama yolu")
}

enum class VideoCodec(val id: String, val label: String, val description: String) {
    H264("libx264", "H.264 / AVC (libx264)", "En uyumlu format, tüm cihazlar ve oynatıcılar destekler"),
    H265("libx265", "H.265 / HEVC (libx265)", "%40-50 daha az boyut, yüksek verimlilik"),
    VP9("libvpx-vp9", "VP9 (libvpx-vp9)", "Açık kaynak WebM/YouTube formatı"),
    AV1("libsvtav1", "AV1 (libsvtav1)", "Yeni nesil üstün sıkıştırma (daha uzun sürer)")
}

enum class EncodingPreset(val id: String, val label: String) {
    ULTRAFAST("ultrafast", "Ultra Hızlı (Büyük dosya)"),
    VERYFAST("veryfast", "Çok Hızlı"),
    FAST("fast", "Hızlı"),
    MEDIUM("medium", "Dengeli (Varsayılan)"),
    SLOW("slow", "Yavaş (Yüksek sıkıştırma)"),
    SLOWER("slower", "Çok Yavaş")
}

enum class ResolutionScale(val id: String, val label: String, val filterValue: String?) {
    ORIGINAL("original", "Orijinal Çözünürlük", null),
    P1080("1080p", "1080p FHD (scale=-2:1080)", "scale=-2:1080"),
    P720("720p", "720p HD (scale=-2:720)", "scale=-2:720"),
    P480("480p", "480p SD (scale=-2:480)", "scale=-2:480"),
    P360("360p", "360p Düşük (scale=-2:360)", "scale=-2:360")
}

enum class AudioOption(val id: String, val label: String, val ffmpegArgs: String) {
    COPY("copy", "Sesi Kopyala (Yeniden kodlama yok, en hızlı)", "-c:a copy"),
    AAC_192("aac_192", "AAC 192 kbps (Yüksek Kalite)", "-c:a aac -b:a 192k"),
    AAC_128("aac_128", "AAC 128 kbps (Standart)", "-c:a aac -b:a 128k"),
    AAC_96("aac_96", "AAC 96 kbps (Düşük Boyut)", "-c:a aac -b:a 96k"),
    MUTE("mute", "Sesi Kaldır (Sessiz Video)", "-an")
}

enum class SubtitleType {
    EXTERNAL_ASS,
    EXTERNAL_SRT,
    INTERNAL_STREAM
}

data class HardsubParams(
    val videoPath: String = "",
    val subtitlePath: String = "",
    val fontsDirPath: String = "",
    val isInternalSubtitle: Boolean = false,
    val internalStreamIndex: Int = 0,
    val videoCodec: VideoCodec = VideoCodec.H264,
    val preset: EncodingPreset = EncodingPreset.FAST,
    val crf: Int = 20,
    val resolution: ResolutionScale = ResolutionScale.ORIGINAL,
    val audioOption: AudioOption = AudioOption.COPY,
    val forceYuv420p: Boolean = true,
    val customFilterOptions: String = "",
    val outputPath: String = ""
)

enum class CompressionMode {
    CRF_QUALITY,
    TARGET_FILE_SIZE
}

data class CompressParams(
    val videoPath: String = "",
    val mode: CompressionMode = CompressionMode.CRF_QUALITY,
    val crf: Int = 24,
    val targetSizeMb: Int = 25,
    val estimatedDurationSeconds: Int = 120,
    val videoCodec: VideoCodec = VideoCodec.H264,
    val preset: EncodingPreset = EncodingPreset.FAST,
    val resolution: ResolutionScale = ResolutionScale.ORIGINAL,
    val audioOption: AudioOption = AudioOption.AAC_128,
    val twoPass: Boolean = false,
    val outputPath: String = ""
)
