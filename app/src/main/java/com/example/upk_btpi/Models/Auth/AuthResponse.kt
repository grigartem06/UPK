package com.example.upk_btpi.Models.Auth

import com.google.gson.annotations.SerializedName

// Ответ от сервера
data class AuthResponse(
    @SerializedName("accessToken")
    val token: String?,
    @SerializedName("unique_name")
    val name: String?,
    @SerializedName("Id")
    val userId: Int? = null,
    @SerializedName("fullName")
    val fullName: String? = null,
    @SerializedName("message")
    val message: String?= null,
)