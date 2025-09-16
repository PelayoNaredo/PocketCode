package com.pocketcode.domain.marketplace.repository

import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.model.Review
import java.io.File

interface MarketplaceRepository {

    suspend fun getAssets(): Result<List<Asset>>

    suspend fun getAssetDetails(assetId: String): Result<Asset>

    suspend fun getAssetReviews(assetId: String): Result<List<Review>>

    suspend fun uploadAsset(name: String, description: String, authorId: String, file: File): Result<Unit>

    suspend fun addReview(assetId: String, userId: String, comment: String): Result<Unit>

    suspend fun addRating(assetId: String, userId: String, value: Int): Result<Unit>
}
