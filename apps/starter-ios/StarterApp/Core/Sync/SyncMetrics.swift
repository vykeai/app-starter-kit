import Foundation

struct SyncRecord: Codable {
    let timestamp: Date
    let pushedCount: Int
    let pulledCount: Int
    let conflictCount: Int
    let errorCount: Int
    let durationMs: Int
    let succeeded: Bool
}

@Observable
final class SyncMetrics {
    private let defaultsKey = "com.appstarterkit.sync.records"
    private(set) var records: [SyncRecord] = []

    init() { load() }

    var successRate: Double {
        guard !records.isEmpty else { return 1.0 }
        return Double(records.filter(\.succeeded).count) / Double(records.count)
    }

    func record(_ entry: SyncRecord) {
        records.append(entry)
        // Keep last 100
        if records.count > 100 { records.removeFirst(records.count - 100) }
        save()
    }

    private func load() {
        guard let data = UserDefaults.standard.data(forKey: defaultsKey),
              let decoded = try? JSONDecoder().decode([SyncRecord].self, from: data) else { return }
        records = decoded
    }

    private func save() {
        guard let data = try? JSONEncoder().encode(records) else { return }
        UserDefaults.standard.set(data, forKey: defaultsKey)
    }
}
