package com.simo3000.imieicompiti.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simo3000.imieicompiti.data.api.RetrofitClient
import com.simo3000.imieicompiti.data.local.TokenStore
import com.simo3000.imieicompiti.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean    = true,
    val isSaving: Boolean     = false,
    val error: String?        = null,
    val saveSuccess: Boolean  = false,
    val subjects: List<String>    = emptyList(),
    val categories: List<String>  = emptyList()
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val repository = SettingsRepository(
        RetrofitClient.api,
        "Bearer ${tokenStore.getToken()}"
    )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { loadSettings() }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getSettings()
                .onSuccess { settings ->
                    _uiState.update {
                        it.copy(
                            isLoading  = false,
                            subjects   = settings.subjects,
                            categories = settings.categories
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun addSubject(subject: String) {
        val s = subject.trim()
        if (s.isBlank() || _uiState.value.subjects.contains(s)) return
        _uiState.update { it.copy(subjects = it.subjects + s) }
    }

    fun removeSubject(subject: String) {
        _uiState.update { it.copy(subjects = it.subjects.filter { s -> s != subject }) }
    }

    fun addCategory(category: String) {
        val c = category.trim()
        if (c.isBlank() || _uiState.value.categories.contains(c)) return
        _uiState.update { it.copy(categories = it.categories + c) }
    }

    fun removeCategory(category: String) {
        _uiState.update { it.copy(categories = it.categories.filter { c -> c != category }) }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saveSuccess = false) }
            repository.updateSettings(
                _uiState.value.subjects,
                _uiState.value.categories
            )
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, error = error.message) }
                }
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
}