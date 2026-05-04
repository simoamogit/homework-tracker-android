package com.simo3000.imieicompiti.ui.screens.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simo3000.imieicompiti.data.api.RetrofitClient
import com.simo3000.imieicompiti.data.local.TokenStore
import com.simo3000.imieicompiti.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String?     = null,
    val success: Boolean   = false
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val repository = AuthRepository(RetrofitClient.api, tokenStore)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun isLoggedIn() = repository.isLoggedIn()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Email e password sono obbligatorie.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.login(email.trim(), password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(success = true)
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(email: String, password: String, confirm: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(error = "Email e password sono obbligatorie.")
            return
        }
        if (password != confirm) {
            _uiState.value = AuthUiState(error = "Le password non coincidono.")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState(error = "La password deve essere di almeno 6 caratteri.")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.register(email.trim(), password)
            _uiState.value = if (result.isSuccess) {
                AuthUiState(success = true)
            } else {
                AuthUiState(error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}