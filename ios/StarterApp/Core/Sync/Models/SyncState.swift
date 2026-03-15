import Foundation

enum SyncState: Equatable {
    case idle
    case syncing
    case error(String)
    case offline
}
