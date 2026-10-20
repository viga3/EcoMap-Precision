package com.example.ecomapprecision.ui.screen.map

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import com.example.ecomapprecision.location.LocationResultData
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.theme.WasteAbiti
import com.example.ecomapprecision.theme.WasteBlu
import com.example.ecomapprecision.theme.WasteGiallo
import com.example.ecomapprecision.theme.WasteGrigio
import com.example.ecomapprecision.theme.WasteMarrone
import com.example.ecomapprecision.theme.WasteRaee
import com.example.ecomapprecision.theme.WasteVerde
import com.example.ecomapprecision.ui.components.BottomNavBar
import com.example.ecomapprecision.ui.components.FilterChipItem
import com.example.ecomapprecision.ui.components.FilterChipsRow
import com.example.ecomapprecision.ui.components.ScreenTab
import com.example.ecomapprecision.ui.components.TopAppBarEcoMap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import androidx.compose.runtime.key
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import androidx.compose.material.icons.filled.AssignmentInd
import androidx.compose.material.icons.filled.Edit
import com.example.ecomapprecision.ui.components.EditPostazioneDialog
import com.example.ecomapprecision.util.DistanceCalculator

@Composable
fun MapScreen(
    onNavigateTab: (ScreenTab) -> Unit,
    onNavigateToAddPostazione: (lat: Double, lng: Double) -> Unit,
    onNavigateToSegnalazione: (lat: Double?, lng: Double?) -> Unit,
    onNavigateToSegnalazioniRicevute: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: MapViewModel = viewModel()
) {
    val postazioni by viewModel.filteredPostazioni.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isAutoFollowing by viewModel.isAutoFollowing.collectAsState()
    val selectedPostazione by viewModel.selectedPostazione.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var editingPostazione by remember { mutableStateOf<Postazione?>(null) }
    var repositioningPostazione by remember { mutableStateOf<Postazione?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    var isProgrammaticAnimation by remember { mutableStateOf(false) }

    val cameraPositionState = rememberCameraPositionState {
        val saved = viewModel.lastCameraPosition
        if (saved != null) {
            position = saved
        } else {
            val initialLat = if (currentLocation.latitude != 0.0) currentLocation.latitude else 42.9248
            val initialLng = if (currentLocation.longitude != 0.0) currentLocation.longitude else 10.5342
            position = CameraPosition.fromLatLngZoom(LatLng(initialLat, initialLng), 17f)
        }
    }

    // Ascolta il movimento della fotocamera:
    // Non appena l'utente muove la mappa con il dito (o gesture), l'inseguimento si disabilita all'istante
    LaunchedEffect(cameraPositionState) {
        snapshotFlow {
            cameraPositionState.isMoving to cameraPositionState.cameraMoveStartedReason
        }.collect { (isMoving, reason) ->
            if (isMoving) {
                // Se il movimento è iniziato da un gesto dell'utente OPPURE se non è una nostra animazione programmata:
                if (!isProgrammaticAnimation || reason == CameraMoveStartedReason.GESTURE) {
                    if (isAutoFollowing) {
                        viewModel.setAutoFollowing(false)
                    }
                }
            } else {
                viewModel.lastCameraPosition = cameraPositionState.position
            }
        }
    }

    // Salva la posizione quando si esce dalla schermata della mappa
    DisposableEffect(Unit) {
        onDispose {
            viewModel.lastCameraPosition = cameraPositionState.position
        }
    }

    // Centra all'avvio sulle coordinate reali e segue l'utente SOLO se isAutoFollowing è attivo
    LaunchedEffect(currentLocation, isAutoFollowing) {
        if (currentLocation.isRealFix && currentLocation.latitude != 0.0 && currentLocation.longitude != 0.0) {
            val target = LatLng(currentLocation.latitude, currentLocation.longitude)
            if (!viewModel.hasInitiallyCentered) {
                // Primo aggancio all'avvio: scatto immediato (move) senza animazione scenica dal mondo
                isProgrammaticAnimation = true
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(target, 17f))
                isProgrammaticAnimation = false
                viewModel.hasInitiallyCentered = true
            } else if (isAutoFollowing && !cameraPositionState.isMoving) {
                // Inseguimento in tempo reale quando ci si sposta in macchina:
                // anima solo se c'è un effettivo spostamento >= 4.0m
                val curTarget = cameraPositionState.position.target
                val distMeters = DistanceCalculator.calculateDistanceMeters(
                    curTarget.latitude, curTarget.longitude,
                    target.latitude, target.longitude
                )
                if (distMeters >= 4.0) {
                    isProgrammaticAnimation = true
                    try {
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLng(target),
                            durationMs = 400
                        )
                    } finally {
                        isProgrammaticAnimation = false
                    }
                }
            }
        }
    }

    val filterItems = listOf(
        FilterChipItem("giallo", "Multimateriale", WasteGiallo),
        FilterChipItem("vetro", "Vetro", WasteVerde),
        FilterChipItem("carta", "Carta", WasteBlu),
        FilterChipItem("organico", "Organico", WasteMarrone),
        FilterChipItem("indifferenziato", "Indifferenziato", WasteGrigio),
        FilterChipItem("raee", "RAEE", WasteRaee),
        FilterChipItem("abiti", "Abiti Usati", WasteAbiti)
    )

    fun performSearch() {
        keyboardController?.hide()
        viewModel.searchAddress(searchQuery) { success ->
            if (success) {
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(currentLocation.latitude, currentLocation.longitude),
                            17f
                        )
                    )
                }
            } else {
                Toast.makeText(context, "Indirizzo non trovato", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val themeMode by com.example.ecomapprecision.theme.ThemeManager.themeMode.collectAsState()
    val hideMapPois by com.example.ecomapprecision.util.MapSettingsManager.hideMapPois.collectAsState()
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (themeMode) {
        com.example.ecomapprecision.theme.AppThemeMode.LIGHT -> false
        com.example.ecomapprecision.theme.AppThemeMode.DARK -> true
        com.example.ecomapprecision.theme.AppThemeMode.SYSTEM -> isSystemDark
    }

    val mapProperties = remember(hasLocationPermission, isDarkTheme, hideMapPois) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission,
            mapType = MapType.NORMAL,
            mapStyleOptions = com.example.ecomapprecision.util.MapStyleUtils.getMapStyleOptions(isDarkTheme, hideMapPois)
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true
        )
    }

    Scaffold(
        topBar = {
            TopAppBarEcoMap(title = "EcoMap", onMenuClick = onMenuClick)
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = ScreenTab.Map.route,
                onTabSelected = onNavigateTab
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Native Google Map Layer
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = mapUiSettings,
                onMapClick = { latLng ->
                    repositioningPostazione?.let { targetPost ->
                        coroutineScope.launch {
                            val newAddress = withContext(Dispatchers.IO) {
                                try {
                                    val geocoder = android.location.Geocoder(context, java.util.Locale.ITALIAN)
                                    val addrs = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                                    if (!addrs.isNullOrEmpty()) {
                                        val a = addrs[0]
                                        val street = a.thoroughfare ?: a.featureName ?: "Corso Vittorio Emanuele"
                                        val num = a.subThoroughfare ?: "1"
                                        val city = a.locality ?: "Piombino"
                                        "$street, $num, $city"
                                    } else {
                                        String.format(java.util.Locale.US, "%.4f° N, %.4f° E", latLng.latitude, latLng.longitude)
                                    }
                                } catch (e: Exception) {
                                    String.format(java.util.Locale.US, "%.4f° N, %.4f° E", latLng.latitude, latLng.longitude)
                                }
                            }
                            val updated = targetPost.copy(
                                latitudine = latLng.latitude,
                                longitudine = latLng.longitude,
                                indirizzo = newAddress,
                                dataUltimoAggiornamento = System.currentTimeMillis()
                            )
                            viewModel.updatePostazione(updated)
                            repositioningPostazione = null
                            Toast.makeText(context, "Postazione spostata nella nuova posizione!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            ) {
                // Pre-cached marker icons per massime prestazioni
                val iconGiallo = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW) }
                val iconVerde = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN) }
                val iconBlu = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE) }
                val iconArancio = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE) }
                val iconRosso = remember { BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) }

                // Filtered Postazioni Markers
                postazioni.forEach { postazione ->
                    key(postazione.id) {
                        Marker(
                            state = rememberMarkerState(
                                key = "post_${postazione.id}_${postazione.latitudine}_${postazione.longitudine}",
                                position = LatLng(postazione.latitudine, postazione.longitudine)
                            ),
                            title = postazione.nome,
                            snippet = "${postazione.indirizzo} (${postazione.totaleBins()} bins)",
                            icon = when {
                                postazione.numMultimateriale > 0 -> iconGiallo
                                postazione.numVetro > 0 -> iconVerde
                                postazione.numCarta > 0 -> iconBlu
                                postazione.numOrganico > 0 -> iconArancio
                                else -> iconRosso
                            },
                            onClick = {
                                viewModel.onPostazioneClick(postazione)
                                true
                            }
                        )
                    }
                }
            }

            // Top Header Control Panel (Search Bar + Category Filters + Action Buttons Row)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            ) {
                // 1. Search Bar Input
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
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
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Posizione",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cerca indirizzo vicino", style = MaterialTheme.typography.bodyMedium) },
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

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons Row (+ Rilevamento & SEGNALA) directly below search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Green Action Button: Rilevamento
                    Button(
                        onClick = {
                            val target = cameraPositionState.position.target
                            onNavigateToAddPostazione(target.latitude, target.longitude)
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .height(46.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Nuovo Rilevamento",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Rilevamento",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Red Action Button: SEGNALA
                    Button(
                        onClick = {
                            if (currentLocation.isRealFix && currentLocation.latitude != 0.0) {
                                onNavigateToSegnalazione(currentLocation.latitude, currentLocation.longitude)
                            } else {
                                onNavigateToSegnalazione(null, null)
                            }
                        },
                        modifier = Modifier
                            .weight(0.9f)
                            .height(46.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Segnala",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEGNALA",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Real-Time Location Card Overlay
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            viewModel.setAutoFollowing(true)
                            viewModel.refreshLocation()
                            if (currentLocation.isRealFix && currentLocation.latitude != 0.0) {
                                coroutineScope.launch {
                                    isProgrammaticAnimation = true
                                    try {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(currentLocation.latitude, currentLocation.longitude),
                                                17f
                                            ),
                                            durationMs = 600
                                        )
                                    } finally {
                                        isProgrammaticAnimation = false
                                    }
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "GPS Address",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isAutoFollowing) "INSEGUIMENTO GPS ATTIVO" else "GPS ATTIVO • TOCCA PER SEGUIRE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isAutoFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = currentLocation.address,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Floating Action Buttons Column (Bottom Right)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Floating GPS Recenter FAB ("Mirino")
                FloatingActionButton(
                    onClick = {
                        viewModel.setAutoFollowing(true)
                        viewModel.refreshLocation()
                        if (currentLocation.isRealFix && currentLocation.latitude != 0.0) {
                            coroutineScope.launch {
                                isProgrammaticAnimation = true
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(currentLocation.latitude, currentLocation.longitude),
                                            17f
                                        ),
                                        durationMs = 600
                                    )
                                } finally {
                                    isProgrammaticAnimation = false
                                }
                            }
                        }
                        Toast.makeText(context, "Inseguimento posizione attivato", Toast.LENGTH_SHORT).show()
                    },
                    shape = CircleShape,
                    containerColor = if (isAutoFollowing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = if (isAutoFollowing) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Centra su di me",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Postazione Details Card Popup if selected
            selectedPostazione?.let { postazione ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${postazione.indirizzo} (${postazione.nome})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                                maxLines = 2
                            )
                            IconButton(
                                onClick = { viewModel.dismissPostazioneDetails() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Chiudi",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Bins: Carta (${postazione.numCarta}), Vetro (${postazione.numVetro}), Multimateriale (${postazione.numMultimateriale}), Organico (${postazione.numOrganico}), Indifferenziato (${postazione.numIndifferenziato}), RAEE (${postazione.numRaee}), Abiti Usati (${postazione.numAbitiUsati})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val updated = postazione.copy(
                                        latitudine = currentLocation.latitude,
                                        longitudine = currentLocation.longitude,
                                        indirizzo = currentLocation.address,
                                        dataUltimoAggiornamento = System.currentTimeMillis()
                                    )
                                    viewModel.updatePostazione(updated)
                                    Toast.makeText(context, "Spostata in posizione GPS attuale!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sposta GPS", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = {
                                    repositioningPostazione = postazione
                                    viewModel.dismissPostazioneDetails()
                                    Toast.makeText(context, "Clicca la nuova posizione sulla mappa", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sposta manuale", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { editingPostazione = postazione },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Modifica", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modifica", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    viewModel.deletePostazione(postazione)
                                    Toast.makeText(context, "Postazione eliminata!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Elimina", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Elimina", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Repositioning Banner Overlay when in Sposta Manuale Mode
            repositioningPostazione?.let { post ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp, vertical = 76.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "SPOSTAMENTO MANUALE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Clicca la nuova posizione sulla mappa per ${post.nome}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 2
                                )
                            }
                        }

                        IconButton(onClick = { repositioningPostazione = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Annulla",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Edit Postazione Dialog
            editingPostazione?.let { post ->
                EditPostazioneDialog(
                    postazione = post,
                    onDismiss = { editingPostazione = null },
                    onConfirmSave = { updated ->
                        viewModel.updatePostazione(updated)
                        editingPostazione = null
                        Toast.makeText(context, "Postazione aggiornata!", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { toDel ->
                        viewModel.deletePostazione(toDel)
                        editingPostazione = null
                        Toast.makeText(context, "Postazione eliminata!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun OsmMapView(
    currentLocation: LocationResultData,
    postazioni: List<Postazione>,
    onPostazioneClick: (Postazione) -> Unit,
    modifier: Modifier = Modifier
) {
    val webViewRef = remember { mutableStateOf<android.webkit.WebView?>(null) }

    fun getTipoDominante(p: Postazione): String {
        val map = mapOf(
            "giallo" to p.numMultimateriale,
            "vetro" to p.numVetro,
            "carta" to p.numCarta,
            "organico" to p.numOrganico,
            "indifferenziato" to p.numIndifferenziato
        )
        return map.maxByOrNull { it.value }?.key ?: "indifferenziato"
    }

    LaunchedEffect(currentLocation) {
        val safeAddress = currentLocation.address.replace("'", "\\'")
        webViewRef.value?.evaluateJavascript(
            "if (window.map) { window.map.flyTo([${currentLocation.latitude}, ${currentLocation.longitude}], 16); if(window.updateCurrentLocation) window.updateCurrentLocation(${currentLocation.latitude}, ${currentLocation.longitude}, '$safeAddress'); }",
            null
        )
    }

    LaunchedEffect(postazioni) {
        val jsonArray = withContext(Dispatchers.IO) {
            postazioni.joinToString(",", "[", "]") { p ->
                val safeName = p.nome.replace("'", "\\'").replace("\n", " ")
                val safeAddr = p.indirizzo.replace("'", "\\'").replace("\n", " ")
                val tipo = getTipoDominante(p)
                """{"id":"${p.id}","lat":${p.latitudine},"lng":${p.longitudine},"nome":"$safeName","indirizzo":"$safeAddr","tipo":"$tipo"}"""
            }
        }
        withContext(Dispatchers.Main) {
            webViewRef.value?.evaluateJavascript(
                "if (window.updatePostazioni) { window.updatePostazioni($jsonArray); }",
                null
            )
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            android.webkit.WebView(ctx).apply {
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onMarkerClicked(id: String) {
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            val found = postazioni.find { it.id.toString() == id }
                            if (found != null) {
                                onPostazioneClick(found)
                            }
                        }
                    }
                }, "AndroidBridge")

                webViewClient = object : android.webkit.WebViewClient() {}

                val safeAddress = currentLocation.address.replace("'", "\\'")
                val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
                    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
                    <style>
                        html, body, #map { width:100%; height: 100%; margin: 0; padding: 0; background: #e5e3df; }
                        .leaflet-control-attribution { display: none !important; }
                        .user-pin {
                            width: 20px; height: 20px; background-color: #0284c7; border: 3px solid white;
                            border-radius: 50%; box-shadow: 0 0 12px rgba(2,132,199,0.9);
                        }
                    </style>
                </head>
                <body>
                    <div id="map"></div>
                    <script>
                        var map = L.map('map', { zoomControl: false }).setView([${currentLocation.latitude}, ${currentLocation.longitude}], 16);
                        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19
                        }).addTo(map);
                        window.map = map;

                        var userMarker = L.marker([${currentLocation.latitude}, ${currentLocation.longitude}], {
                            icon: L.divIcon({ className: 'user-pin', iconSize: [20, 20], iconAnchor: [10, 10] })
                        }).addTo(map).bindPopup("<b>La tua posizione GPS</b><br>$safeAddress");

                        function updateCurrentLocation(lat, lng, address) {
                            userMarker.setLatLng([lat, lng]);
                            userMarker.setPopupContent("<b>La tua posizione GPS</b><br>" + address);
                        }
                        window.updateCurrentLocation = updateCurrentLocation;

                        var markersGroup = L.layerGroup().addTo(map);

                        function getColorForType(tipo) {
                            if (tipo.includes("giallo") || tipo.includes("multi")) return "#eab308";
                            if (tipo.includes("vetro")) return "#22c55e";
                            if (tipo.includes("carta")) return "#3b82f6";
                            if (tipo.includes("organico")) return "#a16207";
                            return "#6b7280";
                        }

                        function updatePostazioni(postazioni) {
                            markersGroup.clearLayers();
                            postazioni.forEach(function(p) {
                                var color = getColorForType(p.tipo || "");
                                var svgIcon = `<svg width="30" height="42" viewBox="0 0 30 42" fill="none" xmlns="http://www.w3.org/2000/svg">
                                    <path d="M15 0C6.71573 0 0 6.71573 0 15C0 26.25 15 42 15 42C15 42 30 26.25 30 15C30 6.71573 23.2843 0 15 0Z" fill="` + color + `"/>
                                    <circle cx="15" cy="15" r="7" fill="white"/>
                                </svg>`;
                                var customIcon = L.divIcon({
                                    html: svgIcon,
                                    className: '',
                                    iconSize: [30, 42],
                                    iconAnchor: [15, 42],
                                    popupAnchor: [0, -38]
                                });
                                var m = L.marker([p.lat, p.lng], { icon: customIcon }).addTo(markersGroup);
                                m.bindPopup("<b>" + p.nome + "</b><br>" + p.indirizzo);
                                m.on('click', function() {
                                    if (window.AndroidBridge) {
                                        window.AndroidBridge.onMarkerClicked(p.id);
                                    }
                                });
                            });
                        }
                        window.updatePostazioni = updatePostazioni;
                    </script>
                </body>
                </html>
                """.trimIndent()

                loadDataWithBaseURL("https://openstreetmap.org", html, "text/html", "UTF-8", null)
                webViewRef.value = this
            }
        }
    )
}
