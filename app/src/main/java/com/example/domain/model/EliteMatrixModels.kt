package com.example.domain.model

import kotlin.math.abs
import kotlin.math.roundToInt

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

enum class AppFontFamily(val displayName: String, val subtitle: String) {
    PlusJakarta("Plus Jakarta Sans", "Geometric Studio Display"),
    InterFont("Inter Modern", "High-Legibility UI Sans"),
    SystemDefault("System Sans-Serif", "Native Android Roboto"),
    EditorialSerif("Editorial Serif", "Classic Cinema Serif"),
    TechnicalMono("Studio Monospace", "Precision Timecode Mono")
}

enum class ExecutionRoute(val label: String) {
    Auto("Auto"),
    Local("Local"),
    Cloud("Cloud")
}

/**
 * Three real, functional processing performance modes with distinct encoding pipelines,
 * memory footprints, filter passes, and hardware/bitrate strategies.
 */
enum class ProcessingPerformanceMode(
    val badge: String,
    val title: String,
    val subtitle: String,
    val ffmpegPreset: String,
    val bitrateMultiplier: Float,
    val enableMotionInterpolation: Boolean,
    val pixelFilterStep: Int
) {
    Lite(
        badge = "LITE",
        title = "Lite Mode",
        subtitle = "Fastest speed & battery saver • Direct HW pass",
        ffmpegPreset = "veryfast",
        bitrateMultiplier = 0.85f,
        enableMotionInterpolation = false,
        pixelFilterStep = 4
    ),
    Elite(
        badge = "ELITE",
        title = "Elite Mode",
        subtitle = "Balanced speed & studio quality • HW + CFR lock",
        ffmpegPreset = "medium",
        bitrateMultiplier = 1.00f,
        enableMotionInterpolation = true,
        pixelFilterStep = 2
    ),
    Max(
        badge = "MAX",
        title = "Max Mode",
        subtitle = "Maximum detail, 1×1 luma unsharp & 1.25× bitrate headroom",
        ffmpegPreset = "slow",
        bitrateMultiplier = 1.25f,
        enableMotionInterpolation = true,
        pixelFilterStep = 1
    );

    val badgeLabel: String
        get() = badge
}

/**
 * Aspect ratio & framing mode supporting automatic original preservation as well as
 * optional manual framing ratios without stretching or distorting pixels.
 */
enum class AspectRatioMode(
    val label: String,
    val description: String,
    val ratioValue: Double?
) {
    Original("Original", "Preserve exact source aspect ratio", null),
    Vertical9x16("9:16", "Portrait Vertical (Reels / TikTok / Shorts)", 9.0 / 16.0),
    Widescreen16x9("16:9", "Landscape Widescreen (YouTube / Cinema)", 16.0 / 9.0),
    Square1x1("1:1", "Square Feed Format", 1.0),
    Classic4x3("4:3", "Classic Landscape 4:3", 4.0 / 3.0),
    Portrait3x4("3:4", "Portrait 3:4", 3.0 / 4.0),
    Ultrawide21x9("21:9", "Anamorphic Ultrawide", 21.0 / 9.0);

    val shortLabel: String
        get() = label

    companion object {
        fun fromLabel(label: String): AspectRatioMode =
            entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: Original
    }
}

enum class CinematicColorProfile(
    val displayName: String,
    val description: String,
    val contrastDelta: Int,
    val saturationDelta: Int,
    val temperatureDelta: Int,
    val tintDelta: Int,
    val vibranceDelta: Int
) {
    Neutral("Neutral (Original)", "True-to-source color balance", 0, 0, 0, 0, 0),
    TealAndOrange("Cinematic Teal & Orange", "Hollywood complementary shadow/highlight grade", 14, 8, -8, -4, 18),
    HdrVivid("HDR Vivid Boost", "Expanded dynamic micro-contrast and rich chroma", 18, 16, 2, 0, 24),
    GoldenHour("Warm Golden Hour", "Sunlit amber warmth with soft highlight roll-off", 8, 12, 22, 4, 14),
    CrispDaylight("Crisp Daylight", "Clean neutral whites with enhanced clarity", 12, 6, -6, 0, 12),
    MoodyMatte("Moody Matte Film", "Lifted cinema blacks with desaturated tones", 6, -18, -4, 2, -10),
    CyberpunkNeon("Cyberpunk Neon", "High-contrast magenta and cyan night grade", 20, 22, -14, 16, 25),
    MonochromeBw("Studio Monochrome", "High-dynamic-range silver gelatin black & white", 22, -100, 0, 0, 0);

    val label: String
        get() = displayName
}

