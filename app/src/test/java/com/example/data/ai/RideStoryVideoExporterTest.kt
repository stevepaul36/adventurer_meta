package com.example.data.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class RideStoryVideoExporterTest {
    @Test
    fun parsesMinuteSecondRanges() {
        val range = RideStoryVideoExporter.parseTimeRange("01:05 - 01:18")
        assertEquals(65_000L, range.startMs)
        assertEquals(78_000L, range.endMs)
        assertEquals(13_000L, range.durationMs)
    }

    @Test
    fun rejectsMalformedOrReversedRanges() {
        assertThrows(IllegalArgumentException::class.java) {
            RideStoryVideoExporter.parseTimeRange("bad timestamp")
        }
        assertThrows(IllegalArgumentException::class.java) {
            RideStoryVideoExporter.parseTimeRange("00:20 - 00:10")
        }
    }

    @Test
    fun limitsExportedClipsToSelectedReelLength() {
        val ranges = RideStoryVideoExporter.limitSegments(
            listOf(
                RideStoryVideoExporter.Segment("00:14 - 00:22", "Summit"),
                RideStoryVideoExporter.Segment("01:05 - 01:18", "Lake"),
                RideStoryVideoExporter.Segment("02:40 - 02:52", "Dunes")
            ),
            "15s Reel"
        )

        assertEquals(2, ranges.size)
        assertEquals(8_000L, ranges[0].durationMs)
        assertEquals(7_000L, ranges[1].durationMs)
        assertEquals(15_000L, ranges.sumOf { it.durationMs })
    }

    @Test
    fun supportsThirtyAndSixtySecondTargets() {
        val clips = listOf(
            RideStoryVideoExporter.Segment("00:00 - 00:20", "A"),
            RideStoryVideoExporter.Segment("00:20 - 00:40", "B"),
            RideStoryVideoExporter.Segment("00:40 - 01:00", "C"),
            RideStoryVideoExporter.Segment("01:00 - 01:20", "D")
        )
        assertEquals(30_000L, RideStoryVideoExporter.limitSegments(clips, "30s Reel").sumOf { it.durationMs })
        assertEquals(60_000L, RideStoryVideoExporter.limitSegments(clips, "60s Reel").sumOf { it.durationMs })
    }
}
