import SwiftUI

// MARK: - Firebase / Crashlytics
// TODO: Uncomment after adding GoogleService-Info.plist to the Xcode project target.
//       Steps:
//         1. Download GoogleService-Info.plist from the Firebase console.
//         2. Drag it into the AppStarterKit target (Copy Items if Needed = YES).
//         3. Add the Firebase SDK via Swift Package Manager (https://github.com/firebase/firebase-ios-sdk).
//         4. Uncomment the lines below.
// import Firebase
// FirebaseApp.configure()

@main
struct AppStarterKit: App {
    @State private var appState = AppState()
    @State private var toastManager = ToastManager()

    var body: some Scene {
        WindowGroup {
            RootView()
                .environment(appState)
                .environment(toastManager)
                .onOpenURL { url in
                    handleDeepLink(url, appState: appState)
                }
        }
    }

    // MARK: - Deep-link handling

    /// Handles two URL shapes:
    ///   • Custom scheme:  `appstarterkit://auth/verify?email=user@example.com&code=XXXXXX&linkToken=...`
    ///   • Universal link: `https://yourapp.com/auth/verify?email=user@example.com&code=XXXXXX&linkToken=...`
    private func handleDeepLink(_ url: URL, appState: AppState) {
        guard let components = URLComponents(url: url, resolvingAgainstBaseURL: true) else {
            return
        }

        // Accept both the custom scheme and the universal-link host/path.
        let isCustomScheme = url.scheme?.lowercased() == "appstarterkit"
        let isUniversalLink = (url.scheme == "https" || url.scheme == "http")
            && url.path == "/auth/verify"

        guard isCustomScheme || isUniversalLink else { return }

        // The path for custom scheme looks like "/auth/verify"; host is "auth".
        let isAuthVerify: Bool
        if isCustomScheme {
            isAuthVerify = url.host?.lowercased() == "auth" && url.path == "/verify"
        } else {
            isAuthVerify = true // already checked path above
        }

        guard isAuthVerify else {
            return
        }

        let email = components.queryItems?.first(where: { $0.name == "email" })?.value
        let code = components.queryItems?.first(where: { $0.name == "code" })?.value
        let linkToken = components.queryItems?.first(where: { $0.name == "linkToken" })?.value
        let pendingLink = PendingAuthLink(email: email, code: code, linkToken: linkToken)

        guard pendingLink.hasAuthPayload else { return }
        appState.pendingAuthLink = pendingLink
    }
}
