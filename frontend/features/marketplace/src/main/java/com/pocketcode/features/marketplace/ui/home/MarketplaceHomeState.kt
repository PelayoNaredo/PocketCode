package com.pocketcode.features.marketplace.ui.home

import com.pocketcode.domain.marketplace.model.Asset

data class MarketplaceHomeState(
    val isLoading: Boolean = false,
    val assets: List<Asset> = emptyList(),
    val error: String? = null
)
