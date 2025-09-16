package com.pocketcode.domain.marketplace.model

import java.util.Date

data class Review(
    val id: String,
    val userId: String,
    val comment: String,
    val createdAt: Date
)
