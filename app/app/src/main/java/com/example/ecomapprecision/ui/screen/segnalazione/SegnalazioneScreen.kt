package com.example.ecomapprecision.ui.screen.segnalazione

import android.widget.Toast
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PropaneTank
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import com.example.ecomapprecision.util.DistanceCalculator
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomapprecision.data.model.TipoSegnalazione
import com.example.ecomapprecision.ui.components.TopAppBarEcoMap

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.ecomapprecision.util.PhotoStorageUtils
import java.io.File

@Composable
fun SegnalazioneScreen(
    onNavigateBack: () -> Unit,
    viewModel: SegnalazioneViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showChangePostazioneDialog by remember { mutableStateOf(false) }
    var postazioneFilterText by remember { mutableStateOf("") }
    var showSearchAddressDialog by remember { mutableStateOf(false) }
    var searchAddressText by remember { mutableStateOf("") }

    var currentPhotoTarget by remember { mutableStateOf<PhotoStorageUtils.PhotoTarget?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoTarget != null) {
            val path = currentPhotoTarget!!.filePath
            viewModel.setPhotoPath(path)
            PhotoStorageUtils.notifyMediaScanner(context, path)
            Toast.makeText(context, "Foto scattata e salvata in Pictures/EcoMap!", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val copiedFile = PhotoStorageUtils.copyUriToAccessibleFile(context, it, "Galleria")
            if (copiedFile != null) {
                viewModel.setPhotoPath(copiedFile.absolutePath)
                Toast.makeText(context, "Foto allegata e salvata in Pictures/EcoMap!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openCamera() {
        try {
            val target = PhotoStorageUtils.createCameraPhotoTarget(context, "Segnalazione")
            if (target != null) {
                currentPhotoTarget = target
                takePictureLauncher.launch(target.uri)
            } else {
                Toast.makeText(context, "Impossibile creare il file foto in Pictures/EcoMap", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Errore apertura fotocamera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SegnalazioneEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SegnalazioneEvent.SubmitSuccess -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBarEcoMap(
                title = "EcoMap Precision",
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
                Button(
                    onClick = { viewModel.submitSegnalazione() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Invia",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Invia Segnalazione",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Dialog Selezione Postazione
            if (showChangePostazioneDialog) {
                AlertDialog(
                    onDismissRequest = { showChangePostazioneDialog = false },
                    title = {
                        Text(
                            text = "Seleziona Postazione",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = postazioneFilterText,
                                onValueChange = { postazioneFilterText = it },
                                placeholder = { Text("Cerca per nome o via...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Cerca")
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.resetToCurrentGps()
                                    showChangePostazioneDialog = false
                                    Toast.makeText(context, "Riagganciata postazione più vicina al GPS", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Riaggancia alla più vicina al GPS", style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val userGps = uiState.gpsLocationData
                            val sortedList = remember(uiState.allPostazioni, postazioneFilterText, userGps) {
                                uiState.allPostazioni
                                    .filter { p ->
                                        if (postazioneFilterText.isBlank()) true
                                        else p.nome.contains(postazioneFilterText, ignoreCase = true) ||
                                                p.indirizzo.contains(postazioneFilterText, ignoreCase = true)
                                    }
                                    .map { p ->
                                        val dist = if (userGps != null && userGps.isRealFix) {
                                            DistanceCalculator.calculateDistanceMeters(
                                                userGps.latitude, userGps.longitude,
                                                p.latitudine, p.longitudine
                                            )
                                        } else null
                                        p to dist
                                    }
                                    .sortedBy { it.second ?: Double.MAX_VALUE }
                            }

                            if (sortedList.isEmpty()) {
                                Text(
                                    text = "Nessuna postazione trovata.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 320.dp)
                                ) {
                                    items(sortedList) { (postazione, dist) ->
                                        val isSelected = uiState.selectedPostazione?.id == postazione.id
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp)
                                                .clickable {
                                                    viewModel.selectPostazione(postazione)
                                                    showChangePostazioneDialog = false
                                                    Toast.makeText(context, "Selezionata: ${postazione.nome}", Toast.LENGTH_SHORT).show()
                                                },
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = postazione.nome,
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = postazione.indirizzo,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (dist != null) {
                                                    val distText = if (dist < 1000) "${dist.toInt()} m" else String.format(java.util.Locale.US, "%.1f km", dist / 1000.0)
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                        modifier = Modifier.padding(start = 8.dp)
                                                    ) {
                                                        Text(
                                                            text = distText,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showChangePostazioneDialog = false }) {
                            Text("Chiudi")
                        }
                    }
                )
            }

            // Dialog Cerca Indirizzo
            if (showSearchAddressDialog) {
                AlertDialog(
                    onDismissRequest = { showSearchAddressDialog = false },
                    title = { Text("Cerca Indirizzo", fontWeight = FontWeight.Bold) },
                    text = {
                        OutlinedTextField(
                            value = searchAddressText,
                            onValueChange = { searchAddressText = it },
                            placeholder = { Text("Es. Corso Vittorio Emanuele...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.searchAddress(searchAddressText) { found ->
                                    if (found) {
                                        showSearchAddressDialog = false
                                        Toast.makeText(context, "Indirizzo impostato", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Indirizzo non trovato", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        ) {
                            Text("Cerca")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSearchAddressDialog = false }) {
                            Text("Annulla")
                        }
                    }
                )
            }

            // Header Postazione
            Text(
                text = "POSTAZIONE SEGNALATA",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Card Postazione Principale
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    if (uiState.isPostazioneAgganciata) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.secondaryContainer
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (uiState.isPostazioneAgganciata) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                                contentDescription = "Posizione",
                                tint = if (uiState.isPostazioneAgganciata) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            val badgeLabel = when {
                                uiState.isPostazioneAgganciata -> "POSTAZIONE PIÙ VICINA (AGGANCIATA)"
                                uiState.selectedPostazione != null -> "POSTAZIONE SELEZIONATA DAL MENU"
                                else -> "POSIZIONE GPS RILEVATA"
                            }
                            Text(
                                text = badgeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (uiState.isPostazioneAgganciata) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                            val title = uiState.selectedPostazione?.let { "${it.nome} • ${it.indirizzo}" } ?: uiState.locationData.address
                            Text(
                                text = title,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val distStr = uiState.distanceToSelectedPostazioneMeters?.let { d ->
                                if (d < 1000) "${d.toInt()} m" else String.format(java.util.Locale.US, "%.1f km", d / 1000.0)
                            }
                            val accuracyStr = uiState.gpsLocationData?.accuracyMeters?.let { "${it.toInt()} m" } ?: "3 m"
                            val subtitle = if (distStr != null) {
                                "Distanza dalla tua posizione: $distStr • Precisione GPS: $accuracyStr"
                            } else {
                                "Precisione GPS: $accuracyStr"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showChangePostazioneDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cambia dal menu", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }

                        if (uiState.isManualLocation) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.resetToCurrentGps()
                                    Toast.makeText(context, "Riagganciata postazione più vicina", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Riaggancia GPS", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Segnala da un'altra postazione widget
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Casa",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Segnala da un'altra postazione?",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showChangePostazioneDialog = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showChangePostazioneDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scegli postazione", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = { showSearchAddressDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cerca indirizzo", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cosa vuoi segnalare?",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 6 Reporting Categories Grid (2 Columns)
            val item1 = TipoSegnalazione.ABBANDONI
            val item2 = TipoSegnalazione.INGOMBRANTI
            val item3 = TipoSegnalazione.BOMBOLE
            val item4 = TipoSegnalazione.SACCHI_A_TERRA
            val item5 = TipoSegnalazione.CASSONETTO_BLOCCATO
            val item6 = TipoSegnalazione.RIFIUTI_SPECIALI

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegnalazioneOptionItem(
                        tipo = item1,
                        isSelected = uiState.selectedTipo == item1,
                        onClick = { viewModel.selectTipo(item1) },
                        modifier = Modifier.weight(1f)
                    )
                    SegnalazioneOptionItem(
                        tipo = item2,
                        isSelected = uiState.selectedTipo == item2,
                        onClick = { viewModel.selectTipo(item2) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegnalazioneOptionItem(
                        tipo = item3,
                        isSelected = uiState.selectedTipo == item3,
                        onClick = { viewModel.selectTipo(item3) },
                        modifier = Modifier.weight(1f)
                    )
                    SegnalazioneOptionItem(
                        tipo = item4,
                        isSelected = uiState.selectedTipo == item4,
                        onClick = { viewModel.selectTipo(item4) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SegnalazioneOptionItem(
                        tipo = item5,
                        isSelected = uiState.selectedTipo == item5,
                        onClick = { viewModel.selectTipo(item5) },
                        modifier = Modifier.weight(1f)
                    )
                    SegnalazioneOptionItem(
                        tipo = item6,
                        isSelected = uiState.selectedTipo == item6,
                        onClick = { viewModel.selectTipo(item6) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Sub-details for Bombole (Colore & Quantità)
            if (uiState.selectedTipo == TipoSegnalazione.BOMBOLE) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Dettaglio Bombole per Colore:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val colorsList = listOf(
                            Triple("Rossa", Color(0xFFEF4444), uiState.bomboleRossa),
                            Triple("Blu", Color(0xFF3B82F6), uiState.bomboleBlu),
                            Triple("Grigia", Color(0xFF6B7280), uiState.bomboleGrigia),
                            Triple("Verde", Color(0xFF22C55E), uiState.bomboleVerde)
                        )

                        colorsList.forEach { (coloreName, badgeColor, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Bombola $coloreName",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { viewModel.decrementBombole(coloreName) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("-", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(
                                        text = count.toString(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    IconButton(
                                        onClick = { viewModel.incrementBombole(coloreName) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Text("+", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Sub-details for Cassonetto Bloccato / Danneggiato
            if (uiState.selectedTipo == TipoSegnalazione.CASSONETTO_BLOCCATO) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Specifica quale cassonetto è bloccato/danneggiato:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val cassonettiOptions = listOf(
                            "Indifferenziato",
                            "Plastica / Giallo",
                            "Carta",
                            "Organico",
                            "Vetro",
                            "Contenitore RAEE",
                            "Contenitore Abiti Usati"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            cassonettiOptions.forEach { option ->
                                val isSelected = uiState.cassonettoBloccatoTipo == option
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setCassonettoBloccatoTipo(option) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.LockReset,
                                            contentDescription = null,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Photo documentation section
            Text(
                text = "Documentazione fotografica",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!uiState.photoPath.isNullOrBlank() && File(uiState.photoPath!!).exists()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = File(uiState.photoPath!!),
                            contentDescription = "Foto allegata",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )

                        // Delete photo X button top-right
                        IconButton(
                            onClick = { viewModel.setPhotoPath(null) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(36.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Rimuovi foto",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Storage path badge at bottom
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(8.dp),
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = "Pictures/EcoMap/${File(uiState.photoPath!!).name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Camera button
                    Button(
                        onClick = { openCamera() },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Fotocamera",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fotocamera",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Gallery button
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Galleria",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Galleria",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes section
            Text(
                text = "Note aggiuntive",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.note,
                onValueChange = viewModel::onNoteChange,
                placeholder = { Text("Aggiungi dettagli come il civico esatto o il tipo di rifiuto...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SegnalazioneOptionItem(
    tipo: TipoSegnalazione,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryClr = MaterialTheme.colorScheme.primary
    val outlineClr = MaterialTheme.colorScheme.outlineVariant

    val icon = when (tipo) {
        TipoSegnalazione.ABBANDONI -> Icons.Default.Delete
        TipoSegnalazione.INGOMBRANTI -> Icons.Default.Chair
        TipoSegnalazione.BOMBOLE -> Icons.Default.PropaneTank
        TipoSegnalazione.SACCHI_A_TERRA -> Icons.Default.ShoppingBag
        TipoSegnalazione.CASSONETTO_BLOCCATO -> Icons.Default.LockReset
        TipoSegnalazione.RIFIUTI_SPECIALI -> Icons.Default.Warning
    }

    val titleText = tipo.title

    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, primaryClr) else androidx.compose.foundation.BorderStroke(1.dp, outlineClr)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) primaryClr else MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = titleText,
                    tint = if (isSelected) Color.White else primaryClr,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = titleText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
