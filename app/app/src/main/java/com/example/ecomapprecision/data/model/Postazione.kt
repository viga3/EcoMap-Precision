package com.example.ecomapprecision.data.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "postazioni")
data class Postazione(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val indirizzo: String,
    val latitudine: Double,
    val longitudine: Double,
    val numIndifferenziato: Int = 0,
    val numCarta: Int = 0,
    val numMultimateriale: Int = 0,
    val numVetro: Int = 0,
    val numOrganico: Int = 0,
    val numRaee: Int = 0,
    val numAbitiUsati: Int = 0,
    val dataCreazione: Long = System.currentTimeMillis(),
    val dataUltimoAggiornamento: Long = System.currentTimeMillis()
) {
    @Ignore
    fun totaleBins(): Int = numIndifferenziato + numCarta + numMultimateriale + numVetro + numOrganico + numRaee + numAbitiUsati
}
