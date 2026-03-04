import StoreKit
import UIKit

class ReviewManager {
    static let shared = ReviewManager()
    private init() {}

    private let lastReviewRequestKey = "reviewManager.lastRequestDate"
    private let minimumDaysBetweenRequests: Double = 7

    func requestReviewIfEligible(trigger: ReviewTrigger) {
        let defaults = UserDefaults.standard
        let lastRequest = defaults.object(forKey: lastReviewRequestKey) as? Date
        let now = Date()

        if let last = lastRequest,
           now.timeIntervalSince(last) < minimumDaysBetweenRequests * 86400 {
            return
        }

        defaults.set(now, forKey: lastReviewRequestKey)

        Task { @MainActor in
            if let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene {
                SKStoreReviewController.requestReview(in: scene)
            }
        }
    }
}

enum ReviewTrigger {
    case firstCompletedAction
    case milestone(count: Int)
    case customTrigger(name: String)
}
