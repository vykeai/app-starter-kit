package com.onlystack.starterapp.core.subscription

import android.content.Context
import com.android.billingclient.api.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

enum class SubscriptionTier { FREE, TRACKER }

enum class TrackerFeature {
    BASIC_LOGGING, EXERCISE_LIBRARY,                           // Free
    UNLIMITED_TEMPLATES, ADVANCED_ANALYTICS, EXPORT_DATA,     // Tracker+
}

@Singleton
class SubscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : PurchasesUpdatedListener {

    // Product IDs — must match Play Console exactly
    private val trackerMonthlyId  = "tracker_monthly"
    private val trackerYearlyId   = "tracker_yearly"
    private val trackerLifetimeId = "tracker_lifetime"

    private val _currentTier = MutableStateFlow(SubscriptionTier.FREE)
    val currentTier: StateFlow<SubscriptionTier> = _currentTier.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .build()

    init {
        connect()
    }

    // MARK: — Connection

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch { refreshEntitlements() }
                }
            }
            override fun onBillingServiceDisconnected() {
                connect() // auto-reconnect
            }
        })
    }

    // MARK: — Products

    suspend fun queryProducts(): List<ProductDetails> {
        val productList = listOf(trackerMonthlyId, trackerYearlyId).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        } + listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(trackerLifetimeId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        return suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { _, details ->
                cont.resume(details)
            }
        }
    }

    // MARK: — Purchase

    fun launchPurchaseFlow(activity: android.app.Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
        val productDetailsParamsList = if (offerToken != null) {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        } else {
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .build()
            )
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                purchases.forEach { handlePurchase(it) }
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                suspendCancellableCoroutine { cont ->
                    billingClient.acknowledgePurchase(params) { cont.resume(it) }
                }
            }
            refreshEntitlements()
        }
    }

    // MARK: — Entitlements

    suspend fun refreshEntitlements() {
        val subsResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        val inappResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val allPurchases = subsResult.purchasesList + inappResult.purchasesList
        val hasTracker = allPurchases.any { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            purchase.products.any { it in listOf(trackerMonthlyId, trackerYearlyId, trackerLifetimeId) }
        }
        _currentTier.value = if (hasTracker) SubscriptionTier.TRACKER else SubscriptionTier.FREE
    }

    fun resetEntitlements() {
        _currentTier.value = SubscriptionTier.FREE
    }

    fun isFeatureAvailable(feature: TrackerFeature): Boolean = when (feature) {
        TrackerFeature.BASIC_LOGGING, TrackerFeature.EXERCISE_LIBRARY -> true
        TrackerFeature.UNLIMITED_TEMPLATES,
        TrackerFeature.ADVANCED_ANALYTICS,
        TrackerFeature.EXPORT_DATA -> _currentTier.value == SubscriptionTier.TRACKER
    }
}