enum class CompressionMode(
    val displayName: String,
    val description: String,
    val defaultCrf: Int,
    val bitrateFactor: Float
) {
    Smart("Smart", "Adaptive bitrate based on motion and resolution", 21, 0.72f),
    Balanced("Balanced", "Optimal balance of visual quality and file size", 23, 0.60f),
    HighQuality("High Quality", "Visually lossless compression with high bitrate", 18, 0.90f),
    MaxCompression("Maximum Compression", "Smallest file size for quick messaging", 28, 0.42f),
    Custom("Custom", "Manual target size, CRF, bitrate and codec control", 20, 1.0f)
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
    val containerFormat: String,
    val audioBitrateKbps: Int = 256,
    val antiCompressionNote: String = "CFR locked • BT.709 • FastStart moov • 2s GOP"
) {
    TikTok(
        title = "TikTok",
        subtitle = "1080p • 60 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 60,
        codec = "H.264",
        bitrateKbps = 12500,
        containerFormat = "MP4",
        audioBitrateKbps = 256,
        antiCompressionNote = "Tuned below TikTok's re-encode ceiling with 60 FPS CFR & High@L4.2"
    ),
    InstagramReels(
        title = "Instagram Reels",
        subtitle = "1080p • 60 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 60,
        codec = "H.264",
        bitrateKbps = 14000,
        containerFormat = "MP4",
        audioBitrateKbps = 256,
        antiCompressionNote = "Optimized for Reels High-Quality Upload with BT.709 & AAC 48kHz"
    ),
    YouTubeShorts(
        title = "YouTube Shorts",
        subtitle = "1080p • 60 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 60,
        codec = "H.264",
        bitrateKbps = 18000,
        containerFormat = "MP4",
        audioBitrateKbps = 320,
        antiCompressionNote = "High-bitrate 60 FPS VP9/AVC ingest profile to preserve fine detail"
    ),
    YouTube4KMaster(
        title = "YouTube 4K UHD",
        subtitle = "4K UHD • 60 FPS",
        resolution = "4K",
        width = 3840,
        height = 2160,
        fps = 60,
        codec = "H.265",
        bitrateKbps = 42000,
        containerFormat = "MP4",
        audioBitrateKbps = 320,
        antiCompressionNote = "Triggers YouTube VP9/AV1 high-tier transcode pipeline"
    ),
    WhatsAppHdStatus(
        title = "WhatsApp Status HD",
        subtitle = "1080p • 30 FPS",
        resolution = "1080p",
        width = 1080,
        height = 1920,
        fps = 30,
        codec = "H.264",
        bitrateKbps = 6500,
        containerFormat = "MP4",
        audioBitrateKbps = 192,
        antiCompressionNote = "Matches WhatsApp HD media limit to bypass aggressive status downscaling"
    ),
    TwitterXStudio(
        title = "X / Twitter Studio",
        subtitle = "1080p • 60 FPS",
        resolution = "1080p",
        width = 1920,
        height = 1080,
        fps = 60,
        codec = "H.264",
        bitrateKbps = 15000,
        containerFormat = "MP4",
        audioBitrateKbps = 256,
        antiCompressionNote = "Strict 60 FPS CFR & 2s keyframe cadence for X studio player"
    )
}

