package com.example.domain.model

enum class ThemePalette(val displayName: String, val isDarkPalette: Boolean = false) {
    WarmIvory("Warm Ivory (Default)", false),
    SoftMint("Soft Mint", false),
    EmeraldMist("Emerald Mist", false),
    Cream("Warm Cream", false),
    Lavender("Royal Lavender", false),
    Sky("Nordic Sky", false),
    Peach("Sunset Peach", false),
    RoseQuartz("Rose Quartz", false),
    Sand("Desert Sand", false),
    MatchaZen("Matcha Zen", false),
    ArcticPearl("Arctic Pearl", false),
    Graphite("Graphite Dark", true),
    Midnight("Midnight Slate", true),
    ObsidianMint("Obsidian Emerald", true),
    CyberViolet("Cyber Violet Dark", true),
    MochaEspresso("Mocha Espresso Dark", true)
}

enum class ThemeMode(val displayName: String) {
    Light("Light"),
    Dark("Dark"),
    System("Follow System")
}

enum class ExecutionRoute(val label: String) {
    Auto("Auto"),
    Local("Local"),
    Cloud("Cloud")
}

enum class CompressionMode(
    val displayName: String,
    val description: String,
    val defaultCrf: Int,
    val bitrateFactor: Float
) {
    Smart("Smart", "Adaptive bitrate based on motion and resolution", 21, 0.68f),
    Balanced("Balanced", "Optimal balance of visual quality and file size", 23, 0.58f),
    HighQuality("High Quality", "Visually lossless compression with higher bitrate", 18, 0.82f),
    MaxCompression("Maximum Compression", "Smallest file size for quick messaging", 28, 0.38f),
    Custom("Custom", "Manual target size, CRF, bitrate and codec control", 20, 0.72f)
}

enum class SocialMediaPreset(
    val title: String,
    val subtitle: String,
    val resolution: String,
    val width: Int,
    val height: Int,
    val fps: Int,
    val codec: String,
    val bitrateKbps: Int,
    val containerFormat: String
) {
    TikTok(
        title = "TikTok",
        subtitle = "1080 × 1920 • 30 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 30,
        codec = "H.264",
        bitrateKbps = 10000,
        containerFormat = "MP4"
    ),
    InstagramReels(
        title = "Instagram Reels",
        subtitle = "1080 × 1920 • 30 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 30,
        codec = "H.264",
        bitrateKbps = 12000,
        containerFormat = "MP4"
    ),
    YouTubeShorts(
        title = "YouTube Shorts",
        subtitle = "1080 × 1920 • 60 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 60,
        codec = "H.264",
        bitrateKbps = 16000,
        containerFormat = "MP4"
    )
}

data class VideoMetadata(
    val uriString: String = "",
    val fileName: String = "",
    val sizeBytes: Long = 0L,
    val durationMs: Long = 0L,
    val width: Int = 1920,
    val height: Int = 1080,
    val fps: Float = 30f,
    val bitrateKbps: Int = 8500,
    val videoCodec: String = "H.264 / AVC",
    val audioCodec: String = "AAC-LC",
    val pixelFormat: String = "yuv420p",
    val rotationDegrees: Int = 0,
    val streamCount: Int = 2,
    val hasAudio: Boolean = true,
    val isHdr: Boolean = false,
    val colorStandard: String = "BT.709",
    val containerFormat: String = "MP4",
    val isVfr: Boolean = false
) {
    val resolutionLabel: String
        get() {
            val shortSide = minOf(width, height)
            return when {
                shortSide >= 2160 -> "4K"
                shortSide >= 1440 -> "2K"
                shortSide >= 1080 -> "1080p"
                shortSide >= 720 -> "720p"
                else -> "${width}×${height}"
            }
        }

    val aspectRatioLabel: String
        get() {
            if (width <= 0 || height <= 0) return "16:9"
            fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
            val g = gcd(width, height)
            return "${width / g}:${height / g}"
        }
}

data class EnhancementAdjustments(
    val brightness: Int = 15,
    val contrast: Int = 20,
    val saturation: Int = 10,
    val temperature: Int = 0,
    // Advanced Options
    val gamma: Float = 1.0f,
    val tint: Int = 0,
    val highlights: Int = 0,
    val shadows: Int = 0,
    val sharpness: Int = 20,
    val denoise: Int = 10,
    val deinterlace: Boolean = false,
    val cropRatio: String = "Original",
    val rotationDegrees: Int = 0
) {
    fun isDefault(): Boolean =
        brightness == 0 && contrast == 0 && saturation == 0 && temperature == 0 &&
            gamma == 1.0f && tint == 0 && highlights == 0 && shadows == 0 &&
            sharpness == 0 && denoise == 0 && !deinterlace &&
            cropRatio == "Original" && rotationDegrees == 0

    companion object {
        val Neutral = EnhancementAdjustments(
            brightness = 0,
            contrast = 0,
            saturation = 0,
            temperature = 0,
            gamma = 1.0f,
            tint = 0,
            highlights = 0,
            shadows = 0,
            sharpness = 0,
            denoise = 0,
            deinterlace = false,
            cropRatio = "Original",
            rotationDegrees = 0
        )
    }
}

