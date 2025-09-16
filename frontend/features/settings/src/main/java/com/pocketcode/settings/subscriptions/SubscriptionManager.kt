package com.pocketcode.settings.subscriptions

import android.content.Context
import android.util.Log
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration

object SubscriptionManager {

    private const val REVENUECAT_API_KEY = "your_revenuecat_api_key" // This should be loaded securely

    fun initialize(context: Context) {
        if (Purchases.isConfigured) {
            return
        }

        val configuration = PurchasesConfiguration.Builder(context, REVENUECAT_API_KEY)
            .logLevel(LogLevel.DEBUG)
            .build()
        Purchases.configure(configuration)
        Log.d("SubscriptionManager", "RevenueCat SDK initialized.")
    }

    suspend fun getOfferings(): List<com.revenuecat.purchases.Package> {
        return try {
            val offerings = Purchases.sharedInstance.getOfferings()
            offerings.current?.availablePackages ?: emptyList()
        } catch (e: Exception) {
            Log.e("SubscriptionManager", "Error fetching offerings", e)
            emptyList()
        }
    }

    fun purchase(
        activity: Activity,
        pkg: com.revenuecat.purchases.Package,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.purchasePackage(
            activity,
            pkg,
            onError = { error, _ -> onError(error.message) },
            onSuccess = { _, _ -> onSuccess() }
        )
    }

    suspend fun isUserPro(): Boolean {
        return try {
            val customerInfo = Purchases.sharedInstance.getCustomerInfo()
            customerInfo.entitlements["pro"]?.isActive == true
        } catch (e: Exception) {
            Log.e("SubscriptionManager", "Error checking subscription status", e)
            false
        }
    }
}
