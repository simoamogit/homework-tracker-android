package com.simo3000.imieicompiti.ui.screens.dashboard

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

data class DashboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val tasks: List<Task> = emptyList(),
    val hideCompletedDays: Boolean = false,
    val searchQuery: String = "",
    val userEmail: String? = null
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val repository = TaskRepository(
        RetrofitClient.api,
        "Bearer ${tokenStore.getToken()}"
    )

    private val _uiState = MutableStateFlow(
        DashboardUiState(userEmail = tokenStore.getEmail())
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

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
                .onSuccess { updatedTask ->
                    _uiState.update { state ->
                        state.copy(
                            tasks = state.tasks.map { if (it.id == taskId) updatedTask else it }
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
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
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun createTask(date: String, subject: String, category: String, description: String) {
        viewModelScope.launch {
            repository.createTask(date, subject, category, description)
                .onSuccess { newTask ->
                    _uiState.update { it.copy(tasks = it.tasks + newTask) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun updateTask(id: String, date: String, subject: String, category: String, description: String) {
        viewModelScope.launch {
            repository.updateTask(id, date, subject, category, description)
                .onSuccess { updatedTask ->
                    _uiState.update { state ->
                        state.copy(tasks = state.tasks.map { if (it.id == id) updatedTask else it })
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun toggleHideCompletedDays() {
        _uiState.update { it.copy(hideCompletedDays = !it.hideCompletedDays) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getEmail() = tokenStore.getEmail()
}
