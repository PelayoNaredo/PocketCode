package com.pocketcode.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pocketcode.settings.subscriptions.SubscriptionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaywallViewModel : ViewModel() {

    private val _offerings = MutableStateFlow<List<com.revenuecat.purchases.Package>>(emptyList())
    val offerings: StateFlow<List<com.revenuecat.purchases.Package>> = _offerings.asStateFlow()

    private val _isProUser = MutableStateFlow(false)
    val isProUser: StateFlow<Boolean> = _isProUser.asStateFlow()

    init {
        loadOfferings()
        checkSubscriptionStatus()
    }

    private fun loadOfferings() {
        viewModelScope.launch {
            _offerings.value = SubscriptionManager.getOfferings()
        }
    }

    private fun checkSubscriptionStatus() {
        viewModelScope.launch {
            _isProUser.value = SubscriptionManager.isUserPro()
        }
    }

    fun purchase(activity: android.app.Activity, pkg: com.revenuecat.purchases.Package) {
        SubscriptionManager.purchase(activity, pkg,
            onSuccess = {
                // On success, check the subscription status again
                checkSubscriptionStatus()
            },
            onError = { errorMsg ->
                // Handle error
            }
        )
    }
}
