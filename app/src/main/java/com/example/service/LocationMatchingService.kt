package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

class LocationMatchingService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val _currentLocation = MutableStateFlow<UserLocation?>(null)
    val currentLocation: StateFlow<UserLocation?> = _currentLocation.asStateFlow()

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): UserLocation? {
        return try {
            val cancellationTokenSource = CancellationTokenSource()
            val location: Location? = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).await()

            if (location != null) {
                val userLocation = UserLocation(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracyMeters = location.accuracy,
                    timestamp = location.time
                )
                _currentLocation.value = userLocation
                userLocation
            } else {
                _currentLocation.value
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _currentLocation.value
        }
    }

    companion object {
        private const val EARTH_RADIUS_MILES = 3958.8

        /**
         * Calculate distance between two coordinates in miles using the Haversine formula.
         */
        fun calculateDistanceMiles(
            startLat: Double,
            startLng: Double,
            endLat: Double,
            endLng: Double
        ): Double {
            val dLat = Math.toRadians(endLat - startLat)
            val dLng = Math.toRadians(endLng - startLng)
            val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                    Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat)) *
                    Math.sin(dLng / 2) * Math.sin(dLng / 2)
            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
            return EARTH_RADIUS_MILES * c
        }

        /**
         * Check if candidate profile location is within maximum matching radius (in miles).
         */
        fun isWithinMatchingRadius(
            userLat: Double,
            userLng: Double,
            matchLat: Double,
            matchLng: Double,
            maxRadiusMiles: Double
        ): Boolean {
            val distance = calculateDistanceMiles(userLat, userLng, matchLat, matchLng)
            return distance <= maxRadiusMiles
        }

        /**
         * Filter and rank potential AdventHearts matches based on geographic distance.
         */
        fun <T> rankMatchesByProximity(
            userLat: Double,
            userLng: Double,
            candidates: List<T>,
            maxRadiusMiles: Double = 50.0,
            coordsExtractor: (T) -> Pair<Double, Double>
        ): List<Pair<T, Double>> {
            return candidates.mapNotNull { candidate ->
                val (lat, lng) = coordsExtractor(candidate)
                val distance = calculateDistanceMiles(userLat, userLng, lat, lng)
                if (distance <= maxRadiusMiles) {
                    candidate to distance
                } else {
                    null
                }
            }.sortedBy { it.second }
        }
    }
}
