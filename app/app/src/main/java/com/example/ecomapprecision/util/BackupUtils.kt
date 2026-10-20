package com.example.ecomapprecision.util

import android.content.Context
import android.net.Uri
import com.example.ecomapprecision.data.model.Postazione
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object BackupUtils {

    /**
     * Exports a list of Postazione objects to a JSON backup file at the specified Uri.
     */
    fun exportBackup(context: Context, uri: Uri, postazioni: List<Postazione>): Boolean {
        return try {
            val rootObject = JSONObject()
            rootObject.put("appName", "EcoMap Precision")
            rootObject.put("version", 1)
            rootObject.put("exportDate", System.currentTimeMillis())

            val array = JSONArray()
            for (p in postazioni) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("nome", p.nome)
                obj.put("indirizzo", p.indirizzo)
                obj.put("latitudine", p.latitudine)
                obj.put("longitudine", p.longitudine)
                obj.put("numIndifferenziato", p.numIndifferenziato)
                obj.put("numCarta", p.numCarta)
                obj.put("numMultimateriale", p.numMultimateriale)
                obj.put("numVetro", p.numVetro)
                obj.put("numOrganico", p.numOrganico)
                obj.put("numRaee", p.numRaee)
                obj.put("numAbitiUsati", p.numAbitiUsati)
                obj.put("dataCreazione", p.dataCreazione)
                obj.put("dataUltimoAggiornamento", p.dataUltimoAggiornamento)
                array.put(obj)
            }

            rootObject.put("postazioni", array)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(rootObject.toString(2).toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads a JSON backup file from the specified Uri and parses it into a list of Postazione objects.
     */
    fun importBackup(context: Context, uri: Uri): List<Postazione>? {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return null

            val rootObject = JSONObject(jsonString)
            val array = if (rootObject.has("postazioni")) {
                rootObject.getJSONArray("postazioni")
            } else if (jsonString.trim().startsWith("[")) {
                JSONArray(jsonString)
            } else {
                return null
            }

            val list = mutableListOf<Postazione>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val postazione = Postazione(
                    id = 0, // Reset ID so Room assigns a new primary key upon import
                    nome = obj.optString("nome", "Postazione Importata"),
                    indirizzo = obj.optString("indirizzo", ""),
                    latitudine = obj.optDouble("latitudine", 0.0),
                    longitudine = obj.optDouble("longitudine", 0.0),
                    numIndifferenziato = obj.optInt("numIndifferenziato", 0),
                    numCarta = obj.optInt("numCarta", 0),
                    numMultimateriale = obj.optInt("numMultimateriale", 0),
                    numVetro = obj.optInt("numVetro", 0),
                    numOrganico = obj.optInt("numOrganico", 0),
                    numRaee = obj.optInt("numRaee", 0),
                    numAbitiUsati = obj.optInt("numAbitiUsati", 0),
                    dataCreazione = obj.optLong("dataCreazione", System.currentTimeMillis()),
                    dataUltimoAggiornamento = obj.optLong("dataUltimoAggiornamento", System.currentTimeMillis())
                )
                list.add(postazione)
            }
            list
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
