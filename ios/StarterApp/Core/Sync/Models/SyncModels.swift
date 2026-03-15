import Foundation

enum SyncAction: String, Codable {
    case create
    case update
    case delete
}

struct SyncChange: Codable {
    let id: String
    let collection: String
    let action: SyncAction
    let payload: [String: AnyCodable]
    let updatedAt: String
}

struct SyncResult: Codable {
    let id: String
    let collection: String
    let success: Bool
}

struct SyncConflict: Codable {
    let id: String
    let collection: String
    let clientVersion: [String: AnyCodable]
    let serverVersion: [String: AnyCodable]
    let serverUpdatedAt: String
}

struct SyncError: Codable {
    let id: String
    let collection: String
    let message: String
}

struct SyncPushResponse: Codable {
    let applied: [SyncResult]
    let conflicts: [SyncConflict]
    let errors: [SyncError]
    let timestamp: String
}

struct SyncPullResponse<T: Codable>: Codable {
    let items: [T]
    let hasMore: Bool
    let nextSyncAt: String
}

// Minimal type-erased Codable wrapper
struct AnyCodable: Codable {
    let value: Any

    init(_ value: Any) { self.value = value }

    init(from decoder: Decoder) throws {
        let container = try decoder.singleValueContainer()
        if let bool = try? container.decode(Bool.self) { value = bool }
        else if let int = try? container.decode(Int.self) { value = int }
        else if let double = try? container.decode(Double.self) { value = double }
        else if let string = try? container.decode(String.self) { value = string }
        else if let array = try? container.decode([AnyCodable].self) { value = array.map(\.value) }
        else if let dict = try? container.decode([String: AnyCodable].self) { value = dict.mapValues(\.value) }
        else { value = NSNull() }
    }

    func encode(to encoder: Encoder) throws {
        var container = encoder.singleValueContainer()
        switch value {
        case let bool as Bool: try container.encode(bool)
        case let int as Int: try container.encode(int)
        case let double as Double: try container.encode(double)
        case let string as String: try container.encode(string)
        case let array as [Any]: try container.encode(array.map { AnyCodable($0) })
        case let dict as [String: Any]: try container.encode(dict.mapValues { AnyCodable($0) })
        default: try container.encodeNil()
        }
    }
}
