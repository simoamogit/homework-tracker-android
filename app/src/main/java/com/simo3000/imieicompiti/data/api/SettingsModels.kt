package com.simo3000.imieicompiti.data.api

data class Settings(
    val id: String,
    val user_id: String,
    val subjects: List<String>,
    val categories: List<String>,
    val updated_at: String
)

data class UpdateSettingsRequest(
    val subjects: List<String>,
    val categories: List<String>
)