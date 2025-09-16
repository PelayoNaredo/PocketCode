package com.pocketcode.data.marketplace.remote.dto

import com.google.gson.annotations.SerializedName

data class AssetDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("authorId") val authorId: String,
    @SerializedName("filePath") val filePath: String,
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("ratingCount") val ratingCount: Int,
    @SerializedName("createdAt") val createdAt: String, // Dates are typically strings in JSON
    @SerializedName("updatedAt") val updatedAt: String
)
