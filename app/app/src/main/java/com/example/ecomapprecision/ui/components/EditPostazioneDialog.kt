package com.example.ecomapprecision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ecomapprecision.data.model.Postazione
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.ecomapprecision.theme.WasteAbiti
import com.example.ecomapprecision.theme.WasteBlu
import com.example.ecomapprecision.theme.WasteGiallo
import com.example.ecomapprecision.theme.WasteGrigio
import com.example.ecomapprecision.theme.WasteMarrone
import com.example.ecomapprecision.theme.WasteRaee
import com.example.ecomapprecision.theme.WasteVerde

@Composable
fun EditPostazioneDialog(
    postazione: Postazione,
    onDismiss: () -> Unit,
    onConfirmSave: (Postazione) -> Unit,
    onDelete: (Postazione) -> Unit
) {
    var indirizzo by remember { mutableStateOf(postazione.indirizzo) }
    var latitudine by remember { mutableDoubleStateOf(postazione.latitudine) }
    var longitudine by remember { mutableDoubleStateOf(postazione.longitudine) }

    var numIndifferenziato by remember { mutableIntStateOf(postazione.numIndifferenziato) }
    var numCarta by remember { mutableIntStateOf(postazione.numCarta) }
    var numMultimateriale by remember { mutableIntStateOf(postazione.numMultimateriale) }
    var numVetro by remember { mutableIntStateOf(postazione.numVetro) }
    var numOrganico by remember { mutableIntStateOf(postazione.numOrganico) }
    var numRaee by remember { mutableIntStateOf(postazione.numRaee) }
    var numAbitiUsati by remember { mutableIntStateOf(postazione.numAbitiUsati) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isUpdatingGps by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with title and large X close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MODIFICA POSTAZIONE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = postazione.nome,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Large prominent X close button
                    IconButton(
                        onClick = onDismiss,
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

                Spacer(modifier = Modifier.height(16.dp))

                // Location & Address Edit Section
                Text(
                    text = "Posizione e Indirizzo:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = indirizzo,
                    onValueChange = { indirizzo = it },
                    label = { Text("Indirizzo postazione") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (indirizzo.isNotBlank()) {
                                    coroutineScope.launch {
                                        val coords = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            com.example.ecomapprecision.util.GeocodingUtils.searchAddress(context, indirizzo)
                                        }
                                        if (coords != null) {
                                            latitudine = coords.first
                                            longitudine = coords.second
                                            android.widget.Toast.makeText(context, "Coordinate aggiornate per indirizzo!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Indirizzo non trovato", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Cerca indirizzo")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format(Locale.US, "%.4f° N, %.4f° E", latitudine, longitudine),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                if (indirizzo.isNotBlank()) {
                                    coroutineScope.launch {
                                        val coords = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            com.example.ecomapprecision.util.GeocodingUtils.searchAddress(context, indirizzo)
                                        }
                                        if (coords != null) {
                                            latitudine = coords.first
                                            longitudine = coords.second
                                            android.widget.Toast.makeText(context, "Trova da indirizzo ok!", android.widget.Toast.LENGTH_SHORT).show()
                                        } else {
                                            android.widget.Toast.makeText(context, "Indirizzo non trovato", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Da Casa", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Trova Indirizzo", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                isUpdatingGps = true
                                coroutineScope.launch {
                                    val locationService = com.example.ecomapprecision.location.LocationService(context)
                                    val loc = locationService.getCurrentLocation()
                                    latitudine = loc.latitude
                                    longitudine = loc.longitude
                                    if (loc.address.isNotBlank()) {
                                        indirizzo = loc.address
                                    }
                                    isUpdatingGps = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "GPS",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isUpdatingGps) "Rilevo..." else "Usa GPS", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Aggiorna quantità cassonetti:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Counter Rows
                EditCounterRow("Multimateriale", WasteGiallo, numMultimateriale) { numMultimateriale = it }
                EditCounterRow("Vetro", WasteVerde, numVetro) { numVetro = it }
                EditCounterRow("Carta", WasteBlu, numCarta) { numCarta = it }
                EditCounterRow("Organico", WasteMarrone, numOrganico) { numOrganico = it }
                EditCounterRow("Indifferenziato", WasteGrigio, numIndifferenziato) { numIndifferenziato = it }
                EditCounterRow("RAEE", WasteRaee, numRaee) { numRaee = it }
                EditCounterRow("Abiti Usati", WasteAbiti, numAbitiUsati) { numAbitiUsati = it }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onDelete(postazione) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Elimina")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Elimina", fontWeight = FontWeight.Bold)
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Annulla")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val updated = postazione.copy(
                                    indirizzo = indirizzo,
                                    latitudine = latitudine,
                                    longitudine = longitudine,
                                    numIndifferenziato = numIndifferenziato,
                                    numCarta = numCarta,
                                    numMultimateriale = numMultimateriale,
                                    numVetro = numVetro,
                                    numOrganico = numOrganico,
                                    numRaee = numRaee,
                                    numAbitiUsati = numAbitiUsati,
                                    dataUltimoAggiornamento = System.currentTimeMillis()
                                )
                                onConfirmSave(updated)
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Salva", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditCounterRow(
    label: String,
    badgeColor: Color,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(badgeColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (count > 0) onCountChange(count - 1) },
                enabled = count > 0,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "Meno")
            }

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            IconButton(
                onClick = { onCountChange(count + 1) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Più")
            }
        }
    }
}
