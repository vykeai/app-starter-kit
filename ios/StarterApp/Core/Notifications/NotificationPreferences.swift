import Foundation

struct NotificationPreferences: Codable, Equatable {
    let theme: String
    let pushMarketingEnabled: Bool
    let pushActivityEnabled: Bool
    let pushTransactionalEnabled: Bool
    let pushSystemEnabled: Bool
    let emailNotificationsEnabled: Bool
    let updatedAt: Date?
}

struct UpdateNotificationPreferencesBody: Encodable {
    let theme: String?
    let pushMarketingEnabled: Bool?
    let pushActivityEnabled: Bool?
    let pushTransactionalEnabled: Bool?
    let pushSystemEnabled: Bool?
    let emailNotificationsEnabled: Bool?
}

struct RegisterPushDeviceBody: Encodable {
    let platform: String
    let token: String
    let locale: String?
    let appVersion: String?
}

struct RegisteredPushDevice: Codable, Equatable {
    let id: String
    let platform: String
    let token: String
    let locale: String?
    let appVersion: String?
    let lastSeenAt: Date?
    let revokedAt: Date?
}
