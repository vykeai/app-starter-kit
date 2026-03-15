import Foundation

protocol SyncRepository {
    func fetchPending() async throws -> [any SyncableRecord]
    func applyServerChanges(_ changes: [SyncChange]) async throws
    func resolveConflict(_ conflict: AnyConflict, strategy: ResolutionStrategy) async throws
}
