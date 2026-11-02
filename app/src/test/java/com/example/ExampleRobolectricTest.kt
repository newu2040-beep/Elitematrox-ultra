package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.media.VideoProcessingEngine
import com.example.domain.model.EnhancementAdjustments
import com.example.domain.model.ExportConfiguration
import com.example.domain.model.SocialMediaPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name and verify FFmpeg command generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Elite Matrix", appName)

        val adjustments = EnhancementAdjustments(
            brightness = 15,
            contrast = 20,
            saturation = 10,
            temperature = 0,
            sharpness = 25,
            denoise = 15
        )
        val exportConfig = ExportConfiguration(
            resolution = "1080p",
            fps = 30,
            codec = "H.264",
            bitrateMode = "Auto",
            containerFormat = "MP4",
            selectedSocialPreset = SocialMediaPreset.TikTok
        )

        val commandArgs = VideoProcessingEngine.buildFfmpegCommand(
            inputPath = "Nature_Video.mp4",
            outputPath = "Nature_Video_Enhanced.mp4",
            adjustments = adjustments,
            exportConfig = exportConfig
        )
        val joined = commandArgs.joinToString(" ")
        assertTrue(joined.contains("eq=brightness=0.15:contrast=1.20:saturation=1.10"))
        assertTrue(joined.contains("unsharp="))
        assertTrue(joined.contains("hqdn3d="))
        assertTrue(joined.contains("scale=-2:1080"))
        assertTrue(joined.contains("libx264"))
        assertTrue(joined.contains("+faststart"))
    }
}
