package com.example.ecomapprecision.ui.screen.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomapprecision.data.db.EcoMapDatabase
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.data.repository.PostazioneRepository
import com.example.ecomapprecision.location.LocationResultData
import com.example.ecomapprecision.location.LocationService
import com.example.ecomapprecision.util.GeocodingUtils
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.google.android.gms.maps.model.CameraPosition

data class MapUiState(
    val postazioni: List<Postazione> = emptyList(),
    val selectedFilter: String? = null,
    val currentLocation: LocationResultData = LocationResultData(42.9248, 10.5342, "Acquisizione posizione GPS...", isRealFix = false),
    val hasRealLocation: Boolean = false,
    val selectedPostazione: Postazione? = null,
    val isAutoFollowing: Boolean = true
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostazioneRepository
    private val locationService = LocationService(application)

    var lastCameraPosition: CameraPosition? = null
    var hasInitiallyCentered: Boolean = false

    private val _selectedFilter = MutableStateFlow<String?>(null)
    private val _selectedPostazione = MutableStateFlow<Postazione?>(null)
    private val _currentLocation = MutableStateFlow(
        locationService.getFastCachedLocation() ?: LocationResultData(42.9248, 10.5342, "Acquisizione posizione GPS...", isRealFix = false)
    )
    private val _isAutoFollowing = MutableStateFlow(true)

    init {
        val dao = EcoMapDatabase.getDatabase(application).postazioneDao()
        repository = PostazioneRepository(dao)

        startLocationTracking()
    }

    private fun startLocationTracking() {
        viewModelScope.launch {
            val lastKnown = locationService.getLastKnownLocation()
            if (lastKnown != null && lastKnown.isRealFix) {
                _currentLocation.value = lastKnown
            } else {
                val cur = locationService.getCurrentLocation()
                if (cur.isRealFix) {
                    _currentLocation.value = cur
                }
            }
        }

        viewModelScope.launch {
            locationService.getLocationUpdates().collect { loc ->
                if (loc.isRealFix) {
                    _currentLocation.value = loc
                }
            }
        }
    }

    val currentLocation: StateFlow<LocationResultData> = _currentLocation
    val isAutoFollowing: StateFlow<Boolean> = _isAutoFollowing
    val selectedPostazione: StateFlow<Postazione?> = _selectedPostazione
    val selectedFilter: StateFlow<String?> = _selectedFilter

    val filteredPostazioni: StateFlow<List<Postazione>> = combine(
        repository.allPostazioni,
        _selectedFilter
    ) { postazioni, filter ->
        if (filter == null) {
            postazioni
        } else {
            postazioni.filter { p ->
                when (filter.lowercase()) {
                    "indifferenziato" -> p.numIndifferenziato > 0
                    "carta" -> p.numCarta > 0
                    "multimateriale", "giallo" -> p.numMultimateriale > 0
                    "vetro" -> p.numVetro > 0
                    "organico" -> p.numOrganico > 0
                    "raee" -> p.numRaee > 0
                    "abiti", "abiti_usati", "vestiti" -> p.numAbitiUsati > 0
                    else -> true
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState: StateFlow<MapUiState> = combine(
        filteredPostazioni,
        _selectedFilter,
        _currentLocation,
        _selectedPostazione,
        _isAutoFollowing
    ) { postazioni, filter, location, selected, autoFollow ->
        MapUiState(
            postazioni = postazioni,
            selectedFilter = filter,
            currentLocation = location,
            hasRealLocation = location.isRealFix,
            selectedPostazione = selected,
            isAutoFollowing = autoFollow
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MapUiState()
    )

    fun setAutoFollowing(enabled: Boolean) {
        _isAutoFollowing.value = enabled
    }

    fun onFilterSelected(filterId: String) {
        _selectedFilter.value = if (_selectedFilter.value == filterId) null else filterId
    }

    fun onPostazioneClick(postazione: Postazione) {
        _selectedPostazione.value = postazione
    }

    fun dismissPostazioneDetails() {
        _selectedPostazione.value = null
    }

    fun updatePostazione(postazione: Postazione) {
        viewModelScope.launch {
            repository.updatePostazione(postazione)
            _selectedPostazione.value = null
        }
    }

    fun deletePostazione(postazione: Postazione) {
        viewModelScope.launch {
            repository.deletePostazione(postazione)
            _selectedPostazione.value = null
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            val loc = locationService.getCurrentLocation()
            if (loc.isRealFix) {
                _currentLocation.value = loc
            }
        }
    }

    fun searchAddress(query: String, onResult: (Boolean) -> Unit) {
        if (query.isBlank()) {
            onResult(false)
            return
        }
        viewModelScope.launch {
            val currentLoc = _currentLocation.value
            val coords = GeocodingUtils.searchAddress(
                context = getApplication(),
                query = query,
                currentLat = currentLoc.latitude,
                currentLon = currentLoc.longitude,
                currentAddress = currentLoc.address
            )
            if (coords != null) {
                _currentLocation.value = LocationResultData(
                    latitude = coords.first,
                    longitude = coords.second,
                    address = query
                )
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    private suspend fun seedSampleData() {
        val p1 = Postazione(
            nome = "Postazione 1",
            indirizzo = "Corso Vittorio Emanuele, 12",
            latitudine = 42.9248,
            longitudine = 10.5342,
            numIndifferenziato = 1,
            numCarta = 2,
            numMultimateriale = 1
        )
        val p2 = Postazione(
            nome = "Postazione 2",
            indirizzo = "Via Amendola, 8",
            latitudine = 42.9265,
            longitudine = 10.5310,
            numIndifferenziato = 2,
            numVetro = 2,
            numOrganico = 1
        )
        val p3 = Postazione(
            nome = "Postazione 3",
            indirizzo = "Lungomare Marconi, 24",
            latitudine = 42.9220,
            longitudine = 10.5380,
            numCarta = 1,
            numMultimateriale = 4
        )
        repository.savePostazione(p1)
        repository.savePostazione(p2)
        repository.savePostazione(p3)
    }
}
