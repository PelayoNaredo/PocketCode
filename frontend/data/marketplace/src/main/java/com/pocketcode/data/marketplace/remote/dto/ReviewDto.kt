package com.pocketcode.data.marketplace.remote.dto

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("comment") val comment: String,
    @SerializedName("createdAt") val createdAt: String
)
