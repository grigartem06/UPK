package com.example.upk_btpi.Models.User

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    var id: String,
    @SerializedName("fullname")
    var fullname: String?=null,
    @SerializedName("hashPassword")
    var hashPassword: String?=null,
    @SerializedName("phoneNumber")
    var phoneNumber: String?= null,
    @SerializedName("userInfo")
    var userInfo: String? = null,
    @SerializedName("isActive")
    var isActive: Boolean
)
