package com.pocketcode.features.marketplace.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.domain.marketplace.usecase.GetMarketplaceAssetsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketplaceHomeViewModel @Inject constructor(
    private val getMarketplaceAssetsUseCase: GetMarketplaceAssetsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceHomeState())
    val uiState: StateFlow<MarketplaceHomeState> = _uiState.asStateFlow()

    init {
        loadAssets()
    }

    fun loadAssets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMarketplaceAssetsUseCase()
                .onSuccess { assets ->
                    _uiState.update {
                        it.copy(isLoading = false, assets = assets, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }
}
