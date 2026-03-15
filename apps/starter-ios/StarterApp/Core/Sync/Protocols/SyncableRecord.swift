import Foundation

protocol SyncableRecord: AnyObject {
    var id: String { get set }
    var updatedAt: Date { get set }
    var pendingSync: Bool { get set }
    var syncAction: SyncAction? { get set }
    var lastSyncedAt: Date? { get set }
}
