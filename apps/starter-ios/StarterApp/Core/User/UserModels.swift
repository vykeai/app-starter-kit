import Foundation

struct UserPreferencesSummary: Codable, Equatable {
    let theme: String?
    let pushMarketingEnabled: Bool?
    let pushActivityEnabled: Bool?
    let pushTransactionalEnabled: Bool?
    let pushSystemEnabled: Bool?
    let emailNotificationsEnabled: Bool?
}

struct UserProfile: Codable, Equatable {
    let id: String
    let email: String
    let displayName: String?
    let preferences: UserPreferencesSummary?
}

struct UpdateUserProfileBody: Encodable {
    let displayName: String?
}
