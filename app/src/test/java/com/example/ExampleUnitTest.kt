package com.example

import com.example.core.media.VideoProcessingEngine
import com.example.domain.model.AspectRatioMode
import com.example.domain.model.CinematicColorProfile
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.ProcessingPerformanceMode
import com.example.domain.model.SocialMediaPreset
import com.example.domain.model.VideoMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `verify orientation-aware resolution, 60 and 120 FPS CFR, bitrate calculation and FFmpeg pipeline`() {
        val adjustments = EnhancementAdjustments(
            brightness = 15,
            contrast = 20,
            saturation = 10,
            temperature = 0,
            exposure = 10,
            vibrance = 15,
            sharpness = 25,
            denoise = 15,
            cinematicProfile = CinematicColorProfile.TealAndOrange
        )
        val exportConfig = ExportConfiguration(
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Auto",
            containerFormat = "MP4",
            audioCodec = "AAC-LC",
            audioBitrateKbps = 256,
            audioSampleRateHz = 48000,
            selectedSocialPreset = SocialMediaPreset.TikTok,
            performanceMode = ProcessingPerformanceMode.Elite
        )

        // Verify portrait vs landscape target dimension calculation
        val (portraitW, portraitH) = exportConfig.targetDimensionsPx(1080, 1920)
        assertEquals(1080, portraitW)
        assertEquals(1920, portraitH)

        val (landscapeW, landscapeH) = exportConfig.targetDimensionsPx(1920, 1080)
        assertEquals(1920, landscapeW)
        assertEquals(1080, landscapeH)

        // Verify 4K target dimensions
        val export4k = exportConfig.copy(resolution = "4K", fps = 120, codec = "H.265")
        val (w4k, h4k) = export4k.targetDimensionsPx(1920, 1080)
        assertEquals(3840, w4k)
        assertEquals(2160, h4k)

        // Verify full bitrate in export mode (12000 * 1.45 = 17400 kbps) vs scaled bitrate in compression mode
        val exportBitrateKbps = exportConfig.effectiveBitrateKbps(isCompressionWorkflow = false)
        val compressBitrateKbps = exportConfig.effectiveBitrateKbps(isCompressionWorkflow = true)
        assertTrue("Export bitrate should be higher than compressed bitrate", exportBitrateKbps > compressBitrateKbps)
        assertEquals(17400, exportBitrateKbps)

        val commandArgs = VideoProcessingEngine.buildFfmpegCommand(
            inputPath = "Nature_Video.mp4",
            outputPath = "Nature_Video_Enhanced.mp4",
            adjustments = adjustments,
            exportConfig = exportConfig,
            sourceWidth = 1920,
            sourceHeight = 1080,
            isCompressionWorkflow = false
        )
        val joined = commandArgs.joinToString(" ")
        assertTrue(joined.contains("unsharp="))
        assertTrue(joined.contains("hqdn3d="))
        assertTrue(joined.contains("scale=1920:1080"))
        assertTrue(joined.contains("fps=60"))
        assertTrue(joined.contains("-vsync cfr"))
        assertTrue(joined.contains("libx264"))
        assertTrue(joined.contains("-b:v 17400k"))
        assertTrue(joined.contains("-b:a 256k"))
        assertTrue(joined.contains("+faststart"))
    }

    @Test
    fun `verify exact aspect ratio preservation across 9x16, 16x9, 1x1, 4x3, 3x4, 21x9 and rotation metadata`() {
        val config1080 = ExportConfiguration(resolution = "1080p")

        // 1. Square 1:1 source (1080x1080) -> must stay 1080x1080 (never stretched to 1920x1080)
        val (sqW, sqH) = config1080.targetDimensionsPx(1080, 1080)
        assertEquals(1080, sqW)
        assertEquals(1080, sqH)

        // 2. Classic 4:3 source (1440x1080) -> must stay 1440x1080
        val (c43W, c43H) = config1080.targetDimensionsPx(1440, 1080)
        assertEquals(1440, c43W)
        assertEquals(1080, c43H)

        // 3. Portrait 3:4 source (1080x1440) -> must stay 1080x1440
        val (p34W, p34H) = config1080.targetDimensionsPx(1080, 1440)
        assertEquals(1080, p34W)
        assertEquals(1440, p34H)

        // 4. Manual 90° rotation of a 1920x1080 landscape clip -> becomes 1080x1920 portrait
        val (rotW, rotH) = config1080.targetDimensionsPx(
            sourceWidth = 1920,
            sourceHeight = 1080,
            manualRotationDegrees = 90,
            aspectRatioMode = AspectRatioMode.Original
        )
        assertEquals(1080, rotW)
        assertEquals(1920, rotH)

        // 5. Explicit AspectRatioMode override (16:9 clip framed as 9:16 vertical)
        val (ar916W, ar916H) = config1080.targetDimensionsPx(
            sourceWidth = 1920,
            sourceHeight = 1080,
            manualRotationDegrees = 0,
            aspectRatioMode = AspectRatioMode.Vertical9x16
        )
        assertEquals(1080, ar916W)
        assertEquals(1920, ar916H)

        // 6. VideoMetadata rotation normalization (coded 1920x1080 with 90° or 270° metadata -> display 1080x1920 Portrait)
        val meta90 = VideoMetadata(
            uriString = "file:///test.mp4",
            fileName = "front_cam.mp4",
            sizeBytes = 10_000_000L,
            durationMs = 5000L,
            width = 1920,
            height = 1080,
            fps = 60f,
            bitrateKbps = 15000,
            videoCodec = "H.264",
            audioCodec = "AAC-LC",
            containerFormat = "MP4",
            rotationDegrees = 270
        )
        assertEquals(270, meta90.normalizedRotation)
        assertEquals(1080, meta90.displayWidth)
        assertEquals(1920, meta90.displayHeight)
        assertEquals("9:16", meta90.aspectRatioLabel)
        assertEquals("Portrait (9:16)", meta90.orientationCategoryLabel)
    }

    @Test
    fun `verify LITE, ELITE, and MAX performance modes apply distinct encoder presets and bitrates`() {
        val baseConfig = ExportConfiguration(
            resolution = "1080p",
            fps = 60,
            codec = "H.264",
            bitrateMode = "Auto"
        )
        val liteConfig = baseConfig.copy(performanceMode = ProcessingPerformanceMode.Lite)
        val eliteConfig = baseConfig.copy(performanceMode = ProcessingPerformanceMode.Elite)
        val maxConfig = baseConfig.copy(performanceMode = ProcessingPerformanceMode.Max)

        val liteBitrate = liteConfig.effectiveBitrateKbps(isCompressionWorkflow = false)
        val eliteBitrate = eliteConfig.effectiveBitrateKbps(isCompressionWorkflow = false)
        val maxBitrate = maxConfig.effectiveBitrateKbps(isCompressionWorkflow = false)

        assertTrue("LITE bitrate ($liteBitrate) < ELITE bitrate ($eliteBitrate)", liteBitrate < eliteBitrate)
        assertTrue("MAX bitrate ($maxBitrate) > ELITE bitrate ($eliteBitrate)", maxBitrate > eliteBitrate)

        val maxCmd = VideoProcessingEngine.buildFfmpegCommand(
            inputPath = "in.mp4",
            outputPath = "out.mp4",
            adjustments = EnhancementAdjustments.Neutral,
            exportConfig = maxConfig,
            sourceWidth = 1080,
            sourceHeight = 1920
        ).joinToString(" ")
        assertTrue(maxCmd.contains("minterpolate"))
        assertTrue(maxCmd.contains("-preset slow"))
    }

    @Test
    fun `verify Optical Flow interpolation, Creative Film Filters, 3D LUTs, RGB Curves, 8K and ProRes pipeline`() {
        val adjustments = EnhancementAdjustments(
            creativeFilter = com.example.domain.model.CreativeFilterPreset.KodakPortra400,
            filterIntensity = 80,
            lutPreset = com.example.domain.model.LutPreset.Kodak2383Print,
            rgbCurvePreset = com.example.domain.model.RgbCurvePreset.CinemaSCurve,
            superResolution = 30,
            debanding = 20,
            filmGrain = 15,
            artifactReduction = 15,
            skinToneProtection = true,
            motionInterpolationMode = com.example.domain.model.MotionInterpolationMode.OpticalFlowHighQuality,
            sceneCutDetection = true,
            videoStabilization = true,
            slowMotionFactor = 0.5f
        )
        val exportConfig = ExportConfiguration(
            resolution = "8K",
            fps = 90,
            codec = "ProRes",
            containerFormat = "MOV",
            rateControl = "CBR",
            performanceMode = ProcessingPerformanceMode.Max
        )

        val (w8k, h8k) = exportConfig.targetDimensionsPx(3840, 2160)
        assertEquals(7680, w8k)
        assertEquals(4320, h8k)

        val cmd = VideoProcessingEngine.buildFfmpegCommand(
            inputPath = "master.mov",
            outputPath = "master_8k.mov",
            adjustments = adjustments,
            exportConfig = exportConfig,
            sourceWidth = 3840,
            sourceHeight = 2160
        ).joinToString(" ")

        assertTrue(cmd.contains("minterpolate=fps=90:mi_mode=mci:mc_mode=aobmc:me_mode=bidir:vsbmc=1:scd=fdiff"))
        assertTrue(cmd.contains("deshake"))
        assertTrue(cmd.contains("setpts=2.00*PTS"))
        assertTrue(cmd.contains("gradfun="))
        assertTrue(cmd.contains("noise=alls="))
        assertTrue(cmd.contains("curves="))
        assertTrue(cmd.contains("prores_ks"))
    }
}


