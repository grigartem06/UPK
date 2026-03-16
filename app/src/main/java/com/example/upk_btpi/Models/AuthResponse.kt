package com.example.upk_btpi.Models

// Ответ от сервера
 data class AuthResponse(
    val token: String?,
    val message: String?,
    val userId: Int? = null,
    val fullName: String? = null
)
