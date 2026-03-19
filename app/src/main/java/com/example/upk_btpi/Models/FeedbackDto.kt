package com.example.upk_btpi.Models

import com.google.gson.annotations.SerializedName

data class FeedbackDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("comment")
    val comment: String? = null,

    @SerializedName("raiting")
    val rating: Int = 0,

    @SerializedName("imagePath")
    val imagePath: String? = null
)
