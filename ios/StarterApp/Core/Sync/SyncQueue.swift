import Foundation

struct SyncQueueItem {
    let change: SyncChange
    var retryCount: Int = 0
    var nextRetryAt: Date = .now

    // Exponential backoff: 2^n * 2 seconds, capped at 300s
    mutating func scheduleRetry() {
        retryCount += 1
        let delay = min(pow(2.0, Double(retryCount)) * 2.0, 300.0)
        nextRetryAt = Date.now.addingTimeInterval(delay)
    }

    var isEligible: Bool { retryCount < 5 && nextRetryAt <= .now }
}

final class SyncQueue {
    private var items: [SyncQueueItem] = []

    func enqueue(_ change: SyncChange) {
        items.append(SyncQueueItem(change: change))
    }

    func dequeueEligible() -> [SyncQueueItem] {
        items.filter(\.isEligible)
    }

    func markFailed(ids: Set<String>) {
        for i in items.indices where ids.contains(items[i].change.id) {
            items[i].scheduleRetry()
        }
        // Drop permanently failed items (>= 5 retries)
        items.removeAll { $0.retryCount >= 5 }
    }

    func remove(ids: Set<String>) {
        items.removeAll { ids.contains($0.change.id) }
    }

    var isEmpty: Bool { items.isEmpty }
}
