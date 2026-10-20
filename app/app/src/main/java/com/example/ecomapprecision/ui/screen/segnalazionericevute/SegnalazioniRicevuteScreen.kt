package com.example.ecomapprecision.ui.screen.segnalazionericevute

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomapprecision.ui.components.BottomNavBar
import com.example.ecomapprecision.ui.components.FilterChipItem
import com.example.ecomapprecision.ui.components.FilterChipsRow
import com.example.ecomapprecision.ui.components.ReportCard
import com.example.ecomapprecision.ui.components.ScreenTab
import com.example.ecomapprecision.ui.components.TopAppBarEcoMap

import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import com.example.ecomapprecision.data.model.Segnalazione
import com.example.ecomapprecision.data.model.StatoSegnalazione

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import coil.compose.AsyncImage
import java.io.File

import com.example.ecomapprecision.util.PdfReportUtils

@Composable
fun SegnalazioniRicevuteScreen(
    onNavigateTab: (ScreenTab) -> Unit,
    onNavigateToNewSegnalazione: () -> Unit,
    onNavigateBack: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: SegnalazioniRicevuteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedSegnalazioneAction by remember { mutableStateOf<Segnalazione?>(null) }
    val context = LocalContext.current

    val filterItems = listOf(
        FilterChipItem("tutte", "Tutte (${uiState.totalCount})"),
        FilterChipItem("abbandoni", "Abbandoni"),
        FilterChipItem("cassonetti", "Cassonetti"),
        FilterChipItem("ingombranti", "Ingombranti"),
        FilterChipItem("bombole", "Bombole")
    )

    Scaffold(
        topBar = {
            TopAppBarEcoMap(
                title = "Segnalazioni Ricevute",
                showBack = true,
                onBackClick = onNavigateBack,
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = ScreenTab.Segnalazioni.route,
                onTabSelected = onNavigateTab
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewSegnalazione,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuova Segnalazione",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips Bar
            FilterChipsRow(
                items = filterItems,
                selectedId = uiState.selectedFilterId,
                onSelectedChange = { viewModel.onFilterSelected(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Title & PDF Export Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gestione Segnalazioni",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { PdfReportUtils.generateAndSharePdfReport(context, uiState.segnalazioni) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Esporta PDF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filtra",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reports List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.segnalazioni) { segnalazione ->
                    ReportCard(
                        segnalazione = segnalazione,
                        onClick = {
                            selectedSegnalazioneAction = segnalazione
                        }
                    )
                }
            }
        }

        // Action Dialog for Segnalazione
        selectedSegnalazioneAction?.let { seg ->
            Dialog(onDismissRequest = { selectedSegnalazioneAction = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "SEGNALAZIONE #${seg.id}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = seg.tipo.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = seg.indirizzo,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Photo Display
                        if (!seg.fotoPath.isNullOrBlank() && File(seg.fotoPath).exists()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "FOTO ALLEGATA:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = File(seg.fotoPath),
                                    contentDescription = "Foto allegata",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            }
                        }

                        // Notes Display
                        if (!seg.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "NOTE E DETTAGLI:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text(
                                    text = seg.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cambia Stato:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.updateStato(seg, StatoSegnalazione.NUOVO)
                                    selectedSegnalazioneAction = null
                                    Toast.makeText(context, "Stato impostato a NUOVO", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            ) {
                                Text("Nuovo", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateStato(seg, StatoSegnalazione.IN_CARICO)
                                    selectedSegnalazioneAction = null
                                    Toast.makeText(context, "Stato impostato a IN CARICO", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                            ) {
                                Text("In Carico", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateStato(seg, StatoSegnalazione.RISOLTO)
                                    selectedSegnalazioneAction = null
                                    Toast.makeText(context, "Stato impostato a RISOLTO", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                            ) {
                                Text("Risolto", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteSegnalazione(seg)
                                    selectedSegnalazioneAction = null
                                    Toast.makeText(context, "Segnalazione eliminata", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Elimina", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Elimina", style = MaterialTheme.typography.labelSmall)
                            }

                            TextButton(onClick = { selectedSegnalazioneAction = null }) {
                                Text("Chiudi")
                            }
                        }
                    }
                }
            }
        }
    }
}
