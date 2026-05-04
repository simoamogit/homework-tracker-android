package com.simo3000.imieicompiti.data.repository

import com.simo3000.imieicompiti.data.api.ApiService
import com.simo3000.imieicompiti.data.api.Settings
import com.simo3000.imieicompiti.data.api.UpdateSettingsRequest

class SettingsRepository(
    private val api: ApiService,
    private val token: String
) {

    suspend fun getSettings(): Result<Settings> {
        return try {
            val response = api.getSettings(token)
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Impossibile caricare le impostazioni."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }

    suspend fun updateSettings(
        subjects: List<String>,
        categories: List<String>
    ): Result<Settings> {
        return try {
            val response = api.updateSettings(
                token,
                UpdateSettingsRequest(subjects, categories)
            )
            if (response.isSuccessful) Result.success(response.body()!!)
            else Result.failure(Exception("Errore nel salvataggio."))
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete."))
        }
    }
}