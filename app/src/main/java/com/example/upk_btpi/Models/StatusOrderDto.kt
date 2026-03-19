package com.example.upk_btpi.Models

import com.google.gson.annotations.SerializedName

data class StatusOrderDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("statusName")
    val statusName: String? = null
)