data class DeviceHardwareCapabilities(
    val displayRefreshRateHz: Int = 60,
    val supportedDisplayModesHz: List<Int> = listOf(60),
    val supportedFpsOptions: List<Int> = listOf(24, 30, 50, 60, 90, 120),
    val maxHardwareFps1080p: Int = 120,
    val maxHardwareFps4k: Int = 60,
    val supportedCodecs: List<String> = listOf("H.264", "H.265"),
    val hardwareEncoderNames: List<String> = emptyList(),
    val hardwareDecoderNames: List<String> = emptyList(),
    val maxSupportedBitrateKbps: Int = 80000,
    val supportsHdrDisplay: Boolean = false,
    val recommendedFps: Int = 60,
    val recommendedResolution: String = "1080p",
    val recommendedPerformanceMode: ProcessingPerformanceMode = ProcessingPerformanceMode.Elite
)

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
    val audioSampleRateHz: Int = 48000,
    val audioBitrateKbps: Int = 192,
    val audioChannels: Int = 2,
    val pixelFormat: String = "yuv420p",
    val rotationDegrees: Int = 0,
    val streamCount: Int = 2,
    val hasAudio: Boolean = true,
    val isHdr: Boolean = false,
    val colorStandard: String = "BT.709",
    val containerFormat: String = "MP4",
    val isVfr: Boolean = false
) {
    val normalizedRotation: Int
        get() = ((rotationDegrees % 360) + 360) % 360

    val isPortrait: Boolean
        get() = displayHeight > displayWidth

    val displayWidth: Int
        get() = if (normalizedRotation == 90 || normalizedRotation == 270) height else width

    val displayHeight: Int
        get() = if (normalizedRotation == 90 || normalizedRotation == 270) width else height

    val displayAspectRatio: Float
        get() = if (displayHeight > 0) displayWidth.toFloat() / displayHeight.toFloat() else 16f / 9f

    val resolutionLabel: String
        get() {
            val shortSide = minOf(displayWidth, displayHeight)
            return when {
                shortSide >= 2160 -> "4K"
                shortSide >= 1440 -> "2K"
                shortSide >= 1080 -> "1080p"
                shortSide >= 720 -> "720p"
                else -> "${displayWidth}×${displayHeight}"
            }
        }

    val exactDimensionsLabel: String
        get() = "${displayWidth}×${displayHeight}"

    val formattedFpsLabel: String
        get() = if (fps % 1f == 0f) "${fps.roundToInt()} FPS" else "${"%.1f".format(fps)} FPS"

    val aspectRatioLabel: String
        get() {
            if (displayWidth <= 0 || displayHeight <= 0) return "16:9"
            val r = displayWidth.toDouble() / displayHeight.toDouble()
            return when {
                abs(r - 9.0 / 16.0) < 0.03 -> "9:16"
                abs(r - 16.0 / 9.0) < 0.03 -> "16:9"
                abs(r - 1.0) < 0.03 -> "1:1"
                abs(r - 4.0 / 3.0) < 0.03 -> "4:3"
                abs(r - 3.0 / 4.0) < 0.03 -> "3:4"
                abs(r - 21.0 / 9.0) < 0.05 -> "21:9"
                else -> {
                    fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)
                    val g = gcd(displayWidth, displayHeight).coerceAtLeast(1)
                    "${displayWidth / g}:${displayHeight / g}"
                }
            }
        }

    val orientationCategoryLabel: String
        get() = when {
            abs(displayWidth - displayHeight) <= 16 -> "Square ($aspectRatioLabel)"
            displayHeight > displayWidth -> "Portrait ($aspectRatioLabel)"
            displayWidth.toFloat() / displayHeight.toFloat() >= 2.1f -> "Ultrawide ($aspectRatioLabel)"
            else -> "Landscape ($aspectRatioLabel)"
        }
}

