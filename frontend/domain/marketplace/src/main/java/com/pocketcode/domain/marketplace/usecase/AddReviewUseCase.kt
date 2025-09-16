package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import javax.inject.Inject

class AddReviewUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(assetId: String, userId: String, comment: String): Result<Unit> {
        return repository.addReview(assetId, userId, comment)
    }
}
