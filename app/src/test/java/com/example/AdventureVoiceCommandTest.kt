package com.example

import com.example.data.model.ActivityType
import com.example.data.voice.AdventureVoiceAction
import com.example.data.voice.AdventureVoiceCommandParser
import com.example.data.voice.AdventureVoiceSafetyPolicy
import com.example.data.voice.VoiceMotionSnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureVoiceCommandTest {
    @Test
    fun wakePhraseCanLaunchNavigationInOneUtterance() {
        val command = AdventureVoiceCommandParser.parse("Hey Adventurer, open Explore", requireWakePhrase = true)
        assertEquals(AdventureVoiceAction.OPEN_EXPLORE, command.action)
        assertTrue(command.wakeDetected)
    }

    @Test
    fun ambientSpeechIsIgnoredWhenWaitingForWakePhrase() {
        val command = AdventureVoiceCommandParser.parse("open Explore", requireWakePhrase = true)
        assertEquals(AdventureVoiceAction.IGNORED, command.action)
        assertFalse(command.wakeDetected)
    }

    @Test
    fun wakePhraseAloneStartsCommandCapture() {
        val command = AdventureVoiceCommandParser.parse("Hey Adventurer", requireWakePhrase = true)
        assertEquals(AdventureVoiceAction.WAKE_ONLY, command.action)
    }

    @Test
    fun supportsBackAndForwardVoiceNavigation() {
        assertEquals(AdventureVoiceAction.BACK, AdventureVoiceCommandParser.parse("go back").action)
        assertEquals(AdventureVoiceAction.NEXT_SCREEN, AdventureVoiceCommandParser.parse("next screen").action)
        assertEquals(AdventureVoiceAction.PREVIOUS_SCREEN, AdventureVoiceCommandParser.parse("previous tab").action)
    }

    @Test
    fun selectsActivitySpecificModesAcrossAdventurerDomains() {
        assertEquals(ActivityType.Trekking, AdventureVoiceCommandParser.parse("switch to hiking").activity)
        assertEquals(ActivityType.CarTrips, AdventureVoiceCommandParser.parse("switch to road trip").activity)
        assertEquals(ActivityType.ScubaDiving, AdventureVoiceCommandParser.parse("switch to scuba").activity)
        assertEquals(ActivityType.Camping, AdventureVoiceCommandParser.parse("switch to camping").activity)
        assertEquals(ActivityType.WildlifeExploration, AdventureVoiceCommandParser.parse("switch to wildlife").activity)
    }

    @Test
    fun placeQueriesAreExtractedWithoutInventingAPlace() {
        val command = AdventureVoiceCommandParser.parse("search for Niagara Falls")
        assertEquals(AdventureVoiceAction.SEARCH_PLACE, command.action)
        assertEquals("niagara falls", command.query)
    }

    @Test
    fun deicticPlaceQuestionsRequirePlaceContextInsteadOfGuessing() {
        assertEquals(AdventureVoiceAction.NEARBY_CONTEXT, AdventureVoiceCommandParser.parse("what is that").action)
        assertEquals(AdventureVoiceAction.NEARBY_CONTEXT, AdventureVoiceCommandParser.parse("what's that").action)
    }

    @Test
    fun emergencyPhraseRoutesToSafetyScreenButDoesNotDispatchSos() {
        assertEquals(AdventureVoiceAction.SAFETY_HELP, AdventureVoiceCommandParser.parse("emergency").action)
    }

    @Test
    fun directPushToTalkAcceptsCommandsWithoutWakePhrase() {
        assertEquals(AdventureVoiceAction.OPEN_STORY_STUDIO, AdventureVoiceCommandParser.parse("create a story").action)
    }

    @Test
    fun suppressesLongVehicleGuidanceOnlyForConfirmedLiveMovement() {
        val movingMotorcycle = VoiceMotionSnapshot(
            activityType = ActivityType.Motorcycling,
            isRecording = true,
            isPaused = false,
            hasGpsFix = true,
            isDemo = false,
            speedKmH = 12.0,
        )
        assertTrue(AdventureVoiceSafetyPolicy.isVehicleInMotion(movingMotorcycle))
        assertFalse(AdventureVoiceSafetyPolicy.isVehicleInMotion(movingMotorcycle.copy(isPaused = true)))
        assertFalse(AdventureVoiceSafetyPolicy.isVehicleInMotion(movingMotorcycle.copy(isDemo = true)))
        assertFalse(AdventureVoiceSafetyPolicy.isVehicleInMotion(movingMotorcycle.copy(hasGpsFix = false)))
        assertFalse(AdventureVoiceSafetyPolicy.isVehicleInMotion(movingMotorcycle.copy(speedKmH = 3.0)))
        assertFalse(AdventureVoiceSafetyPolicy.isVehicleInMotion(movingMotorcycle.copy(activityType = ActivityType.Walking)))
    }
}
