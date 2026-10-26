package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

data class RideLocation(
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Double,
    val altitudeM: Double,
    val bearingDeg: Float,
    val accuracyM: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val distanceDeltaM: Float = 0f
)

/**
 * LocationManager: Tracks real-time GPS coordinates, speed, altitude, and heading
 * for motorcycle rides using Google Play Services FusedLocationProviderClient.
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val _currentLocation = MutableStateFlow<RideLocation?>(null)
    val currentLocation: StateFlow<RideLocation?> = _currentLocation.asStateFlow()

    private var activeCallback: LocationCallback? = null
    private var lastRawLocation: Location? = null

    /**
     * Check if location permissions are granted.
     */
    fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    /**
     * Builds a high-accuracy LocationRequest optimized for motorcycle rides
     * (1-second update interval with 0.5m minimum displacement).
     */
    private fun createRideLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).apply {
            setMinUpdateIntervalMillis(500L)
            setMinUpdateDistanceMeters(0.5f)
            setGranularity(Granularity.GRANULARITY_FINE)
            setWaitForAccurateLocation(false)
        }.build()
    }

    /**
     * Cold flow that emits real-time RideLocation updates.
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<RideLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permissions not granted"))
            return@callbackFlow
        }

        var prevLocation: Location? = null

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val rideLoc = convertToRideLocation(location, prevLocation)
                    prevLocation = location
                    _currentLocation.value = rideLoc
                    trySend(rideLoc)
                }
            }
        }

        val request = createRideLocationRequest()
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Starts active tracking on this manager instance, updating [currentLocation].
     */
    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasLocationPermission()) return
        stopTracking()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val rideLoc = convertToRideLocation(location, lastRawLocation)
                    lastRawLocation = location
                    _currentLocation.value = rideLoc
                }
            }
        }
        activeCallback = callback
        val request = createRideLocationRequest()
        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    /**
     * Stops tracking and cleans up callbacks.
     */
    fun stopTracking() {
        activeCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            activeCallback = null
        }
        lastRawLocation = null
    }

    /**
     * Converts an Android Location into a domain RideLocation with speed in km/h.
     */
    private fun convertToRideLocation(current: Location, previous: Location?): RideLocation {
        // Calculate speed in km/h. If provider reports speed, convert m/s -> km/h.
        val speedKmH = if (current.hasSpeed() && current.speed >= 0f) {
            (current.speed * 3.6).coerceAtLeast(0.0)
        } else if (previous != null) {
            val dist = previous.distanceTo(current)
            val timeDiffSec = (current.time - previous.time) / 1000.0
            if (timeDiffSec > 0.0) {
                ((dist / timeDiffSec) * 3.6).coerceAtLeast(0.0)
            } else 0.0
        } else {
            0.0
        }

        val deltaM = previous?.distanceTo(current) ?: 0f

        return RideLocation(
            latitude = current.latitude,
            longitude = current.longitude,
            speedKmH = (speedKmH * 10.0).roundToInt() / 10.0,
            altitudeM = if (current.hasAltitude()) (current.altitude * 10.0).roundToInt() / 10.0 else 0.0,
            bearingDeg = if (current.hasBearing()) current.bearing else 0f,
            accuracyM = if (current.hasAccuracy()) current.accuracy else 0f,
            timestamp = current.time,
            distanceDeltaM = deltaM
        )
    }

    /**
     * Fetches the last known location if available.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(onResult: (RideLocation?) -> Unit) {
        if (!hasLocationPermission()) {
            onResult(null)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { loc ->
                if (loc != null) {
                    onResult(convertToRideLocation(loc, null))
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}
