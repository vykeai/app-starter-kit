import Foundation
import UIKit

enum AuthStep {
    case welcome
    case enterEmail
    case enterCode(email: String)
}

@Observable
class AuthViewModel {
    var step: AuthStep = .welcome
    var emailInput: String = ""
    var codeInput: String = ""
    var isLoading: Bool = false
    var errorMessage: String? = nil

    /// Set to `true` after a successful social sign-in so that `AuthFlowView` can
    /// transition the app to the authenticated state (mirrors the `verifyCode` return value pattern).
    var authSucceeded: Bool = false

    private let authService = AuthService()

    // MARK: - Deep-link OTP

    /// Stores any code recovered from a deep link so the code-entry UI can render it.
    func handleDeepLinkCode(_ code: String) {
        codeInput = code
    }

    func consumePendingAuth(_ pendingAuthLink: PendingAuthLink) async -> Bool {
        if let email = pendingAuthLink.email, !email.isEmpty {
            emailInput = email
            step = .enterCode(email: email)
        } else {
            switch step {
            case .welcome:
                step = .enterEmail
            case .enterEmail, .enterCode:
                break
            }
        }

        if let code = pendingAuthLink.code, !code.isEmpty {
            handleDeepLinkCode(code)
        }

        if let linkToken = pendingAuthLink.linkToken, !linkToken.isEmpty {
            return await verifyMagicLink(linkToken: linkToken)
        }

        guard let email = pendingAuthLink.email,
              let code = pendingAuthLink.code,
              !email.isEmpty,
              !code.isEmpty else {
            return false
        }

        return await verifyMagicLink(email: email, code: code)
    }

    // MARK: - Auth flow

    func requestCode() async {
        guard !emailInput.isEmpty else { return }
        isLoading = true
        errorMessage = nil
        do {
            try await authService.requestMagicLink(email: emailInput)
            step = .enterCode(email: emailInput)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func verifyCode(email: String) async -> Bool {
        await verifyMagicLink(email: email, code: codeInput)
    }

    // MARK: - Social sign-in

    func signInWithApple() async {
        isLoading = true
        errorMessage = nil
        do {
            let credential = try await AppleSignInHelper.shared.signIn()
            let response = try await authService.authenticateWithSocial(credential: credential)
            KeychainHelper.shared.saveAccessToken(response.accessToken)
            KeychainHelper.shared.saveRefreshToken(response.refreshToken)
            authSucceeded = true
        } catch let error as SocialAuthError where error == .cancelled {
            // Silently ignore — user tapped cancel in the system sheet.
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func signInWithGoogle(presenting viewController: UIViewController?) async {
        guard let vc = viewController else { return }
        isLoading = true
        errorMessage = nil
        do {
            let credential = try await GoogleSignInHelper.shared.signIn(presenting: vc)
            let response = try await authService.authenticateWithSocial(credential: credential)
            KeychainHelper.shared.saveAccessToken(response.accessToken)
            KeychainHelper.shared.saveRefreshToken(response.refreshToken)
            authSucceeded = true
        } catch let error as SocialAuthError where error == .cancelled {
            // Silently ignore — user tapped cancel.
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    private func verifyMagicLink(
        email: String? = nil,
        code: String? = nil,
        linkToken: String? = nil
    ) async -> Bool {
        isLoading = true
        errorMessage = nil
        do {
            let result = try await authService.verifyMagicLink(
                email: email,
                code: code,
                linkToken: linkToken
            )
            KeychainHelper.shared.saveAccessToken(result.accessToken)
            KeychainHelper.shared.saveRefreshToken(result.refreshToken)
            isLoading = false
            return true
        } catch {
            errorMessage = "Invalid code. Please try again."
            isLoading = false
            return false
        }
    }
}
