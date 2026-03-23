package com.example.upk_btpi.Models.Ypk

import com.google.gson.annotations.SerializedName

data class CreateYpkDto(
    @SerializedName("yplName")
    val ypkName: String
)
