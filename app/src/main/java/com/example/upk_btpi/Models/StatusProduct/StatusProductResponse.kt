package com.example.upk_btpi.Models.StatusProduct

import com.google.gson.annotations.SerializedName

data class StatusProductResponse(
    @SerializedName("statusOrders")
     val statusOrders: List<statusProductDto> = emptyList()
)
