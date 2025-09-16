package com.pocketcode.features.marketplace.ui.detail

import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.model.Review

data class MarketplaceDetailState(
    val isLoading: Boolean = false,
    val asset: Asset? = null,
    val reviews: List<Review> = emptyList(),
    val error: String? = null
)
