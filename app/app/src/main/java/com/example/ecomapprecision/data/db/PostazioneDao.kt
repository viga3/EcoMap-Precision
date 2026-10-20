package com.example.ecomapprecision.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.ecomapprecision.data.model.Postazione
import kotlinx.coroutines.flow.Flow

import androidx.room.Delete

@Dao
interface PostazioneDao {
    @Query("SELECT * FROM postazioni ORDER BY dataUltimoAggiornamento DESC")
    fun getAllPostazioni(): Flow<List<Postazione>>

    @Query("SELECT * FROM postazioni")
    suspend fun getAllPostazioniList(): List<Postazione>

    @Query("SELECT * FROM postazioni WHERE id = :id")
    suspend fun getPostazioneById(id: Long): Postazione?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostazione(postazione: Postazione): Long

    @Update
    suspend fun updatePostazione(postazione: Postazione)

    @Delete
    suspend fun deletePostazione(postazione: Postazione)

    @Query("SELECT COUNT(*) FROM postazioni")
    fun getPostazioniCount(): Flow<Int>

    @Query("DELETE FROM postazioni")
    suspend fun deleteAllPostazioni(): Int
}
