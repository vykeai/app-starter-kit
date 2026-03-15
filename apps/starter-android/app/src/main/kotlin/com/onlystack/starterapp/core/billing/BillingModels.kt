package com.onlystack.starterapp.core.billing

data class EntitlementState(
    val tier: String,
    val source: String,
    val features: List<String>,
    val renewsAt: String? = null,
)

data class VerifyNativePurchaseBody(
    val platform: String,
    val productId: String,
    val purchaseToken: String,
    val transactionId: String? = null,
)

data class VerifyNativePurchaseResponse(
    val verified: Boolean,
    val productId: String,
    val entitlement: EntitlementState,
)
