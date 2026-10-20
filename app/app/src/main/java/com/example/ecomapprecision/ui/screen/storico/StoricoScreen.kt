package com.example.ecomapprecision.ui.screen.storico

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecomapprecision.ui.components.BottomNavBar
import com.example.ecomapprecision.ui.components.HistoryCard
import com.example.ecomapprecision.ui.components.ScreenTab
import com.example.ecomapprecision.ui.components.TopAppBarEcoMap

import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.ecomapprecision.ui.components.EditPostazioneDialog

import androidx.compose.foundation.layout.Box
import com.example.ecomapprecision.data.model.Postazione

@Composable
fun StoricoScreen(
    onNavigateTab: (ScreenTab) -> Unit,
    onNavigateToAddPostazione: () -> Unit,
    onMenuClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: StoricoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPostazioneToEdit by remember { mutableStateOf<Postazione?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBarEcoMap(
                title = "Storico Rilevamenti",
                onMenuClick = onMenuClick,
                onProfileClick = onProfileClick
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = ScreenTab.Storico.route,
                onTabSelected = onNavigateTab
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPostazione,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddLocationAlt,
                    contentDescription = "Nuovo Rilevamento",
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
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Cerca indirizzo o data...", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Summary Stats Cards Row
            val primaryColor = MaterialTheme.colorScheme.primary
            val secondaryColor = MaterialTheme.colorScheme.secondary
            val primaryBorder = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
            val secondaryBorder = androidx.compose.foundation.BorderStroke(1.dp, secondaryColor.copy(alpha = 0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Bins Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                    ),
                    border = primaryBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Totale Bins",
                            style = MaterialTheme.typography.labelSmall,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.totalBins.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor
                        )
                    }
                }

                // Total Detections Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                    ),
                    border = secondaryBorder
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Rilevamenti",
                            style = MaterialTheme.typography.labelSmall,
                            color = secondaryColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.totalRilevamenti.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = secondaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // History List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.postazioni) { postazione ->
                    HistoryCard(
                        postazione = postazione,
                        isToday = android.text.format.DateUtils.isToday(postazione.dataUltimoAggiornamento),
                        onClick = { selectedPostazioneToEdit = postazione }
                    )
                }

                item {
                    Text(
                        text = "ECOMAP PRECISION BY MICHELE VILLANI",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Edit Postazione Dialog from Storico
        selectedPostazioneToEdit?.let { post ->
            EditPostazioneDialog(
                postazione = post,
                onDismiss = { selectedPostazioneToEdit = null },
                onConfirmSave = { updated ->
                    viewModel.updatePostazione(updated)
                    selectedPostazioneToEdit = null
                    Toast.makeText(context, "Postazione aggiornata!", Toast.LENGTH_SHORT).show()
                },
                onDelete = { toDel ->
                    viewModel.deletePostazione(toDel)
                    selectedPostazioneToEdit = null
                    Toast.makeText(context, "Postazione eliminata!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
