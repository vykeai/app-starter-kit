import Foundation

protocol SyncAPIClient {
    func syncPush(changes: [SyncChange], deviceId: String) async throws -> SyncPushResponse
    func syncPull(since: Date?, collections: [String]) async throws -> SyncPullResponse<SyncChange>
}
