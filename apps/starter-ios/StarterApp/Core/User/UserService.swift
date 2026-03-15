import Foundation

struct UserService {
    func fetchMe() async throws -> UserProfile {
        try await APIClient.shared.request("user/me")
    }

    func updateMe(displayName: String) async throws -> UserProfile {
        try await APIClient.shared.request(
            "user/me",
            method: "PATCH",
            body: UpdateUserProfileBody(displayName: displayName)
        )
    }
}
