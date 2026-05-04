package com.simo3000.imieicompiti.data.repository

import com.simo3000.imieicompiti.data.api.*

class TaskRepository(
    private val api: ApiService,
    private val token: String
) {

    suspend fun getTasks(): Result<List<Task>> {
        return try {
            val response = api.getTasks(token)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Impossibile caricare i compiti."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }

    suspend fun createTask(
        date: String,
        subject: String,
        category: String,
        description: String
    ): Result<Task> {
        return try {
            val response = api.createTask(
                token,
                CreateTaskRequest(date, subject, category, description)
            )
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Errore nel salvataggio."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }

    suspend fun updateTask(
        id: String,
        date: String,
        subject: String,
        category: String,
        description: String
    ): Result<Task> {
        return try {
            val response = api.updateTask(
                token, id,
                UpdateTaskRequest(date, subject, category, description)
            )
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Errore nell'aggiornamento."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }

    suspend fun toggleTask(id: String, completed: Boolean): Result<Task> {
        return try {
            val response = api.toggleTask(token, id, ToggleTaskRequest(completed))
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Errore nell'aggiornamento."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }

    suspend fun deleteTask(id: String): Result<Unit> {
        return try {
            val response = api.deleteTask(token, id)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Errore nell'eliminazione."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }
}