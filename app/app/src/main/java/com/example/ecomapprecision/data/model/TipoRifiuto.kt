package com.example.ecomapprecision.data.model

import androidx.compose.ui.graphics.Color
import com.example.ecomapprecision.theme.WasteAbiti
import com.example.ecomapprecision.theme.WasteBlu
import com.example.ecomapprecision.theme.WasteGiallo
import com.example.ecomapprecision.theme.WasteGrigio
import com.example.ecomapprecision.theme.WasteMarrone
import com.example.ecomapprecision.theme.WasteRaee
import com.example.ecomapprecision.theme.WasteVerde

enum class TipoRifiuto(
    val idName: String,
    val labelName: String,
    val categoryName: String,
    val color: Color
) {
    INDIFFERENZIATO("grigio", "Indifferenziato", "Rifiuto Secco", WasteGrigio),
    CARTA("blu", "Carta e Cartone", "Carta & Cartone", WasteBlu),
    MULTIMATERIALE("giallo", "Multimateriale", "Plastica & Metalli", WasteGiallo),
    VETRO("verde", "Vetro", "Vetro & Bottiglie", WasteVerde),
    ORGANICO("marrone", "Organico", "Umido Organico", WasteMarrone),
    RAEE("raee", "RAEE", "Radio & Elettronica", WasteRaee),
    ABITI_USATI("abiti", "Abiti Usati", "Vestiti & Tessili", WasteAbiti)
}
