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
 * Reliable job-state lifecycle stages for local, hybrid, and cloud processing jobs.
 */
enum class JobStage(val label: String, val badgeText: String) {
    Queued("Queued in pipeline", "QUEUED"),
    Uploading("Uploading source media", "UPLOADING"),
    Processing("Processing & optical flow", "PROCESSING"),
    Encoding("Hardware/CFR encoding", "ENCODING"),
    Finalizing("Muxing & verifying streams", "FINALIZING"),
    Completed("Completed & verified", "COMPLETED"),
    Failed("Processing failed", "FAILED"),
    Cancelled("Job cancelled", "CANCELLED");

    companion object {
        fun fromStatusString(status: String): JobStage {
            val s = status.lowercase()
            return when {
                s.contains("queue") -> Queued
                s.contains("upload") -> Uploading
                s.contains("encod") || s.contains("transcod") -> Encoding
                s.contains("final") || s.contains("mux") || s.contains("verif") || s.contains("download") -> Finalizing
                s.contains("complet") -> Completed
                s.contains("fail") || s.contains("error") -> Failed
                s.contains("cancel") -> Cancelled
                else -> Processing
            }
        }
    }
}

/**
 * Three real, functional processing performance modes with distinct encoding pipelines,
 * memory footprints, filter passes, Optical Flow strategies, and hardware/bitrate multipliers.
 */
enum class ProcessingPerformanceMode(
    val badge: String,
    val title: String,
    val subtitle: String,
    val ffmpegPreset: String,
    val bitrateMultiplier: Float,
    val enableMotionInterpolation: Boolean,
    val pixelFilterStep: Int,
    val benefitsFromCloudNote: String
) {
    Lite(
        badge = "LITE",
        title = "Lite Mode",
        subtitle = "Fastest speed & low battery • Direct HW pass & efficient compression",
        ffmpegPreset = "veryfast",
        bitrateMultiplier = 0.85f,
        enableMotionInterpolation = false,
        pixelFilterStep = 4,
        benefitsFromCloudNote = "Runs 100% on-device with minimal RAM and thermal footprint."
    ),
    Elite(
        badge = "ELITE",
        title = "Elite Mode",
        subtitle = "Balanced speed & studio quality • Detail recovery + Optical Flow CFR",
        ffmpegPreset = "medium",
        bitrateMultiplier = 1.00f,
        enableMotionInterpolation = true,
        pixelFilterStep = 2,
        benefitsFromCloudNote = "Uses hardware acceleration locally; 4K/8K AV1 jobs can offload to Cloud Run."
    ),
    Max(
        badge = "MAX",
        title = "Max Mode",
        subtitle = "Maximum detail, 1×1 Optical Flow MCI, Scene-Cut Guard & 1.25× bitrate",
        ffmpegPreset = "slow",
        bitrateMultiplier = 1.25f,
        enableMotionInterpolation = true,
        pixelFilterStep = 1,
        benefitsFromCloudNote = "Full 1×1 pixel restoration & motion-compensated interpolation; heavy 4K/8K benefits from Cloud Run."
    );

    val badgeLabel: String
        get() = badge
}

/**
 * Frame interpolation & motion synthesis engine modes for 24/25/30/50/60/90/120 FPS output.
 */
