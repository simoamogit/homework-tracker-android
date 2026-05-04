package com.simo3000.imieicompiti.data.api

import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    // Tasks
    @GET("tasks")
    suspend fun getTasks(@Header("Authorization") token: String): Response<List<Task>>

    @POST("tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Body request: CreateTaskRequest
    ): Response<Task>

    @PUT("tasks/{id}")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateTaskRequest
    ): Response<Task>

    @PATCH("tasks/{id}/complete")
    suspend fun toggleTask(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: ToggleTaskRequest
    ): Response<Task>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<DeleteTaskResponse>

    // Settings
    @GET("settings")
    suspend fun getSettings(@Header("Authorization") token: String): Response<Settings>

    @PUT("settings")
    suspend fun updateSettings(
        @Header("Authorization") token: String,
        @Body request: UpdateSettingsRequest
    ): Response<Settings>

    // Health
    @GET("health")
    suspend fun health(): Response<Unit>
}