package com.example

import com.example.model.SubtitleFormat
import com.example.parser.SubtitleParser
import com.example.parser.SubtitleWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `parse and generate SRT subtitles`() {
        val srtContent = """
            1
            00:00:01,000 --> 00:00:04,000
            Hello, SubVid Studio!

            2
            00:00:05,500 --> 00:00:08,200
            High performance subtitle editing.
        """.trimIndent()

        val track = SubtitleParser.parseString(srtContent, "test.srt")
        assertEquals(2, track.cues.size)
        assertEquals(1000L, track.cues[0].startTimeMs)
        assertEquals(4000L, track.cues[0].endTimeMs)
        assertEquals("Hello, SubVid Studio!", track.cues[0].text)

        val generated = SubtitleWriter.generate(track, SubtitleFormat.SRT)
        assertTrue(generated.contains("00:00:01,000 --> 00:00:04,000"))
        assertTrue(generated.contains("Hello, SubVid Studio!"))
    }
}
