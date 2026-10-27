package com.example

import com.example.data.voice.RealtimeVoiceInterruptionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureRealtimeVoiceSessionTest {
    @Test
    fun bargeInSuppressesBothActiveGenerationAndQueuedPlaybackResponse() {
        assertEquals(
            listOf("resp-new", "resp-buffered"),
            RealtimeVoiceInterruptionPolicy.responseIdsToSuppress("resp-new", "resp-buffered"),
        )
        assertEquals(
            listOf("resp-same"),
            RealtimeVoiceInterruptionPolicy.responseIdsToSuppress("resp-same", "resp-same"),
        )
        assertEquals(
            emptyList<String>(),
            RealtimeVoiceInterruptionPolicy.responseIdsToSuppress(null, " "),
        )
    }

    @Test
    fun remoteCancellationOnlyRunsWhileAResponseIsActuallyInFlight() {
        assertTrue(RealtimeVoiceInterruptionPolicy.shouldCancelRemoteResponse(true, "resp-1"))
        assertFalse(RealtimeVoiceInterruptionPolicy.shouldCancelRemoteResponse(false, "resp-1"))
        assertFalse(RealtimeVoiceInterruptionPolicy.shouldCancelRemoteResponse(true, null))
        assertFalse(RealtimeVoiceInterruptionPolicy.shouldCancelRemoteResponse(true, ""))
    }

    @Test
    fun truncationUsesOnlyPositiveAndBoundedPlayedDuration() {
        assertEquals(0L, RealtimeVoiceInterruptionPolicy.clampTruncationMs(-20L))
        assertEquals(250L, RealtimeVoiceInterruptionPolicy.clampTruncationMs(250L))
        assertEquals(600_000L, RealtimeVoiceInterruptionPolicy.clampTruncationMs(800_000L))
    }
}
