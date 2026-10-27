package com.example

import com.example.data.voice.PendingTextTurnQueue
import com.example.data.voice.RealtimeUserTranscriptBuffer
import com.example.data.voice.RealtimeVoiceInterruptionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureRealtimeVoiceSessionTest {
    @Test
    fun typedTurnsStayOrderedAndQueueCapacityIsBounded() {
        val queue = PendingTextTurnQueue(capacity = 3)
        assertTrue(queue.offer(" first "))
        assertTrue(queue.offer("second"))
        assertTrue(queue.offer("third"))
        assertFalse(queue.offer("fourth"))
        assertEquals("first", queue.poll())
        assertEquals("second", queue.poll())
        assertEquals("third", queue.poll())
        assertNull(queue.poll())
    }

    @Test
    fun clearingQueueOnDisconnectRemovesPendingMessages() {
        val queue = PendingTextTurnQueue()
        assertTrue(queue.offer("hello"))
        queue.clear()
        assertNull(queue.poll())
        assertFalse(queue.offer("  "))
    }

    @Test
    fun incomingSpeechTranscriptsStaySeparateAndCompleteOncePerItem() {
        val transcripts = RealtimeUserTranscriptBuffer()
        assertEquals("Could ", transcripts.append("item-1", "Could "))
        assertEquals("Could you", transcripts.append("item-1", "you"))
        assertEquals("Find Niagara Falls", transcripts.append("item-2", "Find Niagara Falls"))
        assertEquals("Could you find the lake?", transcripts.complete("item-1", "Could you find the lake?"))
        assertEquals("Find Niagara Falls", transcripts.complete("item-2", ""))
        assertEquals(null, transcripts.complete("item-2", ""))
    }

    @Test
    fun incomingTranscriptBufferCapsTextAndCanBeClearedOnDisconnect() {
        val transcripts = RealtimeUserTranscriptBuffer(maxItems = 2, maxCharacters = 12)
        transcripts.append("one", "one")
        transcripts.append("two", "two")
        transcripts.append("three", "three")
        assertEquals(null, transcripts.complete("one", ""))
        assertEquals("two", transcripts.complete("two", ""))
        transcripts.append("four", "still here")
        transcripts.clear()
        assertEquals(null, transcripts.complete("four", ""))
        assertEquals("789012345678", transcripts.append("limit", "123456789012345678"))
    }

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
