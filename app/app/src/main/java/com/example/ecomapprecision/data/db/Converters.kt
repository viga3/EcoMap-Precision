package com.example.ecomapprecision.data.db

import androidx.room.TypeConverter
import com.example.ecomapprecision.data.model.StatoSegnalazione
import com.example.ecomapprecision.data.model.TipoSegnalazione

class Converters {
    @TypeConverter
    fun fromTipoSegnalazione(value: TipoSegnalazione): String = value.name

    @TypeConverter
    fun toTipoSegnalazione(value: String): TipoSegnalazione = TipoSegnalazione.valueOf(value)

    @TypeConverter
    fun fromStatoSegnalazione(value: StatoSegnalazione): String = value.name

    @TypeConverter
    fun toStatoSegnalazione(value: String): StatoSegnalazione = StatoSegnalazione.valueOf(value)
}
