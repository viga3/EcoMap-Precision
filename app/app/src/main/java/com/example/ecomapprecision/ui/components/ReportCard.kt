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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.PropaneTank
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
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
import com.example.ecomapprecision.data.model.Segnalazione
import com.example.ecomapprecision.data.model.StatoSegnalazione
import com.example.ecomapprecision.data.model.TipoSegnalazione
import com.example.ecomapprecision.theme.StatusInCaricoBg
import com.example.ecomapprecision.theme.StatusInCaricoText
import com.example.ecomapprecision.theme.StatusNuovoBg
import com.example.ecomapprecision.theme.StatusNuovoText
import com.example.ecomapprecision.theme.StatusRisoltoBg
import com.example.ecomapprecision.theme.StatusRisoltoText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.CameraAlt

@Composable
fun ReportCard(
    segnalazione: Segnalazione,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val indicatorColor = when (segnalazione.stato) {
        StatoSegnalazione.NUOVO -> MaterialTheme.colorScheme.error
        StatoSegnalazione.IN_CARICO -> MaterialTheme.colorScheme.tertiary
        StatoSegnalazione.RISOLTO -> MaterialTheme.colorScheme.primary
    }

    val icon = when (segnalazione.tipo) {
        TipoSegnalazione.ABBANDONI -> Icons.Default.Delete
        TipoSegnalazione.INGOMBRANTI -> Icons.Default.Chair
        TipoSegnalazione.BOMBOLE -> Icons.Default.PropaneTank
        TipoSegnalazione.SACCHI_A_TERRA -> Icons.Default.ShoppingBag
        TipoSegnalazione.CASSONETTO_BLOCCATO -> Icons.Default.LockReset
        TipoSegnalazione.RIFIUTI_SPECIALI -> Icons.Default.Warning
    }

    val (badgeBg, badgeText) = when (segnalazione.stato) {
        StatoSegnalazione.NUOVO -> StatusNuovoBg to StatusNuovoText
        StatoSegnalazione.IN_CARICO -> StatusInCaricoBg to StatusInCaricoText
        StatoSegnalazione.RISOLTO -> StatusRisoltoBg to StatusRisoltoText
    }

    val timeString = SimpleDateFormat("HH:mm", Locale.ITALIAN).format(Date(segnalazione.dataCreazione))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left indicator bar
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(110.dp)
                    .background(indicatorColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(indicatorColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = segnalazione.tipo.title,
                                tint = indicatorColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = segnalazione.tipo.title,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = segnalazione.indirizzo,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (!segnalazione.note.isNullOrBlank()) {
                                Text(
                                    text = segnalazione.note,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = segnalazione.stato.label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = badgeText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Ora",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (!segnalazione.fotoPath.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Foto presente",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Foto allegata",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
