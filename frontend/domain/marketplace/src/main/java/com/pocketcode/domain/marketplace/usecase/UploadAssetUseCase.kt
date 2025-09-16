package com.pocketcode.domain.marketplace.usecase

import com.pocketcode.domain.marketplace.repository.MarketplaceRepository
import java.io.File
import javax.inject.Inject

class UploadAssetUseCase @Inject constructor(
    private val repository: MarketplaceRepository
) {
    suspend operator fun invoke(name: String, description: String, authorId: String, file: File): Result<Unit> {
        return repository.uploadAsset(name, description, authorId, file)
    }
}
