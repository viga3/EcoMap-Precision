package com.example.ecomapprecision.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.theme.WasteAbiti
import com.example.ecomapprecision.theme.WasteBlu
import com.example.ecomapprecision.theme.WasteGiallo
import com.example.ecomapprecision.theme.WasteGrigio
import com.example.ecomapprecision.theme.WasteMarrone
import com.example.ecomapprecision.theme.WasteRaee
import com.example.ecomapprecision.theme.WasteVerde
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryCard(
    postazione: Postazione,
    isToday: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateString = try {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy • HH:mm", Locale.ITALIAN)
        if (isToday) {
            "Oggi • " + SimpleDateFormat("HH:mm", Locale.ITALIAN).format(Date(postazione.dataUltimoAggiornamento))
        } else {
            dateFormat.format(Date(postazione.dataUltimoAggiornamento))
        }
    } catch (e: Exception) {
        "Recente"
    }

    val primaryClr = MaterialTheme.colorScheme.primary
    val cardBorder = if (isToday) androidx.compose.foundation.BorderStroke(1.5.dp, primaryClr) else null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(12.dp),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateString.uppercase(Locale.ITALIAN),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${postazione.indirizzo} - ${postazione.nome}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Dettagli",
                    tint = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coordinates badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "GPS",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format(Locale.US, "%.4f° N, %.4f° E", postazione.latitudine, postazione.longitudine),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bin Count Chips in Scrollable Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (postazione.numIndifferenziato > 0) {
                    BinChip(count = postazione.numIndifferenziato, label = "Indifferenziata", color = WasteGrigio)
                }
                if (postazione.numCarta > 0) {
                    BinChip(count = postazione.numCarta, label = "Carta", color = WasteBlu)
                }
                if (postazione.numMultimateriale > 0) {
                    BinChip(count = postazione.numMultimateriale, label = "Plastica/Giallo", color = WasteGiallo)
                }
                if (postazione.numVetro > 0) {
                    BinChip(count = postazione.numVetro, label = "Vetro", color = WasteVerde)
                }
                if (postazione.numOrganico > 0) {
                    BinChip(count = postazione.numOrganico, label = "Organico", color = WasteMarrone)
                }
                if (postazione.numRaee > 0) {
                    BinChip(count = postazione.numRaee, label = "RAEE", color = WasteRaee)
                }
                if (postazione.numAbitiUsati > 0) {
                    BinChip(count = postazione.numAbitiUsati, label = "Abiti Usati", color = WasteAbiti)
                }
            }
        }
    }
}

@Composable
private fun BinChip(
    count: Int,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
