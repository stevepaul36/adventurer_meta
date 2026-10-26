package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class RideLocation(
    val latitude: Double,
    val longitude: Double,
    val speedKmH: Double,
    val altitudeM: Double,
    val bearingDeg: Float,
    val accuracyM: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val distanceDeltaM: Float = 0f,
    /** Estimated lean angle in degrees. Negative = left, positive = right. */
    val leanAngleDeg: Float = 0f
)

/**
 * LocationManager: Tracks real-time GPS coordinates, speed, altitude, heading,
 * and estimates motorcycle lean angle using accelerometer + gyroscope fusion.
 *
 * Lean-angle notes (improvised foundation):
 * - Uses a simple complementary filter between accelerometer-derived tilt and
 *   integrated gyroscope rate for responsive, low-drift lean estimation.
 * - This is a solid starting point for dual-sport / adventure bikes.
 * - Production refinement should include vibration filtering, bike-specific
 *   calibration, and optional external IMU pairing.
 */
class LocationManager(private val context: Context) : SensorEventListener {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val sensorManager: SensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private val _currentLocation = MutableStateFlow<RideLocation?>(null)
    val currentLocation: StateFlow<RideLocation?> = _currentLocation.asStateFlow()

    private var activeCallback: LocationCallback? = null
    private var lastRawLocation: Location? = null

    // --- Lean angle fusion state ---
    private var currentLeanDeg: Float = 0f
    private var lastGyroTimestampNs: Long = 0L
    private val accel = FloatArray(3)
    private val gyro = FloatArray(3)
    private var hasAccel = false
    private var hasGyro = false

    // Complementary filter coefficient (higher = trust gyro more for short-term)
    private val alpha = 0.98f

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

    private fun createRideLocationRequest(): LocationRequest {
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).apply {
            setMinUpdateIntervalMillis(500L)
            setMinUpdateDistanceMeters(0.5f)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(false)
        }.build()
    }

    /** Start listening to accelerometer + gyroscope for lean estimation. */
    fun startSensorFusion() {
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensorFusion() {
        sensorManager.unregisterListener(this)
        hasAccel = false
        hasGyro = false
        lastGyroTimestampNs = 0L
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accel, 0, 3)
                hasAccel = true
            }
            Sensor.TYPE_GYROSCOPE -> {
                System.arraycopy(event.values, 0, gyro, 0, 3)
                hasGyro = true
                updateLeanFromSensors(event.timestamp)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op for now; could gate fusion quality later
    }

    /**
     * Complementary filter:
     * - Accelerometer gives absolute tilt (noisy under vibration).
     * - Gyroscope gives high-frequency rate (drifts over time).
     * Result is a responsive lean angle suitable for a motorcycle HUD.
     */
    private fun updateLeanFromSensors(timestampNs: Long) {
        if (!hasAccel || !hasGyro) return

        // Accelerometer-based roll approximation (simplified for motorcycle lateral lean)
        // Using atan2 of lateral vs vertical components.
        val accelLean = Math.toDegrees(
            atan2(accel[0].toDouble(), sqrt((accel[1] * accel[1] + accel[2] * accel[2]).toDouble()))
        ).toFloat()

        val dt = if (lastGyroTimestampNs > 0L) {
            (timestampNs - lastGyroTimestampNs) / 1_000_000_000f
        } else {
            0f
        }
        lastGyroTimestampNs = timestampNs

        // Integrate gyro rate (rad/s → deg). Sign convention: positive = right lean.
        val gyroRateDeg = Math.toDegrees(gyro[1].toDouble()).toFloat() // pitch/roll axis depending on mounting
        val gyroLean = currentLeanDeg + gyroRateDeg * dt

        // Complementary filter
        currentLeanDeg = alpha * gyroLean + (1f - alpha) * accelLean

        // Clamp to realistic motorcycle lean range
        currentLeanDeg = currentLeanDeg.coerceIn(-55f, 55f)
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<RideLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Location permissions not granted"))
            return@callbackFlow
        }

        startSensorFusion()

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
            .addOnFailureListener { close(it) }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
            stopSensorFusion()
        }
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        if (!hasLocationPermission()) return
        stopTracking()

        startSensorFusion()

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

    fun stopTracking() {
        activeCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            activeCallback = null
        }
        lastRawLocation = null
        stopSensorFusion()
    }

    private fun convertToRideLocation(current: Location, previous: Location?): RideLocation {
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
            distanceDeltaM = deltaM,
            leanAngleDeg = currentLeanDeg
        )
    }

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

    /** Current estimated lean angle in degrees (for HUD or debug). */
    fun getCurrentLeanAngleDeg(): Float = currentLeanDeg
}
