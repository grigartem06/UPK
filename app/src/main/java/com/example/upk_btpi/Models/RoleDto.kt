package com.example.upk_btpi.Models

import com.google.gson.annotations.SerializedName

data class RoleDto (
    @SerializedName("id")
    val id: String,

    @SerializedName("roleName")
    val roleName: String? = null
)
