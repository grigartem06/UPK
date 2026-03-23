package com.example.upk_btpi.Models.Feedback

import com.google.gson.annotations.SerializedName
import java.io.File

data class NewFeedbackDto(
    @SerializedName("FeedbackName")
    val FeedbackName: String,

    @SerializedName("Raiting")
    val Raiting: Int,

    @SerializedName("Image")
    val Image: File?= null
)
