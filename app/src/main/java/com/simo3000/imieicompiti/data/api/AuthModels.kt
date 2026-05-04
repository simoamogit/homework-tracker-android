package com.simo3000.imieicompiti.data.api

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String
)

data class AuthUser(
    val id: String,
    val email: String
)

data class AuthResponse(
    val token: String,
    val user: AuthUser
)

data class ErrorResponse(
    val error: String
)