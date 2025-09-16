package com.pocketcode.data.marketplace.remote.dto

import com.google.gson.annotations.SerializedName

data class AddRatingRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("value") val value: Int
)
