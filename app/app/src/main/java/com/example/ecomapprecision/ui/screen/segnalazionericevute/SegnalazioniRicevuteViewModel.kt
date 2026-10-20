package com.example.ecomapprecision.ui.screen.segnalazionericevute

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomapprecision.data.db.EcoMapDatabase
import com.example.ecomapprecision.data.model.Segnalazione
import com.example.ecomapprecision.data.model.StatoSegnalazione
import com.example.ecomapprecision.data.model.TipoSegnalazione
import com.example.ecomapprecision.data.repository.SegnalazioneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SegnalazioniRicevuteUiState(
    val segnalazioni: List<Segnalazione> = emptyList(),
    val totalCount: Int = 0,
    val selectedFilterId: String = "tutte"
)

class SegnalazioniRicevuteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SegnalazioneRepository
    private val _selectedFilterId = MutableStateFlow("tutte")

    init {
        val dao = EcoMapDatabase.getDatabase(application).segnalazioneDao()
        repository = SegnalazioneRepository(dao)
    }

    val uiState: StateFlow<SegnalazioniRicevuteUiState> = combine(
        repository.allSegnalazioni,
        _selectedFilterId
    ) { list, filterId ->
        val filtered = when (filterId.lowercase()) {
            "abbandoni" -> list.filter { it.tipo == TipoSegnalazione.ABBANDONI }
            "cassonetti" -> list.filter { it.tipo == TipoSegnalazione.CASSONETTO_BLOCCATO }
            "ingombranti" -> list.filter { it.tipo == TipoSegnalazione.INGOMBRANTI }
            "bombole" -> list.filter { it.tipo == TipoSegnalazione.BOMBOLE }
            else -> list
        }

        SegnalazioniRicevuteUiState(
            segnalazioni = filtered,
            totalCount = list.size,
            selectedFilterId = filterId
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SegnalazioniRicevuteUiState()
    )

    fun onFilterSelected(filterId: String) {
        _selectedFilterId.value = filterId
    }

    fun updateStato(segnalazione: Segnalazione, nuovoStato: StatoSegnalazione) {
        viewModelScope.launch {
            val updated = segnalazione.copy(stato = nuovoStato)
            repository.updateSegnalazione(updated)
        }
    }

    fun deleteSegnalazione(segnalazione: Segnalazione) {
        viewModelScope.launch {
            repository.deleteSegnalazione(segnalazione)
        }
    }
}
