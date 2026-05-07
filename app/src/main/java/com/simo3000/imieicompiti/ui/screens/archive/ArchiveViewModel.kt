package com.simo3000.imieicompiti.ui.screens.archive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.simo3000.imieicompiti.data.api.RetrofitClient
import com.simo3000.imieicompiti.data.api.Task
import com.simo3000.imieicompiti.data.local.TokenStore
import com.simo3000.imieicompiti.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ArchiveUiState(
    val isLoading: Boolean  = true,
    val error: String?      = null,
    val tasks: List<Task>   = emptyList(),
    val searchQuery: String = ""
)

class ArchiveViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val repository = TaskRepository(
        RetrofitClient.api,
        "Bearer ${tokenStore.getToken()}"
    )

    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init { loadTasks() }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getTasks()
                .onSuccess { tasks ->
                    _uiState.update { it.copy(isLoading = false, tasks = tasks) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleTask(taskId, completed)
                .onSuccess { updated ->
                    _uiState.update { state ->
                        state.copy(tasks = state.tasks.map { if (it.id == taskId) updated else it })
                    }
                }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(tasks = state.tasks.filter { it.id != taskId })
                    }
                }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}