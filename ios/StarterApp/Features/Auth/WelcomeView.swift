import AuthenticationServices
import SwiftUI

struct WelcomeView: View {
    let viewModel: AuthViewModel

    var body: some View {
        VStack(spacing: AppTokens.Spacing.xl) {
            Spacer()

            VStack(spacing: AppTokens.Spacing.md) {
                Text("\u{26A1}")
                    .font(.system(size: 64))

                Text("StarterApp")
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTokens.Color.textPrimary)

                Text("Your app, ready to build.")
                    .font(.body)
                    .foregroundStyle(AppTokens.Color.textSecondary)
                    .multilineTextAlignment(.center)
            }

            Spacer()

            VStack(spacing: AppTokens.Spacing.md) {
                // Primary email sign-in CTA
                AppButton(label: "Get Started", isLoading: false) {
                    viewModel.step = .enterEmail
                }
                .padding(.horizontal, AppTokens.Spacing.lg)

                // Divider with "or"
                HStack {
                    Rectangle()
                        .frame(height: 1)
                        .foregroundStyle(AppTokens.Color.surfaceElevated)
                    Text("or")
                        .font(.caption)
                        .foregroundStyle(AppTokens.Color.textSecondary)
                    Rectangle()
                        .frame(height: 1)
                        .foregroundStyle(AppTokens.Color.surfaceElevated)
                }
                .padding(.horizontal, AppTokens.Spacing.lg)

                // Sign in with Apple (system-provided button)
                SignInWithAppleButton { request in
                    request.requestedScopes = [.fullName, .email]
                } onCompletion: { _ in
                    Task { await viewModel.signInWithApple() }
                }
                .frame(height: 50)
                .padding(.horizontal, AppTokens.Spacing.lg)
                .cornerRadius(AppTokens.Radius.md)

                // Sign in with Google
                SignInWithGoogleButton {
                    Task { await viewModel.signInWithGoogle(presenting: nil) }
                }
                .padding(.horizontal, AppTokens.Spacing.lg)
            }
        }
        .padding(AppTokens.Spacing.lg)
        .background(AppTokens.Color.background.ignoresSafeArea())
    }
}
