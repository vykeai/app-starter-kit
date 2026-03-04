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

    // Strong reference to the observation token so it lives as long as the ViewModel.
    private var deepLinkObserver: NSObjectProtocol?

    init() {
        // Listen for deep-link OTP codes. If the auth flow is active (the user is on
        // the code-entry screen), we auto-populate the OTP field so they don't have
        // to type it manually.
        deepLinkObserver = NotificationCenter.default.addObserver(
            forName: .deepLinkOTPReceived,
            object: nil,
            queue: .main
        ) { [weak self] notification in
            guard let self,
                  let code = notification.userInfo?["code"] as? String else { return }
            self.handleDeepLinkCode(code)
        }
    }

    deinit {
        if let observer = deepLinkObserver {
            NotificationCenter.default.removeObserver(observer)
        }
    }

    // MARK: - Deep-link OTP

    /// Auto-populates `codeInput` when a deep-link arrives, but only if the user is
    /// currently on the code-entry step. The view observes `codeInput` and will update
    /// its digit boxes accordingly.
    func handleDeepLinkCode(_ code: String) {
        // Only pre-fill if we're on the code-entry screen.
        guard case .enterCode = step else { return }
        codeInput = code
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
        isLoading = true
        errorMessage = nil
        do {
            let result = try await authService.verifyMagicLink(email: email, code: codeInput)
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
}
