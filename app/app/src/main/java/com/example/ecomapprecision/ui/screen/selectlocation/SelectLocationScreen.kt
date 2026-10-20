package com.example.ecomapprecision.ui.screen.selectlocation

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.ecomapprecision.location.LocationResultData
import com.example.ecomapprecision.ui.components.TopAppBarEcoMap
import com.example.ecomapprecision.util.GeocodingUtils
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SelectLocationScreen(
    initialLocation: LocationResultData,
    onNavigateBack: () -> Unit,
    onLocationConfirmed: (LocationResultData) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var selectedLat by remember { mutableStateOf(initialLocation.latitude) }
    var selectedLon by remember { mutableStateOf(initialLocation.longitude) }
    var selectedAddress by remember { mutableStateOf(initialLocation.address) }
    var searchQuery by remember { mutableStateOf("") }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(selectedLat, selectedLon), 17f)
    }

    // Function to update reverse geocoded address when user selects a new point
    fun updateLocationFromLatLng(lat: Double, lng: Double) {
        selectedLat = lat
        selectedLon = lng
        coroutineScope.launch(Dispatchers.IO) {
            val addr = try {
                val geocoder = android.location.Geocoder(context, java.util.Locale.ITALIAN)
                val list = geocoder.getFromLocation(lat, lng, 1)
                if (!list.isNullOrEmpty()) {
                    val a = list[0]
                    val street = a.thoroughfare ?: a.featureName ?: "Via"
                    val num = a.subThoroughfare ?: ""
                    val city = a.locality ?: "Piombino"
                    if (num.isNotBlank()) "$street, $num, $city" else "$street, $city"
                } else {
                    String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)
                }
            } catch (e: Exception) {
                String.format(java.util.Locale.US, "%.5f, %.5f", lat, lng)
            }
            withContext(Dispatchers.Main) {
                selectedAddress = addr
            }
        }
    }

    fun performSearch() {
        keyboardController?.hide()
        if (searchQuery.isBlank()) return

        coroutineScope.launch(Dispatchers.IO) {
            val coords = GeocodingUtils.searchAddress(
                context = context,
                query = searchQuery,
                currentLat = selectedLat,
                currentLon = selectedLon,
                currentAddress = selectedAddress
            )
            withContext(Dispatchers.Main) {
                if (coords != null) {
                    selectedLat = coords.first
                    selectedLon = coords.second
                    selectedAddress = searchQuery.trim()
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(coords.first, coords.second), 17f)
                    )
                } else {
                    Toast.makeText(context, "Indirizzo non trovato", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val themeMode by com.example.ecomapprecision.theme.ThemeManager.themeMode.collectAsState()
    val hideMapPois by com.example.ecomapprecision.util.MapSettingsManager.hideMapPois.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        com.example.ecomapprecision.theme.AppThemeMode.LIGHT -> false
        com.example.ecomapprecision.theme.AppThemeMode.DARK -> true
        else -> isSystemDark
    }

    val mapProperties = remember(hasLocationPermission, isDarkTheme, hideMapPois) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapType = MapType.NORMAL,
            mapStyleOptions = com.example.ecomapprecision.util.MapStyleUtils.getMapStyleOptions(isDarkTheme, hideMapPois)
        )
    }

    val postazioni by remember(context) {
        com.example.ecomapprecision.data.db.EcoMapDatabase.getDatabase(context).postazioneDao().getAllPostazioni()
    }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBarEcoMap(
                title = "Seleziona Posizione",
                showBack = true,
                onBackClick = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Location info box
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "POSIZIONE SELEZIONATA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = selectedAddress,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = GeocodingUtils.formatCoordinates(selectedLat, selectedLon),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val updated = LocationResultData(
                                latitude = selectedLat,
                                longitude = selectedLon,
                                address = selectedAddress
                            )
                            onLocationConfirmed(updated)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Conferma",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "OK / Conferma Posizione",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Map
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true
                ),
                onMapClick = { latLng ->
                    updateLocationFromLatLng(latLng.latitude, latLng.longitude)
                }
            ) {
                // Existing postazioni markers
                postazioni.forEach { post ->
                    val hue = when {
                        post.numMultimateriale > 0 -> BitmapDescriptorFactory.HUE_YELLOW
                        post.numVetro > 0 -> BitmapDescriptorFactory.HUE_GREEN
                        post.numCarta > 0 -> BitmapDescriptorFactory.HUE_AZURE
                        post.numOrganico > 0 -> BitmapDescriptorFactory.HUE_ORANGE
                        else -> BitmapDescriptorFactory.HUE_AZURE
                    }
                    Marker(
                        state = MarkerState(position = LatLng(post.latitudine, post.longitudine)),
                        title = post.nome,
                        snippet = post.indirizzo,
                        icon = BitmapDescriptorFactory.defaultMarker(hue)
                    )
                }

                // Marker at selected point
                Marker(
                    state = MarkerState(position = LatLng(selectedLat, selectedLon)),
                    title = "Punto Selezionato",
                    snippet = selectedAddress,
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    draggable = true
                )
            }

            // Top Search Bar Overlay
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(14.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cerca via o indirizzo sulla mappa...", style = MaterialTheme.typography.bodyMedium) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { performSearch() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    IconButton(onClick = { performSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Cerca",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Recenter GPS FAB
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(LatLng(selectedLat, selectedLon), 17f)
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Centra sul punto",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