data class EnhancementAdjustments(
    val brightness: Int = 15,
    val contrast: Int = 20,
    val saturation: Int = 10,
    val temperature: Int = 0,
    // Professional Color Grading & Tone Mapping
    val exposure: Int = 0,
    val vibrance: Int = 10,
    val highlights: Int = 0,
    val shadows: Int = 0,
    val tint: Int = 0,
    val gamma: Float = 1.0f,
    // Detail & Restoration
    val sharpness: Int = 20,
    val denoise: Int = 10,
    val deinterlace: Boolean = false,
    val cinematicProfile: CinematicColorProfile = CinematicColorProfile.Neutral,
    // Orientation & Aspect Ratio Preservation / Framing
    val cropRatio: String = "Original",
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.Original,
    val scaleToFillCrop: Boolean = false,
    val rotationDegrees: Int = 0
) {
    fun isDefault(): Boolean =
        brightness == 0 && contrast == 0 && saturation == 0 && temperature == 0 &&
            exposure == 0 && vibrance == 0 && highlights == 0 && shadows == 0 &&
            gamma == 1.0f && tint == 0 &&
            sharpness == 0 && denoise == 0 && !deinterlace &&
            cinematicProfile == CinematicColorProfile.Neutral &&
            cropRatio == "Original" && aspectRatioMode == AspectRatioMode.Original &&
            rotationDegrees == 0

    companion object {
        val Neutral = EnhancementAdjustments(
            brightness = 0,
            contrast = 0,
            saturation = 0,
            temperature = 0,
            exposure = 0,
            vibrance = 0,
            highlights = 0,
            shadows = 0,
            tint = 0,
            gamma = 1.0f,
            sharpness = 0,
            denoise = 0,
            deinterlace = false,
            cinematicProfile = CinematicColorProfile.Neutral,
            cropRatio = "Original",
            aspectRatioMode = AspectRatioMode.Original,
            scaleToFillCrop = false,
            rotationDegrees = 0
        )
    }
}

