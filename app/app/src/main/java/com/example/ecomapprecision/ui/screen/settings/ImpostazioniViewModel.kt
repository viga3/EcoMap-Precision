package com.example.ecomapprecision.ui.screen.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecomapprecision.data.db.EcoMapDatabase
import com.example.ecomapprecision.data.repository.ImportResult
import com.example.ecomapprecision.data.repository.PostazioneRepository
import com.example.ecomapprecision.util.BackupUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImpostazioniViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostazioneRepository

    init {
        val dao = EcoMapDatabase.getDatabase(application).postazioneDao()
        repository = PostazioneRepository(dao)
    }

    fun exportBackup(uri: Uri, onResult: (Boolean, Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repository.getAllPostazioniSnapshot()
            val success = BackupUtils.exportBackup(getApplication(), uri, list)
            withContext(Dispatchers.Main) {
                onResult(success, list.size)
            }
        }
    }

    fun importBackup(uri: Uri, onResult: (Boolean, ImportResult?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val importedList = BackupUtils.importBackup(getApplication(), uri)
            if (importedList != null) {
                val result = repository.importPostazioni(importedList)
                withContext(Dispatchers.Main) {
                    onResult(true, result)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onResult(false, null)
                }
            }
        }
    }

    fun deleteAllPostazioni(onResult: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = repository.deleteAllPostazioni()
            withContext(Dispatchers.Main) {
                onResult(count)
            }
        }
    }
}
