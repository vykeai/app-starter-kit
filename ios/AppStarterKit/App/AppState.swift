import Foundation

struct PendingAuthLink: Equatable {
    let email: String?
    let code: String?
    let linkToken: String?

    var hasAuthPayload: Bool {
        !(email?.isEmpty ?? true) || !(code?.isEmpty ?? true) || !(linkToken?.isEmpty ?? true)
    }
}

@Observable
class AppState {
    var isAuthenticated: Bool = false
    var currentUser: AppUser? = nil
    var syncEngine: SyncEngine?
    var pendingAuthLink: PendingAuthLink? = nil
    let forceUpdateChecker = ForceUpdateChecker()
    let networkMonitor = NetworkMonitor()

    /// Guards against double-tap or concurrent logout calls.
    private(set) var isLoggingOut: Bool = false

    @MainActor
    func logout() {
        guard !isLoggingOut else { return }
        isLoggingOut = true
        KeychainHelper.shared.clearAll()
        isAuthenticated = false
        currentUser = nil
        pendingAuthLink = nil
        isLoggingOut = false
    }
}
