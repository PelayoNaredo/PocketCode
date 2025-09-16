package com.pocketcode.data.marketplace.repository

import com.pocketcode.data.marketplace.remote.api.MarketplaceApiService
import com.pocketcode.data.marketplace.remote.dto.AddRatingRequest
import com.pocketcode.data.marketplace.remote.dto.AddReviewRequest
import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.model.Review
import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MarketplaceRepositoryImpl @Inject constructor(
    private val apiService: MarketplaceApiService
) : MarketplaceRepository {

    // A simple parser, in a real app, this would be more robust.
    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)

    override suspend fun getAssets(): Result<List<Asset>> {
        return try {
            val assetDtos = apiService.getAssets()
            val assets = assetDtos.map { dto ->
                Asset(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    authorId = dto.authorId,
                    filePath = dto.filePath,
                    averageRating = dto.averageRating,
                    ratingCount = dto.ratingCount,
                    createdAt = isoParser.parse(dto.createdAt) ?: Date(),
                    updatedAt = isoParser.parse(dto.updatedAt) ?: Date()
                )
            }
            Result.success(assets)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssetDetails(assetId: String): Result<Asset> {
        return try {
            val dto = apiService.getAssetDetails(assetId)
            val asset = Asset(
                id = dto.id,
                name = dto.name,
                description = dto.description,
                authorId = dto.authorId,
                filePath = dto.filePath,
                averageRating = dto.averageRating,
                ratingCount = dto.ratingCount,
                createdAt = isoParser.parse(dto.createdAt) ?: Date(),
                updatedAt = isoParser.parse(dto.updatedAt) ?: Date()
            )
            Result.success(asset)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAssetReviews(assetId: String): Result<List<Review>> {
        return try {
            val reviewDtos = apiService.getAssetReviews(assetId)
            val reviews = reviewDtos.map { dto ->
                Review(
                    id = dto.id,
                    userId = dto.userId,
                    comment = dto.comment,
                    createdAt = isoParser.parse(dto.createdAt) ?: Date()
                )
            }
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uploadAsset(name: String, description: String, authorId: String, file: File): Result<Unit> {
        return try {
            apiService.uploadAsset(
                name = name.toRequestBody("text/plain".toMediaTypeOrNull()),
                description = description.toRequestBody("text/plain".toMediaTypeOrNull()),
                authorId = authorId.toRequestBody("text/plain".toMediaTypeOrNull()),
                assetFile = MultipartBody.Part.createFormData(
                    "assetFile",
                    file.name,
                    file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addReview(assetId: String, userId: String, comment: String): Result<Unit> {
        return try {
            val request = AddReviewRequest(userId, comment)
            apiService.addReview(assetId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRating(assetId: String, userId: String, value: Int): Result<Unit> {
        return try {
            val request = AddRatingRequest(userId, value)
            apiService.addRating(assetId, request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
