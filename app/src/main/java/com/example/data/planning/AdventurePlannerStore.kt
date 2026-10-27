package com.example.data.planning

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * User-created trip drafts and bookmarks are device-local by design.
 * This database does not store tile imagery or claim to be a cloud-synced account store.
 */
@Entity(tableName = "adventure_trip_plans")
data class AdventureTripPlanEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val destination: String,
    val activityTypesCsv: String,
    val startDateLabel: String,
    val durationDays: Int,
    val budgetCad: Double?,
    val itineraryStopsJson: String,
    val prepChecklistJson: String,
    val checkInEnabled: Boolean,
    val checkInIntervalMinutes: Int,
    val expectedReturnLabel: String,
    val trustedContactLabel: String,
    val lastCheckInAtMillis: Long? = null,
    val visibility: String = "PRIVATE",
    val status: String = "DRAFT",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "adventure_saved_places")
data class AdventurePlaceBookmarkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val activityType: String,
    val destinationContext: String,
    val description: String,
    val sourceNotice: String = "Planning idea only — not live-verified",
    val savedAtMillis: Long = System.currentTimeMillis(),
)

@Entity(tableName = "adventure_creator_drafts")
data class AdventureCreatorDraftEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val kind: String,
    val title: String,
    val destination: String,
    val activityType: String,
    val description: String,
    val proposedPriceCad: Double? = null,
    val dateLabel: String = "",
    val status: String = "LOCAL_DRAFT",
    val createdAtMillis: Long = System.currentTimeMillis(),
)

@Dao
interface AdventurePlannerDao {
    @Query("SELECT * FROM adventure_trip_plans ORDER BY updatedAtMillis DESC")
    fun observeTrips(): Flow<List<AdventureTripPlanEntity>>

    @Query("SELECT * FROM adventure_trip_plans WHERE id = :id LIMIT 1")
    suspend fun getTrip(id: String): AdventureTripPlanEntity?

    @Upsert
    suspend fun upsertTrip(trip: AdventureTripPlanEntity)

    @Query("DELETE FROM adventure_trip_plans WHERE id = :id")
    suspend fun deleteTrip(id: String)

    @Query("SELECT * FROM adventure_saved_places ORDER BY savedAtMillis DESC")
    fun observeSavedPlaces(): Flow<List<AdventurePlaceBookmarkEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM adventure_saved_places WHERE id = :id)")
    suspend fun isPlaceSaved(id: String): Boolean

    @Upsert
    suspend fun upsertSavedPlace(place: AdventurePlaceBookmarkEntity)

    @Query("DELETE FROM adventure_saved_places WHERE id = :id")
    suspend fun deleteSavedPlace(id: String)

    @Query("SELECT * FROM adventure_creator_drafts ORDER BY createdAtMillis DESC")
    fun observeCreatorDrafts(): Flow<List<AdventureCreatorDraftEntity>>

    @Upsert
    suspend fun upsertCreatorDraft(draft: AdventureCreatorDraftEntity)

    @Query("DELETE FROM adventure_creator_drafts WHERE id = :id")
    suspend fun deleteCreatorDraft(id: String)
}

@Database(
    entities = [
        AdventureTripPlanEntity::class,
        AdventurePlaceBookmarkEntity::class,
        AdventureCreatorDraftEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AdventurePlannerDatabase : RoomDatabase() {
    abstract fun adventurePlannerDao(): AdventurePlannerDao

    companion object {
        @Volatile private var instance: AdventurePlannerDatabase? = null

        fun getInstance(context: Context): AdventurePlannerDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AdventurePlannerDatabase::class.java,
                    "adventurer-planner.db",
                ).build().also { instance = it }
            }
    }
}

class AdventurePlannerRepository(context: Context) {
    private val dao = AdventurePlannerDatabase.getInstance(context).adventurePlannerDao()

    fun observeTrips(): Flow<List<AdventureTripPlanEntity>> = dao.observeTrips()
    fun observeSavedPlaces(): Flow<List<AdventurePlaceBookmarkEntity>> = dao.observeSavedPlaces()
    fun observeCreatorDrafts(): Flow<List<AdventureCreatorDraftEntity>> = dao.observeCreatorDrafts()

    suspend fun saveTrip(trip: AdventureTripPlanEntity) = dao.upsertTrip(trip)
    suspend fun deleteTrip(id: String) = dao.deleteTrip(id)

    suspend fun recordCheckIn(id: String, atMillis: Long = System.currentTimeMillis()) {
        val existing = dao.getTrip(id) ?: return
        dao.upsertTrip(existing.copy(lastCheckInAtMillis = atMillis, updatedAtMillis = atMillis))
    }

    suspend fun savePlace(place: AdventurePlaceBookmarkEntity) = dao.upsertSavedPlace(place)
    suspend fun removePlace(id: String) = dao.deleteSavedPlace(id)
    suspend fun isPlaceSaved(id: String): Boolean = dao.isPlaceSaved(id)

    suspend fun saveCreatorDraft(draft: AdventureCreatorDraftEntity) = dao.upsertCreatorDraft(draft)
    suspend fun deleteCreatorDraft(id: String) = dao.deleteCreatorDraft(id)
}