data class ExportConfiguration(
    val resolution: String = "1080p", // "720p", "1080p", "2K", "4K", "Original"
    val fps: Int = 60,                // 24, 30, 50, 60, 90, 120
    val codec: String = "H.264",      // "H.264", "H.265", "AV1"
    val bitrateMode: String = "Auto", // "Auto", "Custom"
    val customBitrateKbps: Int = 14000,
    val containerFormat: String = "MP4", // "MP4", "MOV", "MKV"
    val performanceMode: ProcessingPerformanceMode = ProcessingPerformanceMode.Elite,
    val selectedSocialPreset: SocialMediaPreset = SocialMediaPreset.TikTok,
    val socialOptimizationEnabled: Boolean = true, // CFR lock, BT.709, faststart, platform ceiling
    // Advanced Export / HQ Encoder Controls
    val crf: Int = 18,
    val rateControl: String = "CBR/VBR", // "CRF", "CBR", "VBR"
    val encoderPreset: String = "medium", // "ultrafast", "fast", "medium", "slow"
    val profileLevel: String = "High@L4.2",
    val keyframeIntervalSec: Int = 2,
    val pixelFormat: String = "yuv420p",
    // Advanced Audio Export Controls
    val audioCodec: String = "AAC-LC", // "AAC-LC", "HE-AAC", "Opus"
    val audioBitrateKbps: Int = 256,   // 128, 192, 256, 320
    val audioSampleRateHz: Int = 48000, // 44100, 48000
    val audioChannels: Int = 2,        // 1 (Mono), 2 (Stereo)
    val normalizeAudio: Boolean = true,
    val preserveMetadata: Boolean = true,
    val compressionMode: CompressionMode = CompressionMode.HighQuality,
    val hqEncoderPresetName: String = "HQ Standard"
) {
    /**
     * Computes the exact target video bitrate in kbps for the selected resolution, FPS, codec,
     * and ProcessingPerformanceMode (Lite / Elite / Max).
     */
    fun effectiveBitrateKbps(isCompressionWorkflow: Boolean = false): Int {
        if (bitrateMode == "Custom") return customBitrateKbps.coerceIn(500, 160000)
        val base1080p30 = when (resolution) {
            "720p" -> 6500
            "1080p" -> 12000
            "2K" -> 24000
            "4K" -> 45000
            else -> 12000
        }
        val fpsMultiplier = when {
            fps >= 120 -> 2.20f
            fps >= 90 -> 1.80f
            fps >= 60 -> 1.45f
            fps >= 50 -> 1.30f
            fps <= 24 -> 0.88f
            else -> 1.0f
        }
        val codecMultiplier = when (codec) {
            "H.265" -> 0.78f
            "AV1" -> 0.70f
            else -> 1.0f
        }
        val modeFactor = if (isCompressionWorkflow) {
            compressionMode.bitrateFactor * performanceMode.bitrateMultiplier
        } else {
            performanceMode.bitrateMultiplier
        }
        return (base1080p30 * fpsMultiplier * codecMultiplier * modeFactor).roundToInt().coerceIn(800, 160000)
    }

    /**
     * Computes the exact output (width, height) in pixels preserving source aspect orientation
     * (Portrait 9:16, Landscape 16:9, Square 1:1, 4:3, 3:4, Ultrawide, or custom),
     * accounting for any manual user rotation (0°, 90°, 180°, 270°) and optional aspect ratio mode
     * without ever stretching or squashing video frames.
     */
    fun targetDimensionsPx(
        sourceWidth: Int = 1920,
        sourceHeight: Int = 1080,
        manualRotationDegrees: Int = 0,
        aspectRatioMode: AspectRatioMode = AspectRatioMode.Original
    ): Pair<Int, Int> {
        val safeSrcW = if (sourceWidth > 0) sourceWidth else 1920
        val safeSrcH = if (sourceHeight > 0) sourceHeight else 1080

        val normManualRot = ((manualRotationDegrees % 360) + 360) % 360
        val uprightW = if (normManualRot == 90 || normManualRot == 270) safeSrcH else safeSrcW
        val uprightH = if (normManualRot == 90 || normManualRot == 270) safeSrcW else safeSrcH

        val targetAspect = aspectRatioMode.ratioValue ?: (uprightW.toDouble() / uprightH.toDouble())
        val isPortrait = targetAspect < 1.0

        val targetShortSide = when (resolution) {
            "720p" -> 720
            "1080p" -> 1080
            "2K" -> 1440
            "4K" -> 2160
            "Original" -> minOf(uprightW, uprightH)
            else -> 1080
        }

        val rawW: Int
        val rawH: Int
        if (isPortrait) {
            rawW = targetShortSide
            rawH = (targetShortSide / targetAspect).roundToInt()
        } else {
            rawH = targetShortSide
            rawW = (targetShortSide * targetAspect).roundToInt()
        }

        // Align to 2-pixel even boundaries so 1080p stays exact 1920×1080 / 1080×1920 / 1080×1080
        val alignedW = ((rawW + 1) / 2 * 2).coerceAtLeast(320)
        val alignedH = ((rawH + 1) / 2 * 2).coerceAtLeast(320)
        return alignedW to alignedH
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
    val status: String, // queued, processing, paused, completed, failed, cancelled
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
            name = "HQ Standard 60FPS",
            category = "hq_encoder",
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
            sharpness = 22,
            denoise = 10,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_high_detail",
            name = "HQ 2K 60FPS Master",
            category = "hq_encoder",
            resolution = "2K",
            fps = 60,
            codec = "H.265",
            bitrateMode = "Custom",
            bitrateKbps = 28000,
            containerFormat = "MP4",
            crf = 16,
            encoderPreset = "slow",
            brightness = 5,
            contrast = 18,
            saturation = 10,
            temperature = 0,
            sharpness = 35,
            denoise = 12,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_120fps_smooth",
            name = "Ultra Smooth 120FPS",
            category = "hq_encoder",
            resolution = "1080p",
            fps = 120,
            codec = "H.265",
            bitrateMode = "Custom",
            bitrateKbps = 26000,
            containerFormat = "MP4",
            crf = 17,
            encoderPreset = "fast",
            brightness = 8,
            contrast = 16,
            saturation = 10,
            temperature = 0,
            sharpness = 25,
            denoise = 8,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_social",
            name = "Social Media Anti-Compress",
            category = "social",
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 14000,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 12,
            contrast = 18,
            saturation = 10,
            temperature = 0,
            sharpness = 25,
            denoise = 10,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_tiktok",
            name = "TikTok 60FPS Studio",
            category = "social",
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 12500,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 14,
            contrast = 20,
            saturation = 10,
            temperature = 0,
            sharpness = 24,
            denoise = 8,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_reels",
            name = "Instagram Reels 60FPS",
            category = "social",
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 14000,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 12,
            contrast = 18,
            saturation = 10,
            temperature = 2,
            sharpness = 22,
            denoise = 8,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_shorts",
            name = "YouTube Shorts 60FPS",
            category = "social",
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 18000,
            containerFormat = "MP4",
            crf = 17,
            encoderPreset = "medium",
            brightness = 10,
            contrast = 15,
            saturation = 8,
            temperature = 0,
            sharpness = 20,
            denoise = 5,
            isBuiltIn = true
        )
    )
}
