package com.example

import com.example.domain.model.SocialMediaPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `verify social media anti-recompression presets`() {
        val tiktok = SocialMediaPreset.TikTok
        assertEquals(60, tiktok.fps)
        assertEquals("1080p", tiktok.resolution)
        assertTrue(tiktok.bitrateKbps >= 10000)

        val yt4k = SocialMediaPreset.YouTube4KMaster
        assertEquals("4K", yt4k.resolution)
        assertEquals(60, yt4k.fps)
        assertEquals("H.265", yt4k.codec)
    }
}
