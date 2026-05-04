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
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean       = true,
    val error: String?           = null,
    val tasks: List<Task>        = emptyList(),
    val hideCompletedDays: Boolean = false,
    val searchQuery: String      = ""
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenStore = TokenStore(application)
    private val repository = TaskRepository(
        RetrofitClient.api,
        "Bearer ${tokenStore.getToken()}"
    )

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = repository.getTasks()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isLoading = false, tasks = result.getOrDefault(emptyList()))
            } else {
                _uiState.value.copy(isLoading = false, error = result.exceptionOrNull()?.message)
            }
        }
    }

    fun toggleTask(taskId: String, completed: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleTask(taskId, completed)
            if (result.isSuccess) {
                val updated = result.getOrNull()!!
                _uiState.value = _uiState.value.copy(
                    tasks = _uiState.value.tasks.map {
                        if (it.id == taskId) updated else it
                    }
                )
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val result = repository.deleteTask(taskId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    tasks = _uiState.value.tasks.filter { it.id != taskId }
                )
            }
        }
    }

    fun onTaskAdded(task: Task) {
        _uiState.value = _uiState.value.copy(
            tasks = _uiState.value.tasks + task
        )
    }

    fun onTaskUpdated(task: Task) {
        _uiState.value = _uiState.value.copy(
            tasks = _uiState.value.tasks.map { if (it.id == task.id) task else it }
        )
    }

    fun toggleHideCompletedDays() {
        _uiState.value = _uiState.value.copy(
            hideCompletedDays = !_uiState.value.hideCompletedDays
        )
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun getEmail() = tokenStore.getEmail()
}