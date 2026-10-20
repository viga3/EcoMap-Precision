package com.example.ecomapprecision.ui.screen.segnalazione

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.ecomapprecision.data.db.EcoMapDatabase
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.data.model.Segnalazione
import com.example.ecomapprecision.data.model.StatoSegnalazione
import com.example.ecomapprecision.data.model.TipoSegnalazione
import com.example.ecomapprecision.data.repository.PostazioneRepository
import com.example.ecomapprecision.data.repository.SegnalazioneRepository
import com.example.ecomapprecision.location.LocationResultData
import com.example.ecomapprecision.location.LocationService
import com.example.ecomapprecision.util.DistanceCalculator
import com.example.ecomapprecision.util.GeocodingUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SegnalazioneUiState(
    val locationData: LocationResultData = LocationResultData(0.0, 0.0, "Acquisizione posizione...", isRealFix = false),
    val gpsLocationData: LocationResultData? = null,
    val selectedPostazione: Postazione? = null,
    val nearestPostazione: Postazione? = null,
    val distanceToSelectedPostazioneMeters: Double? = null,
    val isPostazioneAgganciata: Boolean = false,
    val isManualLocation: Boolean = false,
    val allPostazioni: List<Postazione> = emptyList(),
    val selectedTipo: TipoSegnalazione? = TipoSegnalazione.ABBANDONI,
    val photoPath: String? = null,
    val note: String = "",
    val bomboleRossa: Int = 0,
    val bomboleBlu: Int = 0,
    val bomboleGrigia: Int = 0,
    val bomboleVerde: Int = 0,
    val cassonettoBloccatoTipo: String? = null
)

sealed class SegnalazioneEvent {
    object SubmitSuccess : SegnalazioneEvent()
    data class ShowToast(val message: String) : SegnalazioneEvent()
}

class SegnalazioneViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle? = null
) : AndroidViewModel(application) {
    private val segnalazioneRepository: SegnalazioneRepository
    private val postazioneRepository: PostazioneRepository
    private val locationService = LocationService(application)

    private val _uiState = MutableStateFlow(SegnalazioneUiState())
    val uiState: StateFlow<SegnalazioneUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SegnalazioneEvent>()
    val eventFlow: SharedFlow<SegnalazioneEvent> = _eventFlow.asSharedFlow()

    init {
        val db = EcoMapDatabase.getDatabase(application)
        segnalazioneRepository = SegnalazioneRepository(db.segnalazioneDao())
        postazioneRepository = PostazioneRepository(db.postazioneDao())

        // Stream all postazioni to keep UI updated
        viewModelScope.launch {
            postazioneRepository.allPostazioni.collect { postazioni ->
                _uiState.update { it.copy(allPostazioni = postazioni) }
                val gps = _uiState.value.gpsLocationData
                if (gps != null && !_uiState.value.isManualLocation) {
                    attachNearestPostazione(gps, postazioni)
                }
            }
        }

        val latArg = savedStateHandle?.get<String>("lat")?.toDoubleOrNull()
        val lngArg = savedStateHandle?.get<String>("lng")?.toDoubleOrNull()

        if (latArg != null && lngArg != null && latArg != 0.0 && lngArg != 0.0) {
            initFromCoordinates(latArg, lngArg)
        } else {
            refreshLocation()
        }
    }

    private fun initFromCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch {
            val addr = withContext(Dispatchers.IO) {
                GeocodingUtils.reverseGeocode(getApplication(), lat, lng)
            }
            val loc = LocationResultData(lat, lng, addr, 3.0f, true)
            _uiState.update { it.copy(gpsLocationData = loc) }
            val postazioni = postazioneRepository.getAllPostazioniSnapshot()
            attachNearestPostazione(loc, postazioni)
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val lastKnown = locationService.getLastKnownLocation()
            if (lastKnown != null && lastKnown.isRealFix) {
                _uiState.update { it.copy(gpsLocationData = lastKnown) }
                val postazioni = postazioneRepository.getAllPostazioniSnapshot()
                attachNearestPostazione(lastKnown, postazioni)
            }

            val cur = locationService.getCurrentLocation()
            if (cur.isRealFix) {
                _uiState.update { it.copy(gpsLocationData = cur) }
                val postazioni = postazioneRepository.getAllPostazioniSnapshot()
                attachNearestPostazione(cur, postazioni)
            }
        }
    }

    private fun attachNearestPostazione(userLoc: LocationResultData, postazioni: List<Postazione>) {
        if (postazioni.isEmpty()) {
            _uiState.update {
                it.copy(
                    locationData = userLoc,
                    selectedPostazione = null,
                    nearestPostazione = null,
                    distanceToSelectedPostazioneMeters = null,
                    isPostazioneAgganciata = false
                )
            }
            return
        }

        var closest: Postazione? = null
        var minDistance = Double.MAX_VALUE

        for (p in postazioni) {
            val dist = DistanceCalculator.calculateDistanceMeters(
                userLoc.latitude, userLoc.longitude,
                p.latitudine, p.longitudine
            )
            if (dist < minDistance) {
                minDistance = dist
                closest = p
            }
        }

        if (closest != null) {
            _uiState.update {
                it.copy(
                    selectedPostazione = closest,
                    nearestPostazione = closest,
                    distanceToSelectedPostazioneMeters = minDistance,
                    isPostazioneAgganciata = true,
                    isManualLocation = false,
                    locationData = LocationResultData(
                        latitude = closest.latitudine,
                        longitude = closest.longitudine,
                        address = "${closest.nome} • ${closest.indirizzo}",
                        accuracyMeters = userLoc.accuracyMeters,
                        isRealFix = true
                    )
                )
            }
        }
    }

    fun selectPostazione(postazione: Postazione) {
        val gps = _uiState.value.gpsLocationData
        val dist = if (gps != null && gps.isRealFix) {
            DistanceCalculator.calculateDistanceMeters(
                gps.latitude, gps.longitude,
                postazione.latitudine, postazione.longitudine
            )
        } else null

        _uiState.update {
            it.copy(
                selectedPostazione = postazione,
                distanceToSelectedPostazioneMeters = dist,
                isPostazioneAgganciata = false,
                isManualLocation = true,
                locationData = LocationResultData(
                    latitude = postazione.latitudine,
                    longitude = postazione.longitudine,
                    address = "${postazione.nome} • ${postazione.indirizzo}",
                    accuracyMeters = gps?.accuracyMeters ?: 3.0f,
                    isRealFix = true
                )
            )
        }
    }

    fun resetToCurrentGps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isManualLocation = false) }
            val gps = _uiState.value.gpsLocationData
            val postazioni = _uiState.value.allPostazioni
            if (gps != null && gps.isRealFix) {
                attachNearestPostazione(gps, postazioni)
            } else {
                val cur = locationService.getCurrentLocation()
                if (cur.isRealFix) {
                    _uiState.update { it.copy(gpsLocationData = cur) }
                    attachNearestPostazione(cur, postazioni)
                }
            }
        }
    }

    fun setManualLocation(lat: Double, lng: Double, address: String) {
        val gps = _uiState.value.gpsLocationData
        val dist = if (gps != null && gps.isRealFix) {
            DistanceCalculator.calculateDistanceMeters(gps.latitude, gps.longitude, lat, lng)
        } else null

        _uiState.update {
            it.copy(
                selectedPostazione = null,
                distanceToSelectedPostazioneMeters = dist,
                isPostazioneAgganciata = false,
                isManualLocation = true,
                locationData = LocationResultData(lat, lng, address, 5.0f, true)
            )
        }
    }

    fun searchAddress(query: String, onResult: (Boolean) -> Unit) {
        if (query.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val currentLoc = _uiState.value.locationData
            val coords = GeocodingUtils.searchAddress(
                context = getApplication(),
                query = query,
                currentLat = currentLoc.latitude,
                currentLon = currentLoc.longitude,
                currentAddress = currentLoc.address
            )
            if (coords != null) {
                setManualLocation(coords.first, coords.second, query)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun selectTipo(tipo: TipoSegnalazione) {
        _uiState.update { it.copy(selectedTipo = tipo) }
    }

    fun onNoteChange(noteText: String) {
        _uiState.update { it.copy(note = noteText) }
    }

    fun setPhotoPath(path: String?) {
        _uiState.update { it.copy(photoPath = path) }
    }

    fun incrementBombole(colore: String) {
        _uiState.update { state ->
            when (colore.lowercase()) {
                "rossa" -> state.copy(bomboleRossa = state.bomboleRossa + 1)
                "blu" -> state.copy(bomboleBlu = state.bomboleBlu + 1)
                "grigia" -> state.copy(bomboleGrigia = state.bomboleGrigia + 1)
                "verde" -> state.copy(bomboleVerde = state.bomboleVerde + 1)
                else -> state
            }
        }
    }

    fun decrementBombole(colore: String) {
        _uiState.update { state ->
            when (colore.lowercase()) {
                "rossa" -> if (state.bomboleRossa > 0) state.copy(bomboleRossa = state.bomboleRossa - 1) else state
                "blu" -> if (state.bomboleBlu > 0) state.copy(bomboleBlu = state.bomboleBlu - 1) else state
                "grigia" -> if (state.bomboleGrigia > 0) state.copy(bomboleGrigia = state.bomboleGrigia - 1) else state
                "verde" -> if (state.bomboleVerde > 0) state.copy(bomboleVerde = state.bomboleVerde - 1) else state
                else -> state
            }
        }
    }

    fun setCassonettoBloccatoTipo(tipo: String) {
        _uiState.update { it.copy(cassonettoBloccatoTipo = tipo) }
    }

    fun submitSegnalazione() {
        viewModelScope.launch {
            val state = _uiState.value
            val tipo = state.selectedTipo ?: TipoSegnalazione.ABBANDONI
            val loc = state.locationData
            val postazione = state.selectedPostazione

            val noteExtra = mutableListOf<String>()
            if (postazione != null) {
                noteExtra.add("Postazione associata: ${postazione.nome}")
            }
            if (tipo == TipoSegnalazione.BOMBOLE) {
                val bomboleList = mutableListOf<String>()
                if (state.bomboleRossa > 0) bomboleList.add("Rossa: ${state.bomboleRossa}")
                if (state.bomboleBlu > 0) bomboleList.add("Blu: ${state.bomboleBlu}")
                if (state.bomboleGrigia > 0) bomboleList.add("Grigia: ${state.bomboleGrigia}")
                if (state.bomboleVerde > 0) bomboleList.add("Verde: ${state.bomboleVerde}")
                if (bomboleList.isNotEmpty()) {
                    noteExtra.add("Bombole [${bomboleList.joinToString(", ")}]")
                }
            } else if (tipo == TipoSegnalazione.CASSONETTO_BLOCCATO) {
                state.cassonettoBloccatoTipo?.let {
                    noteExtra.add("Cassonetto: $it")
                }
            }

            val finalNotes = listOfNotNull(
                if (noteExtra.isNotEmpty()) noteExtra.joinToString(" • ") else null,
                state.note.ifBlank { null }
            ).joinToString("\n")

            val nuovaSegnalazione = Segnalazione(
                tipo = tipo,
                stato = StatoSegnalazione.NUOVO,
                indirizzo = if (postazione != null) "${postazione.nome} • ${postazione.indirizzo}" else loc.address,
                latitudine = loc.latitude,
                longitudine = loc.longitude,
                fotoPath = state.photoPath,
                note = finalNotes.ifBlank { null },
                bomboleRossa = state.bomboleRossa,
                bomboleBlu = state.bomboleBlu,
                bomboleGrigia = state.bomboleGrigia,
                bomboleVerde = state.bomboleVerde,
                cassonettoBloccatoTipo = state.cassonettoBloccatoTipo
            )

            segnalazioneRepository.saveSegnalazione(nuovaSegnalazione)
            _eventFlow.emit(SegnalazioneEvent.ShowToast("Segnalazione inviata con successo"))
            _eventFlow.emit(SegnalazioneEvent.SubmitSuccess)
        }
    }
}
