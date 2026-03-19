package com.example.upk_btpi.Models

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id")
    val id: String,

    @SerializedName("fullname")
    val fullName: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("userInfo")
    val userInfo: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean = false,

    @SerializedName("role")
    val role: RoleDto? = null
)
