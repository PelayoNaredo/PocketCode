package com.pocketcode.domain.marketplace.model

import java.util.Date

data class Asset(
    val id: String,
    val name: String,
    val description: String,
    val authorId: String,
    val filePath: String,
    val averageRating: Double,
    val ratingCount: Int,
    val createdAt: Date,
    val updatedAt: Date
)