enum class MotionInterpolationMode(
    val label: String,
    val shortLabel: String,
    val description: String
) {
    CadenceLock(
        label = "Nearest CFR Lock",
        shortLabel = "CFR Lock",
        description = "Deterministic VFR-to-CFR timestamp normalization without synthetic frames"
    ),
    MotionBlend(
        label = "Temporal Frame Blending",
        shortLabel = "Frame Blend",
        description = "Sub-frame alpha-weighted luma/chroma temporal blending for smooth motion"
    ),
    OpticalFlowStandard(
        label = "Optical Flow (Motion-Compensated)",
        shortLabel = "Optical Flow",
        description = "Block-matching motion vector estimation with ghosting & scene-cut protection"
    ),
    OpticalFlowHighQuality(
        label = "AI Motion-Adaptive Flow (MAX)",
        shortLabel = "AI Optical Flow",
        description = "Bidirectional motion vector synthesis with occlusion & artifact suppression"
    );

    val title: String
        get() = label

    companion object {
        val Off: MotionInterpolationMode get() = CadenceLock
        val FrameBlending: MotionInterpolationMode get() = MotionBlend
        val OpticalFlowMci: MotionInterpolationMode get() = OpticalFlowStandard
        val AiMotionCompensated: MotionInterpolationMode get() = OpticalFlowHighQuality
        val OpticalFlowPro: MotionInterpolationMode get() = OpticalFlowHighQuality
    }
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

/**
 * 14 Professional Creative Filter Categories requested for the Creative Filters & Presets Library.
 */
enum class CreativeFilterCategory(val displayName: String) {
    All("All Filters"),
    Favorites("★ Favorites"),
    KodakFilm("Kodak Film"),
    Fujifilm("Fujifilm"),
    VintageRetro("Vintage & Retro"),
    Cinematic("Cinematic"),
    Natural("Natural"),
    WarmCool("Warm & Cool"),
    Moody("Moody"),
    SoftPastel("Soft Pastel"),
    BlackAndWhite("Black & White"),
    AnalogFilm("Analog Film"),
    NightLowLight("Night & Low-Light"),
    TravelLandscape("Travel & Landscape"),
    PortraitSkinTones("Portrait & Skin"),
    SocialMedia("Social Aesthetic");

    val title: String
        get() = displayName

    val label: String
        get() = displayName
}

/**
 * Built-in professional creative filters across all 14 categories with real color matrix parameters.
 */
enum class CreativeFilterPreset(
    val id: String,
    val displayName: String,
    val category: CreativeFilterCategory,
    val subtitle: String,
    val brightnessDelta: Int,
    val contrastDelta: Int,
    val saturationDelta: Int,
    val temperatureDelta: Int,
    val tintDelta: Int,
    val vibranceDelta: Int,
    val grainAmount: Int = 0,
    val protectSkinTones: Boolean = true
) {
    None(
        id = "filter_none",
        displayName = "Original (No Filter)",
        category = CreativeFilterCategory.Natural,
        subtitle = "Unmodified source color pipeline",
        brightnessDelta = 0,
        contrastDelta = 0,
        saturationDelta = 0,
        temperatureDelta = 0,
        tintDelta = 0,
        vibranceDelta = 0,
        grainAmount = 0
    ),
    // 1. Kodak-inspired film looks
    KodakPortra400(
        id = "kodak_portra_400",
        displayName = "Kodak Portra 400",
        category = CreativeFilterCategory.KodakFilm,
        subtitle = "Warm natural skin tones, soft highlight roll-off & fine grain",
        brightnessDelta = 6,
        contrastDelta = 8,
        saturationDelta = 6,
        temperatureDelta = 14,
        tintDelta = 3,
        vibranceDelta = 14,
        grainAmount = 12,
        protectSkinTones = true
    ),
    KodakGold200(
        id = "kodak_gold_200",
        displayName = "Kodak Gold 200",
        category = CreativeFilterCategory.KodakFilm,
        subtitle = "Nostalgic golden amber warmth and rich daylight saturation",
        brightnessDelta = 8,
        contrastDelta = 12,
        saturationDelta = 14,
        temperatureDelta = 24,
        tintDelta = 4,
        vibranceDelta = 16,
        grainAmount = 15
    ),
    KodakEktar100(
        id = "kodak_ektar_100",
        displayName = "Kodak Ektar 100",
        category = CreativeFilterCategory.KodakFilm,
        subtitle = "Ultra-vivid micro-contrast and punchy primary colors",
        brightnessDelta = 2,
        contrastDelta = 20,
        saturationDelta = 24,
        temperatureDelta = 8,
        tintDelta = -2,
        vibranceDelta = 26,
        grainAmount = 5
    ),
    // 2. Fujifilm-inspired film simulations
    FujiVelvia50(
        id = "fuji_velvia_50",
        displayName = "Fujifilm Velvia 50",
        category = CreativeFilterCategory.Fujifilm,
        subtitle = "Legendary landscape slide film with deep greens & vibrant skies",
        brightnessDelta = 0,
        contrastDelta = 22,
        saturationDelta = 28,
        temperatureDelta = -4,
        tintDelta = -6,
        vibranceDelta = 30,
        grainAmount = 4
    ),
    FujiClassicChrome(
        id = "fuji_classic_chrome",
        displayName = "Fuji Classic Chrome",
        category = CreativeFilterCategory.Fujifilm,
        subtitle = "Documentary magazine look with muted chroma & crisp shadows",
        brightnessDelta = -2,
        contrastDelta = 18,
        saturationDelta = -18,
        temperatureDelta = -6,
        tintDelta = -4,
        vibranceDelta = -8,
        grainAmount = 10
    ),
    FujiAstiaSoft(
        id = "fuji_astia_soft",
        displayName = "Fujifilm Astia Soft",
        category = CreativeFilterCategory.Fujifilm,
        subtitle = "Velvety portrait tones with gentle contrast & natural greens",
        brightnessDelta = 7,
        contrastDelta = 6,
        saturationDelta = 8,
        temperatureDelta = 6,
        tintDelta = 2,
        vibranceDelta = 14,
        grainAmount = 6,
        protectSkinTones = true
    ),
    // 3. Vintage & Retro
    Super8Vintage70s(
        id = "vintage_super8_70s",
        displayName = "70s Super 8 Vintage",
        category = CreativeFilterCategory.VintageRetro,
        subtitle = "Faded blacks, warm sepia-amber cast & organic celluloid grain",
        brightnessDelta = 5,
        contrastDelta = -8,
        saturationDelta = -16,
        temperatureDelta = 28,
        tintDelta = 8,
        vibranceDelta = -10,
        grainAmount = 28
    ),
    RetroVhs90s(
        id = "retro_vhs_90s",
        displayName = "90s Camcorder Retro",
        category = CreativeFilterCategory.VintageRetro,
        subtitle = "Classic magnetic tape chroma shift with lifted midtones",
        brightnessDelta = 6,
        contrastDelta = 10,
        saturationDelta = 12,
        temperatureDelta = -10,
        tintDelta = 12,
        vibranceDelta = 8,
        grainAmount = 22
    ),
    // 4. Cinematic
    AnamorphicBlockbuster(
        id = "cine_anamorphic",
        displayName = "Anamorphic Blockbuster",
        category = CreativeFilterCategory.Cinematic,
        subtitle = "Teal shadows, warm skin highlights & wide dynamic contrast",
        brightnessDelta = 0,
        contrastDelta = 20,
        saturationDelta = 10,
        temperatureDelta = -10,
        tintDelta = -5,
        vibranceDelta = 20,
        grainAmount = 8,
        protectSkinTones = true
    ),
    Kodak2383PrintEmulation(
        id = "cine_2383_print",
        displayName = "Cinema 2383 Print Stock",
        category = CreativeFilterCategory.Cinematic,
        subtitle = "Theatrical release print S-curve with rich shadow density",
        brightnessDelta = -3,
        contrastDelta = 24,
        saturationDelta = 12,
        temperatureDelta = 6,
        tintDelta = 0,
        vibranceDelta = 16,
        grainAmount = 10
    ),
    // 5. Natural
    TrueLifeNatural(
        id = "natural_truelife",
        displayName = "True-Life Studio Clean",
        category = CreativeFilterCategory.Natural,
        subtitle = "Balanced BT.709 clarity with subtle micro-contrast lift",
        brightnessDelta = 4,
        contrastDelta = 10,
        saturationDelta = 6,
        temperatureDelta = 0,
        tintDelta = 0,
        vibranceDelta = 10
    ),
    // 6. Warm & Cool
    SunlitAmberWarm(
        id = "warm_sunlit_amber",
        displayName = "Sunlit Amber Glow",
        category = CreativeFilterCategory.WarmCool,
        subtitle = "Radiant golden-hour warmth with luminous highlights",
        brightnessDelta = 8,
        contrastDelta = 10,
        saturationDelta = 14,
        temperatureDelta = 26,
        tintDelta = 4,
        vibranceDelta = 16
    ),
    ArcticNordicCool(
        id = "cool_arctic_nordic",
        displayName = "Arctic Crystal Cool",
        category = CreativeFilterCategory.WarmCool,
        subtitle = "Crisp glacier blues and clean alabaster highlights",
        brightnessDelta = 6,
        contrastDelta = 14,
        saturationDelta = 8,
        temperatureDelta = -24,
        tintDelta = -4,
        vibranceDelta = 12
    ),
    // 7. Moody
    MoodyNordicNoir(
        id = "moody_nordic_noir",
        displayName = "Moody Cinematic Slate",
        category = CreativeFilterCategory.Moody,
        subtitle = "Desaturated slate greens, deep shadows & atmospheric tone",
        brightnessDelta = -8,
        contrastDelta = 18,
        saturationDelta = -26,
        temperatureDelta = -12,
        tintDelta = -4,
        vibranceDelta = -12,
        grainAmount = 10
    ),
    // 8. Soft Pastel
    SoftPastelDream(
        id = "pastel_soft_dream",
        displayName = "Soft Pastel Peach & Mint",
        category = CreativeFilterCategory.SoftPastel,
        subtitle = "Airy creamy highlights, low contrast & delicate pastel hues",
        brightnessDelta = 14,
        contrastDelta = -12,
        saturationDelta = -8,
        temperatureDelta = 10,
        tintDelta = 6,
        vibranceDelta = 18
    ),
    // 9. Black & White
    IlfordHp5Monochrome(
        id = "bw_ilford_hp5",
        displayName = "Ilford HP5 400 B&W",
        category = CreativeFilterCategory.BlackAndWhite,
        subtitle = "Classic photojournalism monochrome with silver grain structure",
        brightnessDelta = 2,
        contrastDelta = 26,
        saturationDelta = -100,
        temperatureDelta = 0,
        tintDelta = 0,
        vibranceDelta = -100,
        grainAmount = 18
    ),
    // 10. Analog Film
    CineStill800TAnalog(
        id = "analog_cinestill_800t",
        displayName = "CineStill 800T Tungsten",
        category = CreativeFilterCategory.AnalogFilm,
        subtitle = "Tungsten balanced analog stock with halation highlight warmth",
        brightnessDelta = 4,
        contrastDelta = 16,
        saturationDelta = 14,
        temperatureDelta = -16,
        tintDelta = 8,
        vibranceDelta = 20,
        grainAmount = 16
    ),
    // 11. Night & Low-Light
    NightVisionClarity(
        id = "night_lowlight_clarity",
        displayName = "Low-Light Shadow Lift",
        category = CreativeFilterCategory.NightLowLight,
        subtitle = "Recovers dark night shadow detail while suppressing chroma noise",
        brightnessDelta = 18,
        contrastDelta = 12,
        saturationDelta = 6,
        temperatureDelta = -6,
        tintDelta = 2,
        vibranceDelta = 14
    ),
    // 12. Travel & Landscape
    AlpineVistaHDR(
        id = "travel_alpine_hdr",
        displayName = "Alpine Travel HDR",
        category = CreativeFilterCategory.TravelLandscape,
        subtitle = "Sky polarization effect with rich foliage and mountain clarity",
        brightnessDelta = 5,
        contrastDelta = 18,
        saturationDelta = 18,
        temperatureDelta = 4,
        tintDelta = -2,
        vibranceDelta = 24
    ),
    // 13. Portrait & Skin Tones
    StudioPortraitSilk(
        id = "portrait_studio_silk",
        displayName = "Studio Portrait Skin-Safe",
        category = CreativeFilterCategory.PortraitSkinTones,
        subtitle = "Protects natural YCbCr skin hues while enhancing eyes & background",
        brightnessDelta = 8,
        contrastDelta = 8,
        saturationDelta = 6,
        temperatureDelta = 8,
        tintDelta = 2,
        vibranceDelta = 16,
        protectSkinTones = true
    ),
    // 14. Social Media Aesthetics
    ViralCleanCreator(
        id = "social_viral_creator",
        displayName = "Creator Reels Glow",
        category = CreativeFilterCategory.SocialMedia,
        subtitle = "Crisp luminous whites, warm skin & high-engagement color pop",
        brightnessDelta = 10,
        contrastDelta = 14,
        saturationDelta = 12,
        temperatureDelta = 10,
        tintDelta = 2,
        vibranceDelta = 20,
        protectSkinTones = true
    );

    val title: String
        get() = displayName

    val label: String
        get() = displayName

    val exposureShift: Int
        get() = brightnessDelta

    val contrastShift: Int
        get() = contrastDelta

    val saturationShift: Int
        get() = saturationDelta

    val tempShift: Int
        get() = temperatureDelta

    val tintShift: Int
        get() = tintDelta

    val grainHint: Int
        get() = grainAmount

    val isMonochrome: Boolean
        get() = saturationDelta <= -95 || category == CreativeFilterCategory.BlackAndWhite

    val shadowLift: Int
        get() = if (brightnessDelta > 0) brightnessDelta / 2 else 0

    val highlightRollOff: Int
        get() = if (contrastDelta < 0) -contrastDelta else 4

    val rScaleDelta: Float
        get() = temperatureDelta * 0.0025f

    val gScaleDelta: Float
        get() = -tintDelta * 0.0020f

    val bScaleDelta: Float
        get() = -temperatureDelta * 0.0025f
}

enum class LutPreset(val displayName: String, val description: String) {
    None("No 3D LUT", "Bypass LUT stage"),
    Rec709Studio("Rec.709 Broadcast Master", "Standard ITU-R BT.709 studio gamut"),
    Kodak2383Print("Kodak 2383 Film LUT", "Classic cinema print film emulation"),
    Fuji3513Print("Fujifilm 3513DI LUT", "Rich theatrical digital intermediate"),
    TealOrangeMaster("Hollywood Teal-Orange LUT", "Split-toned shadow/highlight grade"),
    LogToRec709("C-Log / S-Log3 to Rec.709", "Normalizes flat camera log profiles");

    val label: String
        get() = displayName

    val title: String
        get() = displayName
}

enum class RgbCurvePreset(val displayName: String, val gammaOffset: Float, val contrastBoost: Int) {
    Linear("Linear (1:1)", 1.0f, 0),
    SCurveContrast("Cinema S-Curve", 0.96f, 16),
    LiftedMatteBlacks("Lifted Matte Blacks", 1.08f, -6),
    SoftHighlightRoll("Soft Highlight Roll-Off", 1.04f, 8);

    val label: String
        get() = displayName

    val title: String
        get() = displayName

    companion object {
        val CinemaSCurve: RgbCurvePreset get() = SCurveContrast
        val FilmFadeToe: RgbCurvePreset get() = LiftedMatteBlacks
        val HighlightRollOff: RgbCurvePreset get() = SoftHighlightRoll
    }
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
    val supportedFpsOptions: List<Int> = listOf(24, 25, 30, 50, 60, 90, 120),
    val maxHardwareFps1080p: Int = 120,
    val maxHardwareFps4k: Int = 60,
    val supportedCodecs: List<String> = listOf("H.264", "H.265"),
    val supportedContainers: List<String> = listOf("MP4", "MOV", "MKV", "WebM"),
    val hardwareEncoderNames: List<String> = emptyList(),
    val hardwareDecoderNames: List<String> = emptyList(),
    val maxSupportedBitrateKbps: Int = 80000,
    val supportsHdrDisplay: Boolean = false,
    val supports8kCloudOrLocal: Boolean = false,
    val freeStorageBytes: Long = 8L * 1024L * 1024L * 1024L,
    val totalStorageBytes: Long = 64L * 1024L * 1024L * 1024L,
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
                shortSide >= 4320 -> "8K"
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
    val hslHueShift: Int = 0,
    val hslLightnessShift: Int = 0,
    val colorBalanceRedCyan: Int = 0,
    val colorBalanceBlueYellow: Int = 0,
    val skinToneProtection: Boolean = true,
    val lutPreset: LutPreset = LutPreset.None,
    val rgbCurvePreset: RgbCurvePreset = RgbCurvePreset.Linear,
    // Creative Filters Library (Kodak, Fujifilm, Vintage, Cinematic, etc.)
    val creativeFilter: CreativeFilterPreset = CreativeFilterPreset.None,
    val filterIntensity: Int = 85, // 0..100%
    // Image Quality, Super-Resolution & Restoration
    val sharpness: Int = 20,
    val denoise: Int = 10,
    val temporalDenoise: Boolean = true,
    val superResolution: Int = 0,
    val detailRecovery: Int = 15,
    val edgeEnhancement: Int = 10,
    val filmGrain: Int = 0,
    val debanding: Int = 10,
    val artifactReduction: Int = 12,
    val deinterlace: Boolean = false,
    val cinematicProfile: CinematicColorProfile = CinematicColorProfile.Neutral,
    // Motion, Optical Flow & Stabilization
    val motionInterpolationMode: MotionInterpolationMode = MotionInterpolationMode.OpticalFlowStandard,
    val sceneCutDetection: Boolean = true,
    val ghostingReduction: Boolean = true,
    val motionBlurReduction: Boolean = false,
    val vfrToCfrConversion: Boolean = true,
    val temporalConsistency: Boolean = true,
    val videoStabilization: Boolean = false,
    val rollingShutterCorrection: Boolean = false,
    val slowMotionFactor: Float = 1.0f, // 1.0f (Normal), 0.5f (2x Slow-Mo), 0.25f (4x Optical Slow-Mo)
    // Orientation & Aspect Ratio Preservation / Framing
    val cropRatio: String = "Original",
    val aspectRatioMode: AspectRatioMode = AspectRatioMode.Original,
    val scaleToFillCrop: Boolean = false,
    val rotationDegrees: Int = 0
) {
    fun isDefault(): Boolean =
        brightness == 0 && contrast == 0 && saturation == 0 && temperature == 0 &&
            exposure == 0 && vibrance == 0 && highlights == 0 && shadows == 0 &&
            gamma == 1.0f && tint == 0 && hslHueShift == 0 && hslLightnessShift == 0 &&
            colorBalanceRedCyan == 0 && colorBalanceBlueYellow == 0 &&
            lutPreset == LutPreset.None && rgbCurvePreset == RgbCurvePreset.Linear &&
            creativeFilter == CreativeFilterPreset.None &&
            sharpness == 0 && denoise == 0 && superResolution == 0 && detailRecovery == 0 &&
            edgeEnhancement == 0 && filmGrain == 0 && debanding == 0 && !deinterlace &&
            !videoStabilization && !rollingShutterCorrection && slowMotionFactor == 1.0f &&
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
            hslHueShift = 0,
            hslLightnessShift = 0,
            colorBalanceRedCyan = 0,
            colorBalanceBlueYellow = 0,
            skinToneProtection = true,
            lutPreset = LutPreset.None,
            rgbCurvePreset = RgbCurvePreset.Linear,
            creativeFilter = CreativeFilterPreset.None,
            filterIntensity = 85,
            sharpness = 0,
            denoise = 0,
            temporalDenoise = false,
            superResolution = 0,
            detailRecovery = 0,
            edgeEnhancement = 0,
            filmGrain = 0,
            debanding = 0,
            artifactReduction = 0,
            deinterlace = false,
            cinematicProfile = CinematicColorProfile.Neutral,
            motionInterpolationMode = MotionInterpolationMode.CadenceLock,
            sceneCutDetection = true,
            ghostingReduction = true,
            motionBlurReduction = false,
            vfrToCfrConversion = true,
            temporalConsistency = false,
            videoStabilization = false,
            rollingShutterCorrection = false,
            slowMotionFactor = 1.0f,
            cropRatio = "Original",
            aspectRatioMode = AspectRatioMode.Original,
            scaleToFillCrop = false,
            rotationDegrees = 0
        )
    }
}

