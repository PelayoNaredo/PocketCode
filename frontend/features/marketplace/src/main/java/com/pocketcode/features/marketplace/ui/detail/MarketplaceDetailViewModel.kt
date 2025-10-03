package com.pocketcode.features.marketplace.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.marketplace.usecase.GetAssetDetailsUseCase
import com.pocketcode.domain.marketplace.usecase.GetAssetReviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketplaceDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAssetDetailsUseCase: GetAssetDetailsUseCase,
    private val getAssetReviewsUseCase: GetAssetReviewsUseCase
) : ViewModel() {

    private var currentAssetId: String = savedStateHandle.get<String>("assetId")!!

    private val _uiState = MutableStateFlow(MarketplaceDetailState())
    val uiState: StateFlow<MarketplaceDetailState> = _uiState.asStateFlow()

    init {
        loadAssetDetails(currentAssetId)
    }

    fun refreshAsset(assetId: String) {
        if (assetId.isBlank()) return
        if (assetId != currentAssetId) {
            currentAssetId = assetId
        }
        loadAssetDetails(assetId)
    }

    fun reloadCurrentAsset() {
        loadAssetDetails(currentAssetId)
    }

    private fun loadAssetDetails(assetId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val detailsResult = getAssetDetailsUseCase(assetId)
            val reviewsResult = getAssetReviewsUseCase(assetId)
            var errorMessage: String? = null

            detailsResult
                .onSuccess { asset ->
                    _uiState.update { it.copy(asset = asset) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                    return@launch
                }

            reviewsResult
                .onSuccess { reviews ->
                    _uiState.update { it.copy(reviews = reviews) }
                }
                .onFailure { error ->
                    errorMessage = error.message
                }

            _uiState.update { it.copy(isLoading = false, error = errorMessage) }
        }
    }
}
