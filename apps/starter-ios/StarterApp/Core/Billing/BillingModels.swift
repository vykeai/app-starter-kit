import Foundation

struct VerifyNativePurchaseBody: Encodable {
    let platform: String
    let productId: String
    let purchaseToken: String
    let transactionId: String?
}

struct EntitlementState: Codable, Equatable {
    let tier: String
    let source: String
    let features: [String]
    let renewsAt: String?
}

struct VerifyNativePurchaseResponse: Codable, Equatable {
    let verified: Bool
    let productId: String
    let entitlement: EntitlementState
}
