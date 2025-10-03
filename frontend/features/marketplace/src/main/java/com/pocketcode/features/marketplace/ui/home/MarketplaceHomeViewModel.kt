package com.pocketcode.features.marketplace.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics
import com.pocketcode.domain.marketplace.usecase.GetMarketplaceAssetsUseCase
import com.pocketcode.domain.marketplace.usecase.ObserveMarketplaceMetricsUseCase
import com.pocketcode.domain.marketplace.usecase.PersistMarketplaceMetricsUseCase
import com.pocketcode.domain.marketplace.usecase.ScheduleMarketplaceMetricsSyncUseCase
import com.pocketcode.domain.marketplace.usecase.UploadMarketplaceMetricsUseCase
import com.pocketcode.domain.marketplace.usecase.ValidateMarketplaceRecommendationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import com.pocketcode.core.ui.components.feedback.PocketToastStyle
import com.pocketcode.core.ui.snackbar.GlobalSnackbarOrigin
import com.pocketcode.core.ui.snackbar.GlobalToastDispatcher
import com.pocketcode.core.ui.components.feedback.PocketToastDuration

@HiltViewModel
class MarketplaceHomeViewModel @Inject constructor(
    private val getMarketplaceAssetsUseCase: GetMarketplaceAssetsUseCase,
    private val observeMarketplaceMetricsUseCase: ObserveMarketplaceMetricsUseCase,
    private val persistMarketplaceMetricsUseCase: PersistMarketplaceMetricsUseCase,
    private val uploadMarketplaceMetricsUseCase: UploadMarketplaceMetricsUseCase,
    private val scheduleMarketplaceMetricsSyncUseCase: ScheduleMarketplaceMetricsSyncUseCase,
    private val validateMarketplaceRecommendationsUseCase: ValidateMarketplaceRecommendationsUseCase,
    private val toastDispatcher: GlobalToastDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceHomeState())
    val uiState: StateFlow<MarketplaceHomeState> = _uiState.asStateFlow()

    private var lastEvaluatedRecommendationImpressions: Int = 0

    init {
        observeMetrics()
        loadAssets()
    }

    fun loadAssets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getMarketplaceAssetsUseCase()
                .onSuccess { assets ->
                    updateStateAndRecord { state ->
                        state.copy(
                            isLoading = false,
                            assets = assets,
                            error = null,
                            isOffline = false
                        ).applyFilters()
                    }
                    if (assets.isNotEmpty()) {
                        toastDispatcher.showMessage(
                            message = "${assets.size} recursos cargados",
                            style = PocketToastStyle.Success,
                            origin = GlobalSnackbarOrigin.MARKETPLACE,
                            duration = PocketToastDuration.Short
                        )
                    }
                }
                .onFailure { error ->
                    updateStateAndRecord { state ->
                        val isOfflineError = error is IOException
                        val updatedError = if (isOfflineError) null else error.message
                        state.copy(
                            isLoading = false,
                            error = updatedError,
                            isOffline = isOfflineError
                        ).applyFilters()
                    }
                    if (error !is IOException) {
                        toastDispatcher.showMessage(
                            message = "Error al cargar recursos",
                            style = PocketToastStyle.Error,
                            origin = GlobalSnackbarOrigin.MARKETPLACE,
                            duration = PocketToastDuration.Extended
                        )
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        updateStateAndRecord { state ->
            val shouldTrackInteraction = state.query != query
            state.copy(
                query = query,
                metrics = if (shouldTrackInteraction) {
                    state.metrics.copy(
                        searchInteractions = state.metrics.searchInteractions + 1
                    )
                } else {
                    state.metrics
                }
            ).applyFilters()
        }
    }

    fun onClearSearch() {
        updateStateAndRecord { state ->
            val shouldTrackInteraction = state.query.isNotBlank()
            state.copy(
                query = "",
                metrics = if (shouldTrackInteraction) {
                    state.metrics.copy(
                        searchInteractions = state.metrics.searchInteractions + 1
                    )
                } else {
                    state.metrics
                }
            ).applyFilters()
        }
    }

    fun onRatingFilterSelected(filter: MarketplaceRatingFilter) {
        updateStateAndRecord { state ->
            val shouldTrackInteraction = state.ratingFilter != filter
            state.copy(
                ratingFilter = filter,
                metrics = if (shouldTrackInteraction) {
                    state.metrics.copy(
                        filterSelections = state.metrics.filterSelections + 1
                    )
                } else {
                    state.metrics
                }
            ).applyFilters()
        }
    }

    fun clearFilters() {
        updateStateAndRecord { state ->
            val hadActiveFilters = state.hasActiveFilters
            state.copy(
                query = "",
                ratingFilter = MarketplaceRatingFilter.All,
                metrics = if (hadActiveFilters) {
                    state.metrics.copy(
                        filterSelections = state.metrics.filterSelections + 1
                    )
                } else {
                    state.metrics
                }
            ).applyFilters()
        }
    }

    fun onAssetSelected(assetId: String, fromRecommendations: Boolean = false) {
        updateStateAndRecord { state ->
            val assetName = state.assets.firstOrNull { it.id == assetId }?.name
            state.copy(
                metrics = state.metrics.copy(
                    assetOpens = state.metrics.assetOpens + 1,
                    recommendationOpens = state.metrics.recommendationOpens + if (fromRecommendations) 1 else 0,
                    lastOpenedAssetId = assetId,
                    lastOpenedAssetName = assetName ?: state.metrics.lastOpenedAssetName
                )
            ).applyFilters()
        }
    }

    fun onRecommendationsImpression(assetIds: List<String>) {
        if (assetIds.isEmpty()) return
        updateStateAndRecord { state ->
            val now = System.currentTimeMillis()
            val shouldSkip = assetIds == state.metrics.lastRecommendedAssetIds &&
                (now - state.metrics.lastRecommendationGeneratedAtMillis) < RECOMMENDATION_IMPRESSION_DEBOUNCE_MS
            if (shouldSkip) {
                state
            } else {
                state.copy(
                    metrics = state.metrics.copy(
                        recommendationImpressions = state.metrics.recommendationImpressions + assetIds.size,
                        lastRecommendedAssetIds = assetIds,
                        lastRecommendationGeneratedAtMillis = now
                    )
                )
            }
        }
    }

    fun retryLoad() {
        updateStateAndRecord { state ->
            state.copy(
                metrics = state.metrics.copy(
                    offlineRetries = state.metrics.offlineRetries + 1
                )
            )
        }
        loadAssets()
    }

    private fun observeMetrics() {
        viewModelScope.launch {
            observeMarketplaceMetricsUseCase().collect { snapshot ->
                updateStateAndRecord(shouldPersistSnapshot = false) { state ->
                    state.copy(metrics = snapshot.toUiModel()).applyFilters()
                }
            }
        }
    }

    private fun updateStateAndRecord(
        shouldPersistSnapshot: Boolean = true,
        transform: (MarketplaceHomeState) -> MarketplaceHomeState
    ) {
        var previousMetrics: MarketplaceMetrics? = null
        var updatedState: MarketplaceHomeState? = null
        _uiState.update { current ->
            previousMetrics = current.metrics
            transform(current).also { transformed ->
                updatedState = transformed
            }
        }

        val newMetrics = updatedState?.metrics
        if (shouldPersistSnapshot && updatedState != null && newMetrics != null && newMetrics != previousMetrics) {
            viewModelScope.launch {
                val snapshot = newMetrics.toInteractionSnapshot()
                persistMarketplaceMetricsUseCase(snapshot)
                val uploadResult = uploadMarketplaceMetricsUseCase(snapshot)
                if (uploadResult.isFailure) {
                    Log.w(TAG, "No se pudieron sincronizar las métricas del Marketplace", uploadResult.exceptionOrNull())
                    scheduleMarketplaceMetricsSyncUseCase()
                    toastDispatcher.showMessage(
                        message = "Métricas sincronizadas en segundo plano",
                        style = PocketToastStyle.Info,
                        origin = GlobalSnackbarOrigin.MARKETPLACE,
                        duration = PocketToastDuration.Short
                    )
                }

                val impressions = newMetrics.recommendationImpressions
                if (impressions >= MIN_RECOMMENDATION_SAMPLE && impressions != lastEvaluatedRecommendationImpressions) {
                    lastEvaluatedRecommendationImpressions = impressions
                    validateMarketplaceRecommendationsUseCase(snapshot)
                }
            }
        }
    }
}

