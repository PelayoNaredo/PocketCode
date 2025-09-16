package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import javax.inject.Inject

class GetAssetDetailsUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(assetId: String): Result<Asset> {
        return repository.getAssetDetails(assetId)
    }
}
