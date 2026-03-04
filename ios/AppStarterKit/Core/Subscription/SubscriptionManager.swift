import Foundation

// RevenueCat integration stub
// TODO: Install RevenueCat SDK via Swift Package Manager:
//   .package(url: "https://github.com/RevenueCat/purchases-ios", from: "4.0.0")
//   then: import RevenueCat

enum SubscriptionTier: String {
    case free = "free"
    case tracker = "tracker"
}

enum TrackerFeature {
    case basicLogging, exerciseLibrary         // Always free
    case unlimitedTemplates, advancedAnalytics, exportData  // Tracker+
}

enum SubscriptionError: LocalizedError {
    case notConfigured

    var errorDescription: String? {
        "Subscription not configured. See SubscriptionManager.swift TODO comments."
    }
}

@Observable
@MainActor
final class SubscriptionManager {
    static let shared = SubscriptionManager()

    var currentTier: SubscriptionTier = .free
    var isLoading: Bool = false

    // TODO: Replace with your RevenueCat API key from the RevenueCat dashboard.
    private let apiKey = "YOUR_REVENUECAT_PUBLIC_API_KEY"

    /// Call once at app launch, after the user ID is known.
    /// - Parameter userId: The authenticated user's ID to associate purchases with.
    func configure(userId: String) {
        // TODO: Uncomment after adding RevenueCat SDK:
        // Purchases.logLevel = .debug
        // Purchases.configure(withAPIKey: apiKey, appUserID: userId)
    }

    /// Loads available offerings from RevenueCat.
    func fetchOfferings() async {
        isLoading = true
        // TODO: let offerings = try? await Purchases.shared.offerings()
        isLoading = false
    }

    /// Initiates a purchase for the given product ID.
    /// - Parameter productId: The RevenueCat product identifier (e.g. "tracker_monthly").
    func purchase(productId: String) async throws {
        // TODO: Resolve the matching Package from offerings, then:
        // let result = try await Purchases.shared.purchase(package: package)
        // if !result.userCancelled { currentTier = .tracker }
        throw SubscriptionError.notConfigured
    }

    /// Restores previous purchases for the current user.
    func restorePurchases() async throws {
        // TODO: try await Purchases.shared.restorePurchases()
    }

    /// Returns `true` when the user has access to the given feature.
    func isFeatureAvailable(_ feature: TrackerFeature) -> Bool {
        switch feature {
        case .basicLogging, .exerciseLibrary:
            return true  // Always available on free tier
        case .unlimitedTemplates, .advancedAnalytics, .exportData:
            return currentTier == .tracker
        }
    }
}
