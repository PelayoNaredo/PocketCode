package com.pocketcode.data.marketplace.remote.api

import com.pocketcode.data.marketplace.remote.dto.AssetDto
import com.pocketcode.data.marketplace.remote.dto.AddRatingRequest
import com.pocketcode.data.marketplace.remote.dto.AddReviewRequest
import com.pocketcode.data.marketplace.remote.dto.ReviewDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MarketplaceApiService {

    @GET("assets")
    suspend fun getAssets(): List<AssetDto>

    @GET("assets/{assetId}")
    suspend fun getAssetDetails(@Path("assetId") assetId: String): AssetDto

    @GET("assets/{assetId}/reviews")
    suspend fun getAssetReviews(@Path("assetId") assetId: String): List<ReviewDto>

    @Multipart
    @POST("assets")
    suspend fun uploadAsset(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("authorId") authorId: RequestBody,
        @Part assetFile: MultipartBody.Part
    ): AssetDto

    @POST("assets/{assetId}/reviews")
    suspend fun addReview(
        @Path("assetId") assetId: String,
        @Body reviewRequest: AddReviewRequest
    ): ReviewDto

    @POST("assets/{assetId}/ratings")
    suspend fun addRating(
        @Path("assetId") assetId: String,
        @Body ratingRequest: AddRatingRequest
    ) // This endpoint on the backend returns the new average, but we can simplify on client
}
