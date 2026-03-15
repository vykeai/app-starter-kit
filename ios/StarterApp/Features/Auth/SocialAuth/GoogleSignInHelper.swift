import Foundation
import UIKit

// Google Sign-In helper
// TODO: Install GoogleSignIn SDK: add to ios/Podfile or Package.swift
//   .package(url: "https://github.com/google/GoogleSignIn-iOS", from: "7.0.0")
//   then: import GoogleSignIn

@MainActor
final class GoogleSignInHelper {
    static let shared = GoogleSignInHelper()

    // TODO: Replace with your Google OAuth Client ID from Google Cloud Console.
    // Set in Info.plist as GIDClientID and REVERSED_CLIENT_ID as a URL scheme.

    func signIn(presenting viewController: UIViewController) async throws -> SocialAuthCredential {
        // TODO: Uncomment after adding GoogleSignIn SDK:
        /*
        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: viewController)
        guard let idToken = result.user.idToken?.tokenString else {
            throw SocialAuthError.invalidCredential
        }
        return SocialAuthCredential(
            provider: .google,
            idToken: idToken,
            displayName: result.user.profile?.name
        )
        */

        // Placeholder until SDK is added:
        throw SocialAuthError.invalidCredential
    }

    func configure() {
        // TODO: Call in StarterApp.init() after adding SDK:
        // GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: "YOUR_CLIENT_ID")
    }

    func handleURL(_ url: URL) -> Bool {
        // TODO: Call from StarterApp .onOpenURL BEFORE deeplink handler:
        // return GIDSignIn.sharedInstance.handle(url)
        return false
    }
}