private fun MarketplaceHomeState.applyFilters(): MarketplaceHomeState {
    val filtered = assets.filter { asset ->
        val matchesQuery = query.isBlank() ||
            asset.name.contains(query, ignoreCase = true) ||
            asset.description.contains(query, ignoreCase = true)
        val matchesRating = ratingFilter.minimumRating?.let { asset.averageRating >= it } ?: true
        matchesQuery && matchesRating
    }
        .sortedWith(compareByDescending<Asset> { it.averageRating }.thenBy { it.name })

    return copy(
        filteredAssets = filtered,
        metrics = metrics.copy(lastResultCount = filtered.size),
        recommendedAssets = computeRecommendations()
    )
}

private fun MarketplaceHomeState.computeRecommendations(): List<Asset> {
    if (assets.isEmpty()) return emptyList()

    val lastOpened = metrics.lastOpenedAssetId?.let { id -> assets.firstOrNull { it.id == id } }
    val lastOpenedId = lastOpened?.id
    val sameAuthorId = lastOpened?.authorId
    val maxRatingCount = assets.maxOfOrNull { it.ratingCount }?.takeIf { it > 0 } ?: 1
    val minUpdatedAt = assets.minOfOrNull { it.updatedAt.time } ?: 0L
    val maxUpdatedAt = assets.maxOfOrNull { it.updatedAt.time } ?: minUpdatedAt
    val recencyRange = (maxUpdatedAt - minUpdatedAt).takeIf { it > 0L } ?: 1L
    val previouslyRecommended = metrics.lastRecommendedAssetIds.toSet()
    val conversionRate = metrics.recommendationConversionRate

    fun score(asset: Asset): Double {
        val ratingScore = (asset.averageRating / MAX_RATING_VALUE).coerceIn(0.0, 1.0)
        val ratingCountScore = asset.ratingCount.toDouble() / maxRatingCount.toDouble()
        val normalizedRecency = ((asset.updatedAt.time - minUpdatedAt)
            .coerceIn(0L, recencyRange).toDouble() / recencyRange.toDouble())
    val authorBoost = if (sameAuthorId != null && asset.authorId == sameAuthorId && asset.id != lastOpenedId) {
            AUTHOR_MATCH_BOOST
        } else {
            0.0
        }
        val repetitionPenalty = when {
            !previouslyRecommended.contains(asset.id) -> 0.0
            conversionRate >= HIGH_CONVERSION_THRESHOLD -> 0.0
            conversionRate <= LOW_CONVERSION_THRESHOLD -> STRONG_REPETITION_PENALTY
            else -> REPETITION_PENALTY
        }

        return ratingScore * RATING_WEIGHT +
            ratingCountScore * RATING_COUNT_WEIGHT +
            normalizedRecency * RECENCY_WEIGHT +
            authorBoost +
            repetitionPenalty
    }

    val sortedCandidates = assets
        .filter { it.id != lastOpened?.id }
        .sortedWith(
            compareByDescending<Asset> { score(it) }
                .thenByDescending { it.ratingCount }
                .thenByDescending { it.averageRating }
                .thenBy { it.name }
        )

    val recommendations = mutableListOf<Asset>()
    val authorUsage = mutableMapOf<String, Int>()

    fun registerAuthor(asset: Asset) {
        authorUsage[asset.authorId] = authorUsage.getOrDefault(asset.authorId, 0) + 1
    }

    fun canAdd(asset: Asset): Boolean {
        return authorUsage.getOrDefault(asset.authorId, 0) < AUTHOR_REPETITION_LIMIT
    }

    if (lastOpened != null) {
        recommendations += lastOpened
        registerAuthor(lastOpened)
    }

    for (asset in sortedCandidates) {
        if (!canAdd(asset)) continue
        recommendations += asset
        registerAuthor(asset)
        if (recommendations.size >= RECOMMENDATION_LIMIT) break
    }

    return recommendations
}

