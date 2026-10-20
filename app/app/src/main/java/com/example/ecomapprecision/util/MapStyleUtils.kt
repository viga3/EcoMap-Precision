package com.example.ecomapprecision.util

import com.google.android.gms.maps.model.MapStyleOptions

object MapStyleUtils {
    val DARK_MAP_STYLE_JSON = """
        [
          {
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#242f3e"
              }
            ]
          },
          {
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#746855"
              }
            ]
          },
          {
            "elementType": "labels.text.stroke",
            "stylers": [
              {
                "color": "#242f3e"
              }
            ]
          },
          {
            "featureType": "administrative.locality",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#d59563"
              }
            ]
          },
          {
            "featureType": "poi",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#d59563"
              }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#263c3f"
              }
            ]
          },
          {
            "featureType": "poi.park",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#6b9a76"
              }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#38414e"
              }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry.stroke",
            "stylers": [
              {
                "color": "#212a37"
              }
            ]
          },
          {
            "featureType": "road",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#9ca5b3"
              }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#746855"
              }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "geometry.stroke",
            "stylers": [
              {
                "color": "#1f2835"
              }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#f3d19c"
              }
            ]
          },
          {
            "featureType": "transit",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#2f3948"
              }
            ]
          },
          {
            "featureType": "transit.station",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#d59563"
              }
            ]
          },
          {
            "featureType": "water",
            "elementType": "geometry",
            "stylers": [
              {
                "color": "#17263c"
              }
            ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.fill",
            "stylers": [
              {
                "color": "#515c6d"
              }
            ]
          },
          {
            "featureType": "water",
            "elementType": "labels.text.stroke",
            "stylers": [
              {
                "color": "#17263c"
              }
            ]
          }
        ]
    """.trimIndent()

    private val POI_HIDE_RULES = """
        {
          "featureType": "poi",
          "elementType": "labels.icon",
          "stylers": [ { "visibility": "off" } ]
        },
        {
          "featureType": "poi",
          "elementType": "labels.text",
          "stylers": [ { "visibility": "off" } ]
        },
        {
          "featureType": "transit",
          "elementType": "labels.icon",
          "stylers": [ { "visibility": "off" } ]
        }
    """.trimIndent()

    fun getMapStyleOptions(isDarkTheme: Boolean, hidePois: Boolean): MapStyleOptions? {
        if (!isDarkTheme && !hidePois) {
            return null
        }

        val jsonBuilder = StringBuilder("[")
        if (isDarkTheme) {
            val darkRules = DARK_MAP_STYLE_JSON.trim().removeSurrounding("[", "]").trim()
            jsonBuilder.append(darkRules)
            if (hidePois) {
                jsonBuilder.append(",").append(POI_HIDE_RULES)
            }
        } else if (hidePois) {
            jsonBuilder.append(POI_HIDE_RULES)
        }
        jsonBuilder.append("]")

        return MapStyleOptions(jsonBuilder.toString())
    }

    fun getDarkMapStyleOptions(): MapStyleOptions {
        return MapStyleOptions(DARK_MAP_STYLE_JSON)
    }
}
