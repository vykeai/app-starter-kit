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

            AppButton(label: "Get Started", isLoading: false) {
                viewModel.step = .enterEmail
            }
            .padding(.horizontal, AppTokens.Spacing.lg)
        }
        .padding(AppTokens.Spacing.lg)
        .background(AppTokens.Color.background.ignoresSafeArea())
    }
}
