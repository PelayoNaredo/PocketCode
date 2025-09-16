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

    private val assetId: String = savedStateHandle.get<String>("assetId")!!

    private val _uiState = MutableStateFlow(MarketplaceDetailState())
    val uiState: StateFlow<MarketplaceDetailState> = _uiState.asStateFlow()

    init {
        loadAssetDetails()
    }

    private fun loadAssetDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val detailsResult = getAssetDetailsUseCase(assetId)
            val reviewsResult = getAssetReviewsUseCase(assetId)

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
                    // Handle review loading failure separately if needed
                }

            _uiState.update { it.copy(isLoading = false, error = null) }
        }
    }
}
