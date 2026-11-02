package com.example

import com.example.core.media.VideoProcessingEngine
import com.example.domain.model.CinematicColorProfile
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.SocialMediaPreset
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
            selectedSocialPreset = SocialMediaPreset.TikTok
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
        assertTrue(joined.contains("scale=1920:1080:flags=lanczos"))
        assertTrue(joined.contains("fps=60"))
        assertTrue(joined.contains("-vsync cfr"))
        assertTrue(joined.contains("libx264"))
        assertTrue(joined.contains("-b:v 17400k"))
        assertTrue(joined.contains("-b:a 256k"))
        assertTrue(joined.contains("+faststart"))
    }
}