private const val MAX_RATING_VALUE = 5.0
private const val RATING_WEIGHT = 0.5
private const val RATING_COUNT_WEIGHT = 0.2
private const val RECENCY_WEIGHT = 0.2
private const val AUTHOR_MATCH_BOOST = 0.15
private const val REPETITION_PENALTY = -0.05
private const val STRONG_REPETITION_PENALTY = -0.15
private const val AUTHOR_REPETITION_LIMIT = 2
private const val RECOMMENDATION_LIMIT = 5
private const val HIGH_CONVERSION_THRESHOLD = 0.25
private const val LOW_CONVERSION_THRESHOLD = 0.1
private const val MIN_RECOMMENDATION_SAMPLE = 20
private const val RECOMMENDATION_IMPRESSION_DEBOUNCE_MS = 2_000L
private const val TAG = "MarketplaceHomeVm"

private fun MarketplaceMetrics.toInteractionSnapshot(): com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics {
    return com.pocketcode.domain.marketplace.analytics.MarketplaceInteractionMetrics(
        searchInteractions = searchInteractions,
        filterSelections = filterSelections,
        assetOpens = assetOpens,
        offlineRetries = offlineRetries,
        lastResultCount = lastResultCount,
        lastOpenedAssetId = lastOpenedAssetId,
        lastOpenedAssetName = lastOpenedAssetName,
        recommendationImpressions = recommendationImpressions,
        recommendationOpens = recommendationOpens,
        lastRecommendedAssetIds = lastRecommendedAssetIds,
        lastRecommendationGeneratedAtMillis = lastRecommendationGeneratedAtMillis,
        lastUpdatedAtMillis = System.currentTimeMillis()
    )
}

private fun MarketplaceInteractionMetrics.toUiModel(): MarketplaceMetrics {
    return MarketplaceMetrics(
        searchInteractions = searchInteractions,
        filterSelections = filterSelections,
        assetOpens = assetOpens,
        offlineRetries = offlineRetries,
        lastResultCount = lastResultCount,
        lastOpenedAssetId = lastOpenedAssetId,
        lastOpenedAssetName = lastOpenedAssetName,
        recommendationImpressions = recommendationImpressions,
        recommendationOpens = recommendationOpens,
        lastRecommendedAssetIds = lastRecommendedAssetIds,
        lastRecommendationGeneratedAtMillis = lastRecommendationGeneratedAtMillis
    )
}
