package com.pocketcode.data.marketplace.remote.api

import com.pocketcode.data.marketplace.remote.dto.MarketplaceInteractionMetricsRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface MarketplaceAnalyticsApiService {

    @POST("analytics/marketplace/interactions")
    suspend fun uploadInteractionSnapshot(
        @Body payload: MarketplaceInteractionMetricsRequest
    )
}
