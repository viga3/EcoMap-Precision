package com.example.ecomapprecision.data.repository

import com.example.ecomapprecision.data.db.PostazioneDao
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.util.DistanceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PostazioneRepository(private val postazioneDao: PostazioneDao) {

    val allPostazioni: Flow<List<Postazione>> = postazioneDao.getAllPostazioni()
    val postazioniCount: Flow<Int> = postazioneDao.getPostazioniCount()

    suspend fun getPostazioneById(id: Long): Postazione? {
        return postazioneDao.getPostazioneById(id)
    }

    /**
     * Checks if a postazione exists within thresholdMeters (default 15.0m).
     * Returns the closest matching Postazione if found.
     */
    suspend fun findNearbyPostazione(
        lat: Double,
        lon: Double,
        thresholdMeters: Double = 15.0
    ): Postazione? {
        val currentList = allPostazioni.first()
        var closest: Postazione? = null
        var minDistance = Double.MAX_VALUE

        for (p in currentList) {
            val dist = DistanceCalculator.calculateDistanceMeters(lat, lon, p.latitudine, p.longitudine)
            if (dist <= thresholdMeters && dist < minDistance) {
                minDistance = dist
                closest = p
            }
        }
        return closest
    }

    /**
     * Finds the closest postazione regardless of distance, along with distance in meters.
     */
    suspend fun findClosestPostazione(
        lat: Double,
        lon: Double
    ): Pair<Postazione, Double>? {
        val currentList = allPostazioni.first()
        if (currentList.isEmpty()) return null
        var closest: Postazione? = null
        var minDistance = Double.MAX_VALUE

        for (p in currentList) {
            val dist = DistanceCalculator.calculateDistanceMeters(lat, lon, p.latitudine, p.longitudine)
            if (dist < minDistance) {
                minDistance = dist
                closest = p
            }
        }
        return if (closest != null) Pair(closest, minDistance) else null
    }

    suspend fun savePostazione(postazione: Postazione): Long {
        return postazioneDao.insertPostazione(postazione)
    }

    suspend fun updatePostazione(postazione: Postazione) {
        postazioneDao.updatePostazione(postazione)
    }

    suspend fun deletePostazione(postazione: Postazione) {
        postazioneDao.deletePostazione(postazione)
    }

    suspend fun getAllPostazioniSnapshot(): List<Postazione> {
        return postazioneDao.getAllPostazioniList()
    }

    suspend fun deleteAllPostazioni(): Int {
        return postazioneDao.deleteAllPostazioni()
    }

    suspend fun importPostazioni(
        postazioni: List<Postazione>,
        duplicateThresholdMeters: Double = 15.0
    ): ImportResult {
        val existingList = postazioneDao.getAllPostazioniList().toMutableList()
        var addedCount = 0
        var skippedCount = 0

        for (imported in postazioni) {
            val match = existingList.find { existing ->
                val dist = DistanceCalculator.calculateDistanceMeters(
                    imported.latitudine,
                    imported.longitudine,
                    existing.latitudine,
                    existing.longitudine
                )
                dist <= duplicateThresholdMeters ||
                        (Math.abs(imported.latitudine - existing.latitudine) < 0.0001 &&
                         Math.abs(imported.longitudine - existing.longitudine) < 0.0001)
            }

            if (match == null) {
                val newId = postazioneDao.insertPostazione(imported.copy(id = 0))
                existingList.add(imported.copy(id = newId))
                addedCount++
            } else {
                skippedCount++
            }
        }

        return ImportResult(
            addedCount = addedCount,
            updatedOrSkippedCount = skippedCount,
            totalProcessed = postazioni.size
        )
    }
}

data class ImportResult(
    val addedCount: Int,
    val updatedOrSkippedCount: Int,
    val totalProcessed: Int
)
