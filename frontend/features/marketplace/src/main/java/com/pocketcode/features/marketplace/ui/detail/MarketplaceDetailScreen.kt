package com.pocketcode.features.marketplace.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.domain.marketplace.model.Asset
import com.pocketcode.domain.marketplace.model.Review

@Composable
fun MarketplaceDetailScreen(
    viewModel: MarketplaceDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.asset?.name ?: "Loading...") },
                navigationIcon = {
                    // IconButton(onClick = onNavigateUp) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading && uiState.asset == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.asset != null -> {
                    AssetDetailsContent(asset = uiState.asset!!, reviews = uiState.reviews)
                }
            }
        }
    }
}

@Composable
private fun AssetDetailsContent(asset: Asset, reviews: List<Review>) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(text = asset.name, style = MaterialTheme.typography.headlineMedium)
            Text(text = "by ${asset.authorId}", style = MaterialTheme.typography.titleSmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = asset.description, style = MaterialTheme.typography.bodyLarge)
        }

        item {
            Text("Reviews", style = MaterialTheme.typography.headlineSmall)
        }

        if (reviews.isEmpty()) {
            item {
                Text("No reviews yet.")
            }
        } else {
            items(reviews) { review ->
                ReviewListItem(review = review)
            }
        }
    }
}

@Composable
private fun ReviewListItem(review: Review) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "User: ${review.userId}", style = MaterialTheme.typography.titleSmall)
            Text(text = review.comment, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
