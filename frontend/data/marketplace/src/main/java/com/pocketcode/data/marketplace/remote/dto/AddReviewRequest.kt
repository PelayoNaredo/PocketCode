package com.pocketcode.data.marketplace.remote.dto

import com.google.gson.annotations.SerializedName

data class AddReviewRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("comment") val comment: String
)
