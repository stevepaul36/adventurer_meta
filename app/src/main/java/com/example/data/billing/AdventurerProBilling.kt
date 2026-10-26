package com.example.data.billing

import android.app.Activity
import android.content.Context
import com.example.BuildConfig
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package as RevenueCatPackage
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AdventurerProState(
    val isConfigured: Boolean = false,
    val isPro: Boolean = false,
    val isLoading: Boolean = false,
    val packages: List<RevenueCatPackage> = emptyList(),
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

/**
 * RevenueCat adapter. Active access is derived from CustomerInfo, never from a local
 * "premium=true" preference. Prices and product identifiers are supplied by the store.
 */
object AdventurerProBilling {
    const val PRO_ENTITLEMENT = "adventurer_pro"

    private val _state = MutableStateFlow(AdventurerProState())
    val state: StateFlow<AdventurerProState> = _state.asStateFlow()
    @Volatile private var initialized = false

    @Synchronized
    fun initialize(context: Context, appUserId: String?) {
        if (initialized) return

        val apiKey = BuildConfig.REVENUECAT_API_KEY.trim()
        if (appUserId.isNullOrBlank()) {
            _state.update {
                it.copy(
                    isConfigured = false,
                    isLoading = false,
                    errorMessage = "Secure sign-in is unavailable. Configure Firebase Authentication and try again.",
                    statusMessage = null
                )
            }
            return
        }
        if (apiKey.isEmpty()) {
            _state.update {
                it.copy(
                    isConfigured = false,
                    isLoading = false,
                    errorMessage = null,
                    statusMessage = "Subscriptions are not configured in this build."
                )
            }
            return
        }

        try {
            initialized = true
            Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.WARN
            Purchases.configure(
                PurchasesConfiguration.Builder(context.applicationContext, apiKey)
                    .appUserID(appUserId)
                    .build()
            )
            _state.update {
                it.copy(
                    isConfigured = true,
                    errorMessage = null,
                    statusMessage = null
                )
            }
            refresh()
        } catch (error: Exception) {
            initialized = false
            _state.update {
                it.copy(
                    isConfigured = false,
                    isLoading = false,
                    errorMessage = "Billing could not be initialized. Check the RevenueCat SDK key and setup.",
                    statusMessage = null
                )
            }
        }
    }

    fun refresh() {
        if (!_state.value.isConfigured || _state.value.isLoading) return
        _state.update { it.copy(isLoading = true, errorMessage = null, statusMessage = null) }

        Purchases.sharedInstance.getCustomerInfoWith(
            onError = {
                // Do not revoke a cached entitlement just because the network is unavailable.
                loadOfferings()
            },
            onSuccess = { customerInfo ->
                applyCustomerInfo(customerInfo)
                loadOfferings()
            }
        )
    }

    private fun loadOfferings() {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message.ifBlank {
                            "Unable to load subscription plans. Check your connection and RevenueCat offering."
                        }
                    )
                }
            },
            onSuccess = { offerings ->
                val available = offerings.current?.availablePackages.orEmpty()
                _state.update {
                    it.copy(
                        packages = available,
                        isLoading = false,
                        errorMessage = when {
                            available.isNotEmpty() -> null
                            else -> "No current offering is configured. Add products to a current offering in RevenueCat."
                        }
                    )
                }
            }
        )
    }

    fun purchase(activity: Activity, packageToPurchase: RevenueCatPackage) {
        if (!_state.value.isConfigured) {
            _state.update { it.copy(errorMessage = "Purchases are not configured for this build.") }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, statusMessage = null) }

        Purchases.sharedInstance.purchaseWith(
            PurchaseParams.Builder(activity, packageToPurchase).build(),
            onError = { error, userCancelled ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (userCancelled) null else
                            error.message.ifBlank { "Purchase failed. Please try again." },
                        statusMessage = if (userCancelled) "Purchase cancelled." else null
                    )
                }
            },
            onSuccess = { _, customerInfo ->
                applyCustomerInfo(customerInfo)
                _state.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = if (it.isPro) "Adventurer Pro is active. Ride on!"
                        else "Purchase was processed, but the adventurer_pro entitlement is not active. Check RevenueCat product mapping."
                    )
                }
            }
        )
    }

    fun restorePurchases() {
        if (!_state.value.isConfigured) {
            _state.update { it.copy(errorMessage = "Purchases are not configured for this build.") }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, statusMessage = null) }
        Purchases.sharedInstance.restorePurchasesWith(
            onError = { error ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message.ifBlank { "Could not restore purchases. Please try again." }
                    )
                }
            },
            onSuccess = { customerInfo ->
                applyCustomerInfo(customerInfo)
                _state.update {
                    it.copy(
                        isLoading = false,
                        statusMessage = if (it.isPro) "Your Adventurer Pro subscription has been restored."
                        else "No active Adventurer Pro subscription was found for this store account."
                    )
                }
            }
        )
    }

    private fun applyCustomerInfo(customerInfo: CustomerInfo) {
        val isPro = customerInfo.entitlements[PRO_ENTITLEMENT]?.isActive == true
        _state.update {
            it.copy(
                isPro = isPro,
                errorMessage = null
            )
        }
    }
}
