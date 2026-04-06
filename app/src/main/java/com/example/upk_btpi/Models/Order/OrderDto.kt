package com.example.upk_btpi.Models.Order

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("executorId")
    val executorId: String? = null,

    @SerializedName("customerId")
    val customerId: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("customersComment")
    val customersComment: String? = null,

    @SerializedName("userComment")
    val userComment: String? = null,

//    @SerializedName("user")
//    val user: UserOrderDto? = null,
//
//    @SerializedName("feedbacks")
//    val feedbacks: List<FeedbackDto>? = null,
//
//    @SerializedName("product")
//    val product: ProductOrderDto? = null,
//
//    @SerializedName("statusOrder")
//    val statusOrder: StatusOrderDto? = null
)