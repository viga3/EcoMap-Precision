package com.example.ecomapprecision.ui.screen.addpostazione

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomapprecision.data.db.EcoMapDatabase
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.data.model.TipoRifiuto
import com.example.ecomapprecision.data.repository.PostazioneRepository
import com.example.ecomapprecision.location.LocationResultData
import com.example.ecomapprecision.location.LocationService
import com.example.ecomapprecision.ui.components.ConfirmDialogType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle
import com.example.ecomapprecision.util.GeocodingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.ecomapprecision.util.DistanceCalculator

data class AddPostazioneUiState(
    val locationData: LocationResultData = LocationResultData(45.4642, 9.1900, "Via Roma, 12, Milano"),
    val binCounts: Map<TipoRifiuto, Int> = mapOf(
        TipoRifiuto.INDIFFERENZIATO to 1,
        TipoRifiuto.CARTA to 1,
        TipoRifiuto.MULTIMATERIALE to 1,
        TipoRifiuto.VETRO to 1,
        TipoRifiuto.ORGANICO to 1,
        TipoRifiuto.RAEE to 0,
        TipoRifiuto.ABITI_USATI to 0
    ),
    val pendingDialogType: ConfirmDialogType? = null,
    val existingNearbyPostazione: Postazione? = null,
    val projectedStationName: String = "",
    val isSavedSuccess: Boolean = false
)

sealed class AddPostazioneEvent {
    object SaveSuccess : AddPostazioneEvent()
    data class ShowToast(val message: String) : AddPostazioneEvent()
}

class AddPostazioneViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle? = null
) : AndroidViewModel(application) {
    private val repository: PostazioneRepository
    private val locationService = LocationService(application)

    private val _uiState = MutableStateFlow(AddPostazioneUiState())
    val uiState: StateFlow<AddPostazioneUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AddPostazioneEvent>()
    val eventFlow: SharedFlow<AddPostazioneEvent> = _eventFlow.asSharedFlow()

    init {
        val dao = EcoMapDatabase.getDatabase(application).postazioneDao()
        repository = PostazioneRepository(dao)

        val latArg = savedStateHandle?.get<String>("lat")?.toDoubleOrNull()
        val lngArg = savedStateHandle?.get<String>("lng")?.toDoubleOrNull()

        if (latArg != null && lngArg != null) {
            setInitialCoordinates(latArg, lngArg)
        } else {
            refreshLocation()
        }
    }

    fun setInitialCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            val addr = withContext(Dispatchers.IO) {
                GeocodingUtils.reverseGeocode(getApplication(), lat, lon)
            }
            _uiState.update {
                it.copy(locationData = LocationResultData(lat, lon, addr))
            }
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationService.getCurrentLocation()
            _uiState.update { it.copy(locationData = loc) }
        }
    }

    fun updateManualLocation(newLoc: LocationResultData) {
        _uiState.update { it.copy(locationData = newLoc) }
    }

    fun incrementBin(tipo: TipoRifiuto) {
        _uiState.update { state ->
            val current = state.binCounts[tipo] ?: 0
            val updated = state.binCounts.toMutableMap().apply { put(tipo, current + 1) }
            state.copy(binCounts = updated)
        }
    }

    fun decrementBin(tipo: TipoRifiuto) {
        _uiState.update { state ->
            val current = state.binCounts[tipo] ?: 0
            if (current > 0) {
                val updated = state.binCounts.toMutableMap().apply { put(tipo, current - 1) }
                state.copy(binCounts = updated)
            } else state
        }
    }

    private fun getCleanStreetName(address: String): String {
        if (address.isBlank() || address.contains("Posizione GPS") || address.contains("in corso")) return ""

        var text = address.replace(Regex("\\([^)]*\\)"), " ")
        val firstPart = text.split(",").firstOrNull() ?: text

        var clean = firstPart.lowercase().trim()
            .replace('à', 'a').replace('á', 'a')
            .replace('è', 'e').replace('é', 'e')
            .replace('ì', 'i').replace('í', 'i')
            .replace('ò', 'o').replace('ó', 'o')
            .replace('ù', 'u').replace('ú', 'u')

        clean = clean.replace(Regex("(?i)\\b(n°?|num\\.?|civico)?\\s*\\d+([a-z]|/[a-z0-9]+)?\\b"), " ")
        clean = clean.replace(Regex("\\d+"), " ")

        val prefixes = listOf(
            "via delle", "via dei", "via del", "via degli", "via della", "via dello", "via di", "via da",
            "viale delle", "viale dei", "viale del", "viale degli", "viale della", "viale dello", "viale di",
            "corso", "viale", "piazza", "piazzale", "largo", "vicolo", "vialetto", "strada", "statale",
            "provinciale", "lungomare", "traversa", "calata", "borgo", "banchina", "via", "v."
        )
        for (p in prefixes) {
            if (clean.startsWith("$p ") || clean == p) {
                clean = clean.removePrefix(p).trim()
                break
            }
        }

        val words = clean.split(Regex("[^a-z0-9]+")).filter { it.length >= 2 }
        return words.joinToString(" ").trim()
    }

    private fun isSameStreet(
        addr1: String,
        addr2: String
    ): Boolean {
        val s1 = getCleanStreetName(addr1)
        val s2 = getCleanStreetName(addr2)

        if (s1.isBlank() || s2.isBlank()) return false
        if (s1 == s2) return true

        if (s1.length >= 4 && s2.contains(s1)) return true
        if (s2.length >= 4 && s1.contains(s2)) return true

        return false
    }

    private suspend fun calculateProjectedStationName(address: String): String {
        val streetName = getCleanStreetName(address)
        if (streetName.isBlank()) {
            return "Postazione 1"
        }

        val allList = repository.allPostazioni.first()
        var countOnSameStreet = 0
        var maxNumberOnStreet = 0

        for (post in allList) {
            if (isSameStreet(post.indirizzo, address)) {
                countOnSameStreet++
                val numMatch = Regex("\\d+").find(post.nome)
                if (numMatch != null) {
                    val num = numMatch.value.toIntOrNull() ?: 0
                    if (num > maxNumberOnStreet) {
                        maxNumberOnStreet = num
                    }
                }
            }
        }

        val nextNum = maxOf(countOnSameStreet + 1, maxNumberOnStreet + 1)
        return "Postazione $nextNum"
    }

    /**
     * Triggers evaluation of 10m threshold distance before saving.
     */
    fun onSaveClicked() {
        viewModelScope.launch {
            val loc = _uiState.value.locationData
            val nearby = repository.findNearbyPostazione(loc.latitude, loc.longitude, thresholdMeters = 10.0)

            if (nearby != null) {
                // <10m -> Existing station alert dialog
                _uiState.update {
                    it.copy(
                        pendingDialogType = ConfirmDialogType.NEARBY_EXISTING_STATION,
                        existingNearbyPostazione = nearby,
                        projectedStationName = nearby.nome
                    )
                }
            } else {
                val stationName = calculateProjectedStationName(loc.address)

                // >10m -> New station confirmation dialog
                _uiState.update {
                    it.copy(
                        pendingDialogType = ConfirmDialogType.NEW_STATION,
                        existingNearbyPostazione = null,
                        projectedStationName = stationName
                    )
                }
            }
        }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(pendingDialogType = null, existingNearbyPostazione = null) }
    }

    fun confirmSave() {
        viewModelScope.launch {
            val state = _uiState.value
            val loc = state.locationData
            val counts = state.binCounts

            val existing = state.existingNearbyPostazione
            if (existing != null) {
                // Update existing station
                val updated = existing.copy(
                    numIndifferenziato = counts[TipoRifiuto.INDIFFERENZIATO] ?: 0,
                    numCarta = counts[TipoRifiuto.CARTA] ?: 0,
                    numMultimateriale = counts[TipoRifiuto.MULTIMATERIALE] ?: 0,
                    numVetro = counts[TipoRifiuto.VETRO] ?: 0,
                    numOrganico = counts[TipoRifiuto.ORGANICO] ?: 0,
                    numRaee = counts[TipoRifiuto.RAEE] ?: 0,
                    numAbitiUsati = counts[TipoRifiuto.ABITI_USATI] ?: 0,
                    dataUltimoAggiornamento = System.currentTimeMillis()
                )
                repository.updatePostazione(updated)
            } else {
                val stationName = calculateProjectedStationName(loc.address)

                val newPostazione = Postazione(
                    nome = stationName,
                    indirizzo = loc.address,
                    latitudine = loc.latitude,
                    longitudine = loc.longitude,
                    numIndifferenziato = counts[TipoRifiuto.INDIFFERENZIATO] ?: 0,
                    numCarta = counts[TipoRifiuto.CARTA] ?: 0,
                    numMultimateriale = counts[TipoRifiuto.MULTIMATERIALE] ?: 0,
                    numVetro = counts[TipoRifiuto.VETRO] ?: 0,
                    numOrganico = counts[TipoRifiuto.ORGANICO] ?: 0,
                    numRaee = counts[TipoRifiuto.RAEE] ?: 0,
                    numAbitiUsati = counts[TipoRifiuto.ABITI_USATI] ?: 0,
                    dataUltimoAggiornamento = System.currentTimeMillis()
                )
                repository.savePostazione(newPostazione)
            }

            _uiState.update { it.copy(pendingDialogType = null, isSavedSuccess = true) }
            _eventFlow.emit(AddPostazioneEvent.ShowToast("Posizione salvata con successo"))
            _eventFlow.emit(AddPostazioneEvent.SaveSuccess)
        }
    }
}
