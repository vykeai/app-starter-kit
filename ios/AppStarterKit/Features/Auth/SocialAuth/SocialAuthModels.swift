import Foundation

enum SocialAuthProvider: String {
    case apple
    case google
}

struct SocialAuthCredential {
    let provider: SocialAuthProvider
    let idToken: String
    let displayName: String?
}

enum SocialAuthError: LocalizedError {
    case invalidCredential
    case cancelled
    case networkError

    var errorDescription: String? {
        switch self {
        case .invalidCredential: return "Invalid sign-in credential"
        case .cancelled: return nil  // User cancelled — no error shown
        case .networkError: return "Sign-in failed. Check your connection."
        }
    }
}
