package com.example.upk_btpi.Models

import com.google.gson.annotations.SerializedName

data class UpdateUserForAdminDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("fullname")
    val fullname: String? = null,

    @SerializedName("phoneNumber")
    val phoneNumber: String? = null,

    @SerializedName("roleId")
    val roleId: String,

    @SerializedName("userInfo")
    val userInfo: String? = null
)
