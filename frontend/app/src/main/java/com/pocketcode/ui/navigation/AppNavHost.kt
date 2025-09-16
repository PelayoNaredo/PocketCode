package com.pocketcode.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pocketcode.features.marketplace.ui.detail.MarketplaceDetailScreen
import com.pocketcode.features.marketplace.ui.home.MarketplaceHomeScreen
import com.pocketcode.features.marketplace.ui.upload.AssetUploadScreen

object AppRoutes {
    const val DASHBOARD = "dashboard"
    const val MARKETPLACE_HOME = "marketplace_home"
    const val MARKETPLACE_DETAIL = "marketplace_detail/{assetId}"
    const val MARKETPLACE_UPLOAD = "marketplace_upload"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = AppRoutes.DASHBOARD) {

        composable(AppRoutes.DASHBOARD) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = { navController.navigate(AppRoutes.MARKETPLACE_HOME) }) {
                    Text("Go to Marketplace")
                }
            }
        }

        // Marketplace Routes
        composable(route = AppRoutes.MARKETPLACE_HOME) {
            MarketplaceHomeScreen(
                onAssetClick = { assetId ->
                    navController.navigate("marketplace_detail/$assetId")
                },
                onUploadClick = {
                    navController.navigate(AppRoutes.MARKETPLACE_UPLOAD)
                }
            )
        }
        composable(
            route = AppRoutes.MARKETPLACE_DETAIL,
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) {
            MarketplaceDetailScreen(
                onNavigateUp = { navController.popBackStack() }
            )
        }
        composable(route = AppRoutes.MARKETPLACE_UPLOAD) {
            AssetUploadScreen(
                onUploadSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
