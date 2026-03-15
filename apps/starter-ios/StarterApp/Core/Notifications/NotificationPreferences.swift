import Foundation

struct NotificationPreferences: Codable, Equatable {
    let theme: String
    let pushMarketingEnabled: Bool
    let pushActivityEnabled: Bool
    let pushTransactionalEnabled: Bool
    let pushSystemEnabled: Bool
    let emailNotificationsEnabled: Bool
    let pushEnabled: Bool?
    let emailEnabled: Bool?
    let enabledCategories: [String]?
    let quietHoursEnabled: Bool?
    let quietHoursStart: String?
    let quietHoursEnd: String?
    let urgentBreaksQuietHours: Bool?
    let batchSoonNotifications: Bool?
    let updatedAt: Date?
}

struct UpdateNotificationPreferencesBody: Encodable {
    let theme: String?
    let pushMarketingEnabled: Bool?
    let pushActivityEnabled: Bool?
    let pushTransactionalEnabled: Bool?
    let pushSystemEnabled: Bool?
    let emailNotificationsEnabled: Bool?
    let pushEnabled: Bool?
    let emailEnabled: Bool?
    let enabledCategories: [String]?
    let quietHoursEnabled: Bool?
    let quietHoursStart: String?
    let quietHoursEnd: String?
    let urgentBreaksQuietHours: Bool?
    let batchSoonNotifications: Bool?
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
