import Foundation
import StoreKit

enum SubscriptionTier: String {
    case free = "free"
    case tracker = "tracker"
}

enum TrackerFeature {
    case basicLogging, exerciseLibrary             // Always free
    case unlimitedTemplates, advancedAnalytics, exportData  // Tracker+
}

@Observable
@MainActor
final class SubscriptionManager {
    static let shared = SubscriptionManager()

    var currentTier: SubscriptionTier = .free
    var isLoading: Bool = false

    // Product IDs — must match App Store Connect exactly
    private let trackerMonthlyId = "tracker_monthly"
    private let trackerYearlyId  = "tracker_yearly"
    private let trackerLifetimeId = "tracker_lifetime"

    private var products: [Product] = []
    nonisolated(unsafe) private var purchaseListener: Task<Void, Never>?

    init() {
        purchaseListener = listenForTransactions()
        Task { await loadProducts() }
    }

    deinit {
        purchaseListener?.cancel()
    }

    // MARK: — Products

    func loadProducts() async {
        isLoading = true
        do {
            products = try await Product.products(for: [
                trackerMonthlyId, trackerYearlyId, trackerLifetimeId,
            ])
        } catch {
            // Silent failure — paywall shows empty state with retry
        }
        isLoading = false
    }

    func product(for id: String) -> Product? {
        products.first { $0.id == id }
    }

    // MARK: — Purchase

    func purchase(_ product: Product) async throws {
        let result = try await product.purchase()
        switch result {
        case .success(let verification):
            let transaction = try checkVerified(verification)
            await transaction.finish()
            await refreshEntitlements()
        case .userCancelled:
            break
        case .pending:
            break
        @unknown default:
            break
        }
    }

    func restorePurchases() async throws {
        try await AppStore.sync()
        await refreshEntitlements()
    }

    // MARK: — Entitlements

    func refreshEntitlements() async {
        for await result in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(result) else { continue }
            if transaction.productID == trackerMonthlyId ||
               transaction.productID == trackerYearlyId ||
               transaction.productID == trackerLifetimeId {
                currentTier = .tracker
                return
            }
        }
        currentTier = .free
    }

    func resetEntitlements() {
        currentTier = .free
    }

    func isFeatureAvailable(_ feature: TrackerFeature) -> Bool {
        switch feature {
        case .basicLogging, .exerciseLibrary:
            return true
        case .unlimitedTemplates, .advancedAnalytics, .exportData:
            return currentTier == .tracker
        }
    }

    // MARK: — Transaction listener

    private func listenForTransactions() -> Task<Void, Never> {
        Task.detached {
            for await result in Transaction.updates {
                guard let transaction = try? self.checkVerified(result) else { continue }
                await self.refreshEntitlements()
                await transaction.finish()
            }
        }
    }

    nonisolated private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified: throw StoreError.failedVerification
        case .verified(let value): return value
        }
    }
}

enum StoreError: Error {
    case failedVerification
}
