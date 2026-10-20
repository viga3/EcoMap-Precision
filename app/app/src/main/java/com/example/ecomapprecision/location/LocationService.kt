package com.example.ecomapprecision.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.example.ecomapprecision.util.DistanceCalculator
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LocationResultData(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val accuracyMeters: Float = 3.0f,
    val isRealFix: Boolean = true
)

class LocationService(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val prefs = context.getSharedPreferences("ecomap_location_cache", Context.MODE_PRIVATE)

    fun getFastCachedLocation(): LocationResultData? {
        val lat = prefs.getString("last_lat", null)?.toDoubleOrNull()
        val lon = prefs.getString("last_lon", null)?.toDoubleOrNull()
        val addr = prefs.getString("last_addr", null)
        if (lat != null && lon != null && lat != 0.0 && lon != 0.0) {
            return LocationResultData(
                latitude = lat,
                longitude = lon,
                address = addr ?: "Posizione recente",
                accuracyMeters = 5.0f,
                isRealFix = true
            )
        }
        return null
    }

    private fun saveLocationToCache(data: LocationResultData) {
        if (data.isRealFix && data.latitude != 0.0 && data.longitude != 0.0) {
            prefs.edit()
                .putString("last_lat", data.latitude.toString())
                .putString("last_lon", data.longitude.toString())
                .putString("last_addr", data.address)
                .apply()
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): LocationResultData? = withContext(Dispatchers.IO) {
        try {
            val location: Location? = suspendCancellableCoroutine { continuation ->
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { loc -> continuation.resume(loc) }
                    .addOnFailureListener { continuation.resume(null) }
            }
            if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                val address = getAddressFromCoordinates(location.latitude, location.longitude)
                val result = LocationResultData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else 5.0f,
                    isRealFix = true
                )
                saveLocationToCache(result)
                result
            } else {
                getFastCachedLocation()
            }
        } catch (e: Exception) {
            getFastCachedLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<LocationResultData> = callbackFlow {
        // Invio immediato della posizione in cache per azzerare qualsiasi ritardo all'avvio
        val cached = getFastCachedLocation()
        if (cached != null) {
            trySend(cached)
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2000L
        ).setMinUpdateDistanceMeters(2.5f).build()

        var lastLat = cached?.latitude ?: 0.0
        var lastLon = cached?.longitude ?: 0.0
        var cachedAddress = cached?.address ?: ""

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                if (location.latitude == 0.0 && location.longitude == 0.0) return

                val dist = DistanceCalculator.calculateDistanceMeters(lastLat, lastLon, location.latitude, location.longitude)

                // Ignora micro-jitter inferiore a 2 metri se abbiamo già una posizione valida
                if (lastLat != 0.0 && lastLon != 0.0 && dist < 2.0) return

                lastLat = location.latitude
                lastLon = location.longitude

                // 1. Emette subito le coordinate GPS al volo: nessun blocco della UI
                val currentAddr = if (cachedAddress.isNotEmpty()) cachedAddress else "Posizione GPS ${String.format(Locale.US, "%.4f, %.4f", location.latitude, location.longitude)}"
                val immediateResult = LocationResultData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = currentAddr,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else 3.0f,
                    isRealFix = true
                )
                trySend(immediateResult)

                // 2. Geocoding asincrono in background su Dispatchers.IO: solo se spostamento significativo (> 30m)
                if (cachedAddress.isEmpty() || dist > 30.0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val addr = getAddressFromCoordinates(location.latitude, location.longitude)
                        if (addr != cachedAddress) {
                            cachedAddress = addr
                            val updated = immediateResult.copy(address = addr)
                            saveLocationToCache(updated)
                            trySend(updated)
                        }
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationResultData = withContext(Dispatchers.IO) {
        val lastKnown = getLastKnownLocation()
        try {
            val location: Location? = suspendCancellableCoroutine { continuation ->
                val cancellationTokenSource = CancellationTokenSource()
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).addOnSuccessListener { loc ->
                    continuation.resume(loc)
                }.addOnFailureListener {
                    continuation.resume(null)
                }
            }

            if (location != null && location.latitude != 0.0 && location.longitude != 0.0) {
                val address = getAddressFromCoordinates(location.latitude, location.longitude)
                val result = LocationResultData(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    accuracyMeters = if (location.hasAccuracy()) location.accuracy else 3.0f,
                    isRealFix = true
                )
                saveLocationToCache(result)
                result
            } else if (lastKnown != null) {
                lastKnown
            } else {
                getFastCachedLocation() ?: LocationResultData(
                    latitude = 0.0,
                    longitude = 0.0,
                    address = "Ricerca posizione GPS...",
                    accuracyMeters = 50.0f,
                    isRealFix = false
                )
            }
        } catch (e: Exception) {
            lastKnown ?: getFastCachedLocation() ?: LocationResultData(
                latitude = 0.0,
                longitude = 0.0,
                address = "Ricerca posizione GPS...",
                accuracyMeters = 50.0f,
                isRealFix = false
            )
        }
    }

    private suspend fun getAddressFromCoordinates(lat: Double, lon: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.ITALIAN)
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: addr.featureName
                val subThoroughfare = addr.subThoroughfare
                val locality = addr.locality ?: ""

                if (!street.isNullOrBlank()) {
                    val numStr = if (!subThoroughfare.isNullOrBlank()) ", $subThoroughfare" else ""
                    val locStr = if (locality.isNotBlank()) ", $locality" else ""
                    "$street$numStr$locStr"
                } else if (locality.isNotBlank()) {
                    "$locality (${String.format(Locale.US, "%.4f, %.4f", lat, lon)})"
                } else {
                    String.format(Locale.US, "Posizione GPS %.4f, %.4f", lat, lon)
                }
            } else {
                String.format(Locale.US, "Posizione GPS %.4f, %.4f", lat, lon)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "Posizione GPS %.4f, %.4f", lat, lon)
        }
    }
}
