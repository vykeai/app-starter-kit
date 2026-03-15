import Foundation

struct BillingService {
    func fetchEntitlements() async throws -> EntitlementState {
        try await APIClient.shared.request("billing/entitlements")
    }

    func verifyNativePurchase(
        productId: String,
        purchaseToken: String,
        transactionId: String? = nil
    ) async throws -> VerifyNativePurchaseResponse {
        try await APIClient.shared.request(
            "billing/verify/native",
            method: "POST",
            body: VerifyNativePurchaseBody(
                platform: "ios",
                productId: productId,
                purchaseToken: purchaseToken,
                transactionId: transactionId
            )
        )
    }
}
