package com.example.ecomapprecision.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.data.model.Segnalazione

@Database(
    entities = [Postazione::class, Segnalazione::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class EcoMapDatabase : RoomDatabase() {
    abstract fun postazioneDao(): PostazioneDao
    abstract fun segnalazioneDao(): SegnalazioneDao

    companion object {
        @Volatile
        private var INSTANCE: EcoMapDatabase? = null

        fun getDatabase(context: Context): EcoMapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EcoMapDatabase::class.java,
                    "ecomap_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
