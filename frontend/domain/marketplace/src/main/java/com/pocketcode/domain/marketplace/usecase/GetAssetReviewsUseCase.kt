package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.model.Review
import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import javax.inject.Inject

class GetAssetReviewsUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(assetId: String): Result<List<Review>> {
        return repository.getAssetReviews(assetId)
    }
}
