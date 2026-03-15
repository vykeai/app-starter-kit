import Foundation

@MainActor
final class PushRegistrationManager {
    private let notificationService = NotificationService()

    func registerCurrentDevice(pushToken: String) async throws -> RegisteredPushDevice {
        try await notificationService.registerDevice(token: pushToken)
    }

    func revokeCurrentDevice(pushToken: String) async throws {
        _ = try await notificationService.revokeDevice(token: pushToken)
    }

    func handleRemoteRegistrationSucceeded(deviceToken: Data) async throws -> RegisteredPushDevice {
        let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()
        return try await registerCurrentDevice(pushToken: token)
    }

    // This manager intentionally does not request APNs permission itself.
    // Permission UX belongs to product UI; registration belongs to starter notification-core.
}
