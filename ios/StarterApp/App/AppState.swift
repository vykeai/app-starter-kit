import Foundation

@Observable
class AppState {
    var isAuthenticated: Bool = false
    var currentUser: AppUser? = nil
    let forceUpdateChecker = ForceUpdateChecker()
    let networkMonitor = NetworkMonitor()
}