data class ExportConfiguration(
    val resolution: String = "1080p", // "720p", "1080p", "2K", "4K", "8K", "Original"
    val fps: Int = 60,                // 24, 25, 30, 50, 60, 90, 120
    val codec: String = "H.264",      // "H.264", "H.265", "AV1", "ProRes"
    val bitrateMode: String = "Auto", // "Auto", "Custom"
    val customBitrateKbps: Int = 14000,
    val containerFormat: String = "MP4", // "MP4", "MOV", "MKV", "WebM"
    val performanceMode: ProcessingPerformanceMode = ProcessingPerformanceMode.Elite,
    val selectedSocialPreset: SocialMediaPreset = SocialMediaPreset.TikTok,
    val socialOptimizationEnabled: Boolean = true, // CFR lock, BT.709, faststart, platform ceiling
    // Advanced Export / HQ Encoder Controls
    val crf: Int = 18,
    val rateControl: String = "CBR", // "CBR", "VBR", "CRF"
    val useHardwareEncoder: Boolean = true, // Hardware MediaCodec vs Software fallback
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
        if (bitrateMode == "Custom") return customBitrateKbps.coerceIn(500, 240000)
        val base1080p30 = when (resolution) {
            "720p" -> 6500
            "1080p" -> 12000
            "2K" -> 24000
            "4K" -> 45000
            "8K" -> 85000
            else -> 12000
        }
        val fpsMultiplier = when {
            fps >= 120 -> 2.20f
            fps >= 90 -> 1.80f
            fps >= 60 -> 1.45f
            fps >= 50 -> 1.30f
            fps <= 25 -> 0.90f
            else -> 1.0f
        }
        val codecMultiplier = when (codec) {
            "H.265" -> 0.78f
            "AV1" -> 0.70f
            "ProRes" -> 2.40f
            else -> 1.0f
        }
        val modeFactor = if (isCompressionWorkflow) {
            compressionMode.bitrateFactor * performanceMode.bitrateMultiplier
        } else {
            performanceMode.bitrateMultiplier
        }
        return (base1080p30 * fpsMultiplier * codecMultiplier * modeFactor).roundToInt().coerceIn(800, 240000)
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
            "8K" -> 4320
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
        "8K" -> 4320
        else -> sourceHeight
    }

    fun requiresCloudAssist(deviceCapabilities: DeviceHardwareCapabilities): Boolean {
        return resolution == "8K" ||
            (codec == "AV1" && "AV1" !in deviceCapabilities.supportedCodecs) ||
            (resolution == "4K" && fps > deviceCapabilities.maxHardwareFps4k)
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
    val status: String, // queued, uploading, processing, encoding, finalizing, paused, completed, failed, cancelled
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
            name = "Ultra Smooth 120FPS Optical Flow",
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
            id = "builtin_hq_kodak_portra",
            name = "Kodak Portra 400 Cinema 60FPS",
            category = "film_preset",
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Custom",
            bitrateKbps = 16500,
            containerFormat = "MP4",
            crf = 18,
            encoderPreset = "medium",
            brightness = 6,
            contrast = 10,
            saturation = 8,
            temperature = 14,
            sharpness = 18,
            denoise = 10,
            isBuiltIn = true
        ),
        PresetItem(
            id = "builtin_hq_fuji_velvia",
            name = "Fujifilm Velvia 50 Landscape 4K",
            category = "film_preset",
            resolution = "4K",
            fps = 60,
            codec = "H.265",
            bitrateMode = "Custom",
            bitrateKbps = 42000,
            containerFormat = "MP4",
            crf = 16,
            encoderPreset = "medium",
            brightness = 2,
            contrast = 22,
            saturation = 24,
            temperature = -4,
            sharpness = 30,
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
