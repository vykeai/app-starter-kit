package com.onlystack.starterapp.core.billing

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepository @Inject constructor(
    private val apiService: BillingApiService,
) {
    suspend fun fetchEntitlements(): Result<EntitlementState> = runCatching {
        apiService.fetchEntitlements()
    }

    suspend fun verifyNativePurchase(
        productId: String,
        purchaseToken: String,
        transactionId: String? = null,
    ): Result<VerifyNativePurchaseResponse> = runCatching {
        apiService.verifyNativePurchase(
            VerifyNativePurchaseBody(
                platform = "android",
                productId = productId,
                purchaseToken = purchaseToken,
                transactionId = transactionId,
            )
        )
    }
}
