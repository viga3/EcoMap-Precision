package com.example.ecomapprecision.util

import android.content.Context
import android.location.Geocoder
import java.util.Locale

object GeocodingUtils {
    fun formatCoordinates(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return String.format(Locale.US, "GPS: %.4f° %s, %.4f° %s", Math.abs(lat), latDir, Math.abs(lon), lonDir)
    }

    fun searchAddress(
        context: Context,
        query: String,
        currentLat: Double? = null,
        currentLon: Double? = null,
        currentAddress: String? = null
    ): Pair<Double, Double>? {
        return try {
            val geocoder = Geocoder(context, Locale.ITALIAN)
            val trimmedQuery = query.trim()

            // 1. Try searching with bounded box if location is available
            if (currentLat != null && currentLon != null) {
                val delta = 0.15
                val minLat = (currentLat - delta).coerceAtLeast(-90.0)
                val maxLat = (currentLat + delta).coerceAtMost(90.0)
                val minLon = (currentLon - delta).coerceAtLeast(-180.0)
                val maxLon = (currentLon + delta).coerceAtMost(180.0)

                val boundedAddresses = geocoder.getFromLocationName(trimmedQuery, 5, minLat, minLon, maxLat, maxLon)
                if (!boundedAddresses.isNullOrEmpty()) {
                    return Pair(boundedAddresses[0].latitude, boundedAddresses[0].longitude)
                }
            }

            // 2. Try appending current city from currentAddress if available
            val city = currentAddress?.split(",")?.lastOrNull()?.trim()
            if (!city.isNullOrBlank() && !trimmedQuery.lowercase().contains(city.lowercase())) {
                val queryWithCity = "$trimmedQuery, $city"
                val cityAddresses = geocoder.getFromLocationName(queryWithCity, 1)
                if (!cityAddresses.isNullOrEmpty()) {
                    return Pair(cityAddresses[0].latitude, cityAddresses[0].longitude)
                }
            }

            // 3. Fallback standard search
            val addresses = geocoder.getFromLocationName(trimmedQuery, 1)
            if (!addresses.isNullOrEmpty()) {
                Pair(addresses[0].latitude, addresses[0].longitude)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun reverseGeocode(context: Context, lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.ITALIAN)
            val list = geocoder.getFromLocation(lat, lon, 1)
            if (!list.isNullOrEmpty()) {
                val a = list[0]
                val street = a.thoroughfare ?: a.featureName ?: "Via"
                val num = a.subThoroughfare ?: ""
                val city = a.locality ?: "Piombino"
                if (num.isNotBlank()) "$street, $num, $city" else "$street, $city"
            } else {
                String.format(Locale.US, "%.4f, %.4f", lat, lon)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "%.4f, %.4f", lat, lon)
        }
    }
}
