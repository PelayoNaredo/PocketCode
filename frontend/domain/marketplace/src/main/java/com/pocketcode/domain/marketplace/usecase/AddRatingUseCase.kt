package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import javax.inject.Inject

class AddRatingUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(assetId: String, userId: String, value: Int): Result<Unit> {
        // Basic validation can be done here
        if (value < 1 || value > 5) {
            return Result.failure(IllegalArgumentException("Rating must be between 1 and 5"))
        }
        return repository.addRating(assetId, userId, value)
    }
}
