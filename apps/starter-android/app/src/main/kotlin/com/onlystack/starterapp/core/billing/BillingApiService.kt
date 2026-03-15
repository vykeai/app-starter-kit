package com.onlystack.starterapp.core.billing

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BillingApiService {
    @GET("billing/entitlements")
    suspend fun fetchEntitlements(): EntitlementState

    @POST("billing/verify/native")
    suspend fun verifyNativePurchase(
        @Body body: VerifyNativePurchaseBody,
    ): VerifyNativePurchaseResponse
}
