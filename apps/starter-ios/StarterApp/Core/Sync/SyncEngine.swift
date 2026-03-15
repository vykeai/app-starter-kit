import Foundation

@Observable
final class SyncEngine {
    var state: SyncState = .idle
    var lastSyncAt: Date?
    var unresolvedConflicts: [AnyConflict] = []

    private let repository: any SyncRepository
    private let apiClient: any SyncAPIClient
    private let network: NetworkMonitor
    private let metrics: SyncMetrics
    private let queue: SyncQueue = SyncQueue()
    private var pollingTask: Task<Void, Never>?

    private var deviceId: String {
        if let stored = UserDefaults.standard.string(forKey: "com.appstarterkit.sync.deviceId") {
            return stored
        }
        let new = UUID().uuidString
        UserDefaults.standard.set(new, forKey: "com.appstarterkit.sync.deviceId")
        return new
    }

    init(
        repository: any SyncRepository,
        apiClient: any SyncAPIClient,
        network: NetworkMonitor,
        metrics: SyncMetrics
    ) {
        self.repository = repository
        self.apiClient = apiClient
        self.network = network
        self.metrics = metrics
    }

    func sync() async {
        guard network.isConnected else {
            state = .offline
            return
        }
        guard state != .syncing else { return }

        state = .syncing
        let start = Date.now

        do {
            // Push pending local changes
            let pending = try await repository.fetchPending()
            let changes = pending.compactMap { record -> SyncChange? in
                guard let action = record.syncAction else { return nil }
                return SyncChange(
                    id: record.id,
                    collection: String(describing: type(of: record)),
                    action: action,
                    payload: [:],
                    updatedAt: ISO8601DateFormatter().string(from: record.updatedAt)
                )
            }

            var pushedCount = 0
            var conflictCount = 0
            var errorCount = 0

            if !changes.isEmpty {
                let response = try await apiClient.syncPush(changes: changes, deviceId: deviceId)
                pushedCount = response.applied.count
                errorCount = response.errors.count
                let appliedIds = Set(response.applied.map(\.id))
                queue.remove(ids: appliedIds)

                // Handle conflicts
                conflictCount = response.conflicts.count
                let newConflicts = response.conflicts.map { c in
                    AnyConflict(id: c.id, collection: c.collection, conflict: c)
                }
                unresolvedConflicts.append(contentsOf: newConflicts)
            }

            // Pull server changes since last sync
            let pullResponse = try await apiClient.syncPull(since: lastSyncAt, collections: [])
            try await repository.applyServerChanges(pullResponse.items)

            let duration = Int(Date.now.timeIntervalSince(start) * 1000)
            metrics.record(SyncRecord(
                timestamp: start,
                pushedCount: pushedCount,
                pulledCount: pullResponse.items.count,
                conflictCount: conflictCount,
                errorCount: errorCount,
                durationMs: duration,
                succeeded: true
            ))

            lastSyncAt = .now
            state = .idle
        } catch {
            let duration = Int(Date.now.timeIntervalSince(start) * 1000)
            metrics.record(SyncRecord(
                timestamp: start,
                pushedCount: 0,
                pulledCount: 0,
                conflictCount: 0,
                errorCount: 1,
                durationMs: duration,
                succeeded: false
            ))
            state = .error(error.localizedDescription)
        }
    }

    func startPolling(interval: TimeInterval = 30) {
        stopPolling()
        pollingTask = Task { [weak self] in
            while !Task.isCancelled {
                await self?.sync()
                try? await Task.sleep(nanoseconds: UInt64(interval * 1_000_000_000))
            }
        }
    }

    func stopPolling() {
        pollingTask?.cancel()
        pollingTask = nil
    }

    func resolve(_ conflict: AnyConflict, strategy: ResolutionStrategy) async {
        do {
            try await repository.resolveConflict(conflict, strategy: strategy)
            unresolvedConflicts.removeAll { $0.id == conflict.id }
        } catch {
            state = .error(error.localizedDescription)
        }
    }
}
