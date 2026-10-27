package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.analytics.AdventureFunnelAnalytics
import com.example.data.model.ActivityType
import com.example.data.planning.*
import com.example.data.story.AdventureStoryTemplates
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
class AdventurePlannerTest {

    private lateinit var db: AdventurePlannerDatabase
    private lateinit var dao: AdventurePlannerDao
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AdventurePlannerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.adventurePlannerDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testTripPlanPersistenceAndCheckIn() = runBlocking {
        val trip = AdventureTripPlanEntity(
            id = "test-trip-1",
            title = "West Coast Explorer",
            destination = "Tofino, BC",
            activityTypesCsv = "${ActivityType.KayakingPaddling.name},${ActivityType.Camping.name}",
            startDateLabel = "July 12",
            durationDays = 4,
            budgetCad = 850.0,
            itineraryStopsJson = "[]",
            prepChecklistJson = "[]",
            checkInEnabled = true,
            checkInIntervalMinutes = 60,
            expectedReturnLabel = "6:00 PM Sunday",
            trustedContactLabel = "Sarah",
        )
        dao.upsertTrip(trip)

        val trips = dao.observeTrips().first()
        assertEquals(1, trips.size)
        assertEquals("West Coast Explorer", trips[0].title)
        assertNull(trips[0].lastCheckInAtMillis)

        val retrieved = dao.getTrip("test-trip-1")
        assertNotNull(retrieved)

        val checkInTime = System.currentTimeMillis()
        dao.upsertTrip(retrieved!!.copy(lastCheckInAtMillis = checkInTime))
        val updated = dao.getTrip("test-trip-1")
        assertEquals(checkInTime, updated?.lastCheckInAtMillis)

        dao.deleteTrip("test-trip-1")
        assertTrue(dao.observeTrips().first().isEmpty())
    }

    @Test
    fun testPlaceBookmarks() = runBlocking {
        val place = AdventurePlaceBookmarkEntity(
            id = "poi-1",
            title = "Cox Bay Lookout",
            category = "Viewpoint",
            activityType = ActivityType.Trekking.name,
            destinationContext = "Tofino",
            description = "Steep muddy climb to ocean vista",
        )
        assertFalse(dao.isPlaceSaved("poi-1"))

        dao.upsertSavedPlace(place)
        assertTrue(dao.isPlaceSaved("poi-1"))

        val saved = dao.observeSavedPlaces().first()
        assertEquals(1, saved.size)
        assertEquals("Cox Bay Lookout", saved[0].title)

        dao.deleteSavedPlace("poi-1")
        assertFalse(dao.isPlaceSaved("poi-1"))
    }

    @Test
    fun testCreatorDrafts() = runBlocking {
        val draft = AdventureCreatorDraftEntity(
            id = "guide-1",
            kind = "GUIDE",
            title = "Hidden Coves of Clayoquot",
            destination = "Vancouver Island",
            activityType = "Kayaking",
            description = "Detailed tidal planning notes",
            proposedPriceCad = 15.0,
        )
        dao.upsertCreatorDraft(draft)

        val drafts = dao.observeCreatorDrafts().first()
        assertEquals(1, drafts.size)
        assertEquals("Hidden Coves of Clayoquot", drafts[0].title)

        dao.deleteCreatorDraft("guide-1")
        assertTrue(dao.observeCreatorDrafts().first().isEmpty())
    }

    @Test
    fun testFunnelAnalyticsLocalLogging() {
        AdventureFunnelAnalytics.log(context, "trip_plan_created", mapOf("activity_count" to 2))
        val snapshot = AdventureFunnelAnalytics.localSnapshot(context)
        assertTrue((snapshot["trip_plan_created"] ?: 0) >= 1)
    }

    @Test
    fun testStoryTemplateFilterByActivity() {
        val diveTemplates = AdventureStoryTemplates.forActivity(ActivityType.ScubaDiving)
        assertTrue(diveTemplates.any { it.id == "water_log" })
        assertTrue(diveTemplates.none { it.id == "road_trip" })

        val bikeTemplates = AdventureStoryTemplates.forActivity(ActivityType.Motorcycling)
        assertTrue(bikeTemplates.any { it.id == "road_trip" })
        assertTrue(bikeTemplates.any { it.id == "trail_journal" })
    }
}
