package com.pocketcode.features.marketplace.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pocketcode.domain.marketplace.model.Asset

@Composable
fun MarketplaceHomeScreen(
    viewModel: MarketplaceHomeViewModel = hiltViewModel(),
    onAssetClick: (String) -> Unit,
    onUploadClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Marketplace") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onUploadClick) {
                Icon(Icons.Default.Add, contentDescription = "Upload Asset")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    AssetList(assets = uiState.assets, onAssetClick = onAssetClick)
                }
            }
        }
    }
}

@Composable
private fun AssetList(assets: List<Asset>, onAssetClick: (String) -> Unit) {
    LazyColumn {
        items(assets) { asset ->
            AssetListItem(asset = asset, onClick = { onAssetClick(asset.id) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssetListItem(asset: Asset, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = asset.name, style = MaterialTheme.typography.titleMedium)
            Text(text = asset.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
