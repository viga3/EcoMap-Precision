package com.example.ecomapprecision.data.repository

import com.example.ecomapprecision.data.db.SegnalazioneDao
import com.example.ecomapprecision.data.model.Segnalazione
import com.example.ecomapprecision.data.model.TipoSegnalazione
import kotlinx.coroutines.flow.Flow

class SegnalazioneRepository(private val segnalazioneDao: SegnalazioneDao) {

    val allSegnalazioni: Flow<List<Segnalazione>> = segnalazioneDao.getAllSegnalazioni()
    val segnalazioniCount: Flow<Int> = segnalazioneDao.getSegnalazioniCount()

    fun getSegnalazioniByTipo(tipo: TipoSegnalazione): Flow<List<Segnalazione>> {
        return segnalazioneDao.getSegnalazioniByTipo(tipo)
    }

    suspend fun getSegnalazioneById(id: Long): Segnalazione? {
        return segnalazioneDao.getSegnalazioneById(id)
    }

    suspend fun saveSegnalazione(segnalazione: Segnalazione): Long {
        return segnalazioneDao.insertSegnalazione(segnalazione)
    }

    suspend fun updateSegnalazione(segnalazione: Segnalazione) {
        segnalazioneDao.updateSegnalazione(segnalazione)
    }

    suspend fun deleteSegnalazione(segnalazione: Segnalazione) {
        segnalazioneDao.deleteSegnalazione(segnalazione)
    }
}
