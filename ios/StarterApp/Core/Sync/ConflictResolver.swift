import Foundation

enum ResolutionStrategy {
    case serverWins
    case clientWins
    case mostRecent
    case merge
    case manual
}

struct AnyConflict {
    let id: String
    let collection: String
    let conflict: SyncConflict
}

struct ConflictResolver {
    static func resolve(_ conflict: AnyConflict, strategy: ResolutionStrategy) -> ResolutionStrategy {
        switch strategy {
        case .mostRecent:
            // Compare timestamps; caller applies chosen version
            return .mostRecent
        default:
            return strategy
        }
    }
}
