import Foundation

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

    private let authService = AuthService()

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
}
