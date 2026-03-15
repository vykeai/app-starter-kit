import Foundation

struct MagicLinkRequestBody: Encodable {
    let email: String
}

struct MagicLinkVerifyBody: Encodable {
    let email: String?
    let code: String?
    let linkToken: String?
}

struct AuthResponse: Decodable {
    let accessToken: String
    let refreshToken: String
    let user: AppUser
}

struct MessageResponse: Decodable {
    let message: String
}

struct AuthService {
    func requestMagicLink(email: String) async throws {
        let _: MessageResponse = try await APIClient.shared.request(
            "auth/magic-link/request",
            method: "POST",
            body: MagicLinkRequestBody(email: email)
        )
    }

    func verifyMagicLink(
        email: String? = nil,
        code: String? = nil,
        linkToken: String? = nil
    ) async throws -> AuthResponse {
        return try await APIClient.shared.request(
            "auth/magic-link/verify",
            method: "POST",
            body: MagicLinkVerifyBody(email: email, code: code, linkToken: linkToken)
        )
    }

    func authenticateWithSocial(credential: SocialAuthCredential) async throws -> AuthResponse {
        struct SocialAuthRequest: Encodable {
            let provider: String
            let idToken: String
        }
        return try await APIClient.shared.request(
            "auth/social",
            method: "POST",
            body: SocialAuthRequest(provider: credential.provider.rawValue, idToken: credential.idToken)
        )
    }
}
