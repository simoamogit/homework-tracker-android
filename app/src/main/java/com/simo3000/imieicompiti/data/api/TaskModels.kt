package com.simo3000.imieicompiti.data.api

data class Task(
    val id: String,
    val user_id: String,
    val date: String,
    val subject: String,
    val category: String,
    val description: String,
    val completed: Boolean,
    val created_at: String
)

data class CreateTaskRequest(
    val date: String,
    val subject: String,
    val category: String,
    val description: String
)

data class UpdateTaskRequest(
    val date: String,
    val subject: String,
    val category: String,
    val description: String
)

data class ToggleTaskRequest(
    val completed: Boolean
)

data class DeleteTaskResponse(
    val message: String,
    val id: String
)