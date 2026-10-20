package com.example.ecomapprecision.ui.screen.storico

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomapprecision.data.db.EcoMapDatabase
import com.example.ecomapprecision.data.model.Postazione
import com.example.ecomapprecision.data.repository.PostazioneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class StoricoUiState(
    val postazioni: List<Postazione> = emptyList(),
    val searchQuery: String = "",
    val totalBins: Int = 0,
    val totalRilevamenti: Int = 0
)

class StoricoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostazioneRepository
    private val _searchQuery = MutableStateFlow("")

    init {
        val dao = EcoMapDatabase.getDatabase(application).postazioneDao()
        repository = PostazioneRepository(dao)
    }

    val uiState: StateFlow<StoricoUiState> = combine(
        repository.allPostazioni,
        _searchQuery
    ) { list, query ->
        val filtered = if (query.isBlank()) {
            list
        } else {
            list.filter { p ->
                p.indirizzo.contains(query, ignoreCase = true) ||
                p.nome.contains(query, ignoreCase = true)
            }
        }

        val binsSum = list.sumOf { it.totaleBins() }

        StoricoUiState(
            postazioni = filtered,
            searchQuery = query,
            totalBins = binsSum,
            totalRilevamenti = list.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoricoUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun updatePostazione(postazione: Postazione) {
        viewModelScope.launch {
            repository.updatePostazione(postazione)
        }
    }

    fun deletePostazione(postazione: Postazione) {
        viewModelScope.launch {
            repository.deletePostazione(postazione)
        }
    }
}
