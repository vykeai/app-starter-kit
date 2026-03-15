import Foundation

struct NotificationService {
    func fetchPreferences() async throws -> NotificationPreferences {
        try await APIClient.shared.request("notifications/preferences")
    }

    func updatePreferences(
        theme: String? = nil,
        pushMarketingEnabled: Bool? = nil,
        pushActivityEnabled: Bool? = nil,
        pushTransactionalEnabled: Bool? = nil,
        pushSystemEnabled: Bool? = nil,
        emailNotificationsEnabled: Bool? = nil
    ) async throws -> NotificationPreferences {
        try await APIClient.shared.request(
            "notifications/preferences",
            method: "PATCH",
            body: UpdateNotificationPreferencesBody(
                theme: theme,
                pushMarketingEnabled: pushMarketingEnabled,
                pushActivityEnabled: pushActivityEnabled,
                pushTransactionalEnabled: pushTransactionalEnabled,
                pushSystemEnabled: pushSystemEnabled,
                emailNotificationsEnabled: emailNotificationsEnabled
            )
        )
    }

    func registerDevice(
        token: String,
        locale: String? = Locale.current.identifier,
        appVersion: String? = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String
    ) async throws -> RegisteredPushDevice {
        try await APIClient.shared.request(
            "notifications/devices",
            method: "POST",
            body: RegisterPushDeviceBody(
                platform: "ios",
                token: token,
                locale: locale,
                appVersion: appVersion
            )
        )
    }

    func revokeDevice(token: String) async throws -> MessageResponse {
        try await APIClient.shared.request(
            "notifications/devices/\(token.addingPercentEncoding(withAllowedCharacters: .urlPathAllowed) ?? token)",
            method: "DELETE"
        )
    }
}
