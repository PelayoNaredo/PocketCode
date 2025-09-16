package com.pocketcode.settings.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PaywallScreen(
    viewModel: PaywallViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val offerings by viewModel.offerings.collectAsState()
    val isProUser by viewModel.isProUser.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isProUser) "You are a Pro!" else "Go Pro") },
                navigationIcon = {
                    Button(onClick = onBackClick) { Text("Back") }
                }
            )
        }
    ) { paddingValues ->
        if (isProUser) {
            Column(modifier = Modifier.padding(paddingValues)) {
                Text("Thank you for being a Pro user!")
            }
        } else {
            if (offerings.isEmpty()) {
                CircularProgressIndicator()
            } else {
                LazyColumn(modifier = Modifier.padding(paddingValues)) {
                    item {
                        Text("Unlock Pro Features!")
                        Text("- Unlimited builds")
                        Text("- Access to premium AI models")
                        Text("- Team collaboration")
                    }
                    items(offerings) { pkg ->
                        Button(onClick = { viewModel.purchase(context as Activity, pkg) }) {
                            Text("${pkg.product.title} - ${pkg.product.price.formatted}")
                        }
                    }
                }
            }
        }
    }
}
