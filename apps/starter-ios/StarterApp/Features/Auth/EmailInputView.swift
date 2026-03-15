import SwiftUI

struct EmailInputView: View {
    let viewModel: AuthViewModel

    var body: some View {
        VStack(spacing: AppTokens.Spacing.xl) {
            Spacer()

            VStack(spacing: AppTokens.Spacing.md) {
                Text("Sign In")
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTokens.Color.textPrimary)

                Text("We'll send an 8-digit code to your email.")
                    .font(.body)
                    .foregroundStyle(AppTokens.Color.textSecondary)
                    .multilineTextAlignment(.center)
            }

            AppTextField(
                label: "Email",
                placeholder: "you@example.com",
                text: Binding(
                    get: { viewModel.emailInput },
                    set: { viewModel.emailInput = $0 }
                ),
                keyboardType: .emailAddress
            )
            .padding(.horizontal, AppTokens.Spacing.lg)

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(AppTokens.Color.error)
            }

            Spacer()

            AppButton(label: "Send Code", isLoading: viewModel.isLoading) {
                await viewModel.requestCode()
            }
            .padding(.horizontal, AppTokens.Spacing.lg)
        }
        .padding(AppTokens.Spacing.lg)
        .background(AppTokens.Color.background.ignoresSafeArea())
    }
}
