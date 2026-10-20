package com.example.ecomapprecision.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecomapprecision.data.model.Segnalazione
import com.example.ecomapprecision.data.model.TipoSegnalazione
import kotlinx.coroutines.flow.Flow

@Dao
interface SegnalazioneDao {
    @Query("SELECT * FROM segnalazioni ORDER BY dataCreazione DESC")
    fun getAllSegnalazioni(): Flow<List<Segnalazione>>

    @Query("SELECT * FROM segnalazioni WHERE tipo = :tipo ORDER BY dataCreazione DESC")
    fun getSegnalazioniByTipo(tipo: TipoSegnalazione): Flow<List<Segnalazione>>

    @Query("SELECT * FROM segnalazioni WHERE id = :id")
    suspend fun getSegnalazioneById(id: Long): Segnalazione?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegnalazione(segnalazione: Segnalazione): Long

    @Update
    suspend fun updateSegnalazione(segnalazione: Segnalazione)

    @Delete
    suspend fun deleteSegnalazione(segnalazione: Segnalazione)

    @Query("SELECT COUNT(*) FROM segnalazioni")
    fun getSegnalazioniCount(): Flow<Int>
}
