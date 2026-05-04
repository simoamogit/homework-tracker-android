package com.simo3000.imieicompiti.data.repository

import com.simo3000.imieicompiti.data.api.ApiService
import com.simo3000.imieicompiti.data.api.AuthResponse
import com.simo3000.imieicompiti.data.api.LoginRequest
import com.simo3000.imieicompiti.data.api.RegisterRequest
import com.simo3000.imieicompiti.data.local.TokenStore

class AuthRepository(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenStore.saveToken(body.token)
                tokenStore.saveEmail(body.user.email)
                Result.success(body)
            } else {
                Result.failure(Exception("Credenziali non valide."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete. Controlla la connessione."))
        }
    }

    suspend fun register(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.register(RegisterRequest(email, password))
            if (response.isSuccessful) {
                val body = response.body()!!
                tokenStore.saveToken(body.token)
                tokenStore.saveEmail(body.user.email)
                Result.success(body)
            } else {
                Result.failure(Exception("Email già registrata o dati non validi."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Errore di rete. Controlla la connessione."))
        }
    }

    fun logout()      = tokenStore.clear()
    fun isLoggedIn()  = tokenStore.isLoggedIn()
    fun getToken()    = "Bearer ${tokenStore.getToken()}"
    fun getEmail()    = tokenStore.getEmail() ?: ""
}