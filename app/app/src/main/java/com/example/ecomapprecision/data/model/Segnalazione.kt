package com.example.ecomapprecision.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "segnalazioni")
data class Segnalazione(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: TipoSegnalazione,
    val stato: StatoSegnalazione = StatoSegnalazione.NUOVO,
    val indirizzo: String,
    val latitudine: Double,
    val longitudine: Double,
    val fotoPath: String? = null,
    val note: String? = null,
    val dataCreazione: Long = System.currentTimeMillis(),
    val bomboleRossa: Int = 0,
    val bomboleBlu: Int = 0,
    val bomboleGrigia: Int = 0,
    val bomboleVerde: Int = 0,
    val cassonettoBloccatoTipo: String? = null
)