data class ExportConfiguration(
    val resolution: String = "1080p", // "720p", "1080p", "2K", "4K", "Original"
    val fps: Int = 30,                // 24, 30, 60
    val codec: String = "H.264",      // "H.264", "H.265", "AV1"
    val bitrateMode: String = "Auto", // "Auto", "Custom"
    val customBitrateKbps: Int = 12000,
    val containerFormat: String = "MP4", // "MP4", "MOV", "MKV"
    val selectedSocialPreset: SocialMediaPreset = SocialMediaPreset.TikTok,
    // Advanced Export / HQ Encoder Controls
    val crf: Int = 19,
    val rateControl: String = "CRF", // "CRF", "CBR", "VBR 2-Pass"
    val encoderPreset: String = "medium", // "ultrafast", "fast", "medium", "slow"
    val profileLevel: String = "High@L4.1",
    val keyframeIntervalSec: Int = 2,
    val pixelFormat: String = "yuv420p",
    val audioCodec: String = "AAC",
    val audioBitrateKbps: Int = 192,
    val audioSampleRateHz: Int = 48000,
    val preserveMetadata: Boolean = true,
    val compressionMode: CompressionMode = CompressionMode.Smart,
    val hqEncoderPresetName: String = "HQ Standard"
) {
    fun effectiveBitrateKbps(): Int {
        if (bitrateMode == "Custom") return customBitrateKbps.coerceIn(500, 150000)
        val base = when (resolution) {
            "720p" -> 5500
            "1080p" -> 10500
            "2K" -> 20000
            "4K" -> 38000
            else -> 10500
        }
        val fpsMultiplier = when (fps) {
            60 -> 1.45f
            24 -> 0.88f
            else -> 1.0f
        }
        val codecMultiplier = when (codec) {
            "H.265" -> 0.72f
            "AV1" -> 0.65f
            else -> 1.0f
        }
        return (base * fpsMultiplier * codecMultiplier * compressionMode.bitrateFactor).toInt().coerceAtLeast(800)
    }

    fun targetHeightPx(sourceHeight: Int = 1080): Int = when (resolution) {
        "720p" -> 720
        "1080p" -> 1080
        "2K" -> 1440
        "4K" -> 2160
        else -> sourceHeight
    }
}

data class PresetItem(
    val id: String,
    val name: String,
    val category: String,
    val resolution: String,
    val fps: Int,
    val codec: String,
    val bitrateMode: String,
    val bitrateKbps: Int,
    val containerFormat: String,
    val crf: Int,
    val encoderPreset: String,
    val brightness: Int,
    val contrast: Int,
    val saturation: Int,
    val temperature: Int,
    val sharpness: Int,
    val denoise: Int,
    val isBuiltIn: Boolean = false,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class JobItem(
    val id: String,
    val sourceFileName: String,
    val sourceUri: String,
    val sourceSizeBytes: Long,
    val operationType: String,
    val executionRoute: String,
    val configSummary: String,
    val status: String, // waiting, uploading, queued, processing, downloading, completed, failed, cancelled
    val progressPercent: Int,
    val errorCode: String? = null,
    val errorMessage: String? = null,
    val outputFilePath: String? = null,
    val outputSizeBytes: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

data class ExportItem(
    val id: String,
    val jobId: String,
    val fileName: String,
    val originalSizeBytes: Long,
    val outputSizeBytes: Long,
    val savedPercent: Int,
    val resolution: String,
    val fps: Int,
    val codec: String,
    val containerFormat: String,
    val durationMs: Long,
    val localUri: String,
    val cloudObjectRef: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)

object BuiltInPresets {
    val defaultHqPresets: List<PresetItem> = listOf(
        PresetItem(
            id = "builtin_hq_standard",
            name = "HQ Standard",
            category = "hq_encoder",
            resolution = "1080p",
            fps = 30,
            codec = "H.264",
            bitrateMode = "Auto",
            bitrateKbps = 12000,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 10,
            contrast = 15,
            saturation = 8,
            temperature = 0,
            sharpness = 20,
            denoise = 10,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_high_detail",
            name = "HQ High Detail",
            category = "hq_encoder",
            resolution = "2K",
            fps = 60,
            codec = "H.265",
            bitrateMode = "Custom",
            bitrateKbps = 24000,
            containerFormat = "MP4",
            crf = 16,
            encoderPreset = "slow",
            brightness = 5,
            contrast = 18,
            saturation = 6,
            temperature = 0,
            sharpness = 35,
            denoise = 15,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_social",
            name = "HQ Social Media",
            category = "social",
            resolution = "1080p",
            fps = 30,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 11000,
            containerFormat = "MP4",
            crf = 19,
            encoderPreset = "medium",
            brightness = 15,
            contrast = 20,
            saturation = 10,
            temperature = 0,
            sharpness = 25,
            denoise = 10,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_tiktok",
            name = "HQ TikTok",
            category = "social",
            resolution = "1080p",
            fps = 30,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 10000,
            containerFormat = "MP4",
            crf = 19,
            encoderPreset = "medium",
            brightness = 15,
            contrast = 20,
            saturation = 10,
            temperature = 0,
            sharpness = 22,
            denoise = 8,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_reels",
            name = "HQ Instagram Reels",
            category = "social",
            resolution = "1080p",
            fps = 30,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 12000,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 12,
            contrast = 18,
            saturation = 10,
            temperature = 2,
            sharpness = 20,
            denoise = 8,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_shorts",
            name = "HQ YouTube Shorts",
            category = "social",
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 16000,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 10,
            contrast = 15,
            saturation = 8,
            temperature = 0,
            sharpness = 18,
            denoise = 5,
            isBuiltIn = true
        )
    )
}
