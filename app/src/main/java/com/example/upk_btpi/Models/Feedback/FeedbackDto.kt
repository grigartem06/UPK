package com.example.upk_btpi.Models.Feedback

import com.google.gson.annotations.SerializedName

data class FeedbackDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("comment")
    val comment: String? = null,

    @SerializedName("imagePath")
    val imagePath: String? = null,

    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("raiting")
    val rating: Double? = null
)