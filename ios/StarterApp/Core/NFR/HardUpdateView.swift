import SwiftUI

struct HardUpdateView: View {
    var body: some View {
        VStack(spacing: AppTokens.Spacing.xl) {
            Spacer()

            Text("\u{1F504}")
                .font(.system(size: 64))

            VStack(spacing: AppTokens.Spacing.md) {
                Text("Update Required")
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTokens.Color.textPrimary)

                Text("A new version of the app is required to continue. Please update from the App Store.")
                    .font(.body)
                    .foregroundStyle(AppTokens.Color.textSecondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, AppTokens.Spacing.lg)
            }

            Spacer()

            AppButton(label: "Update Now") {
                if let url = URL(string: "https://apps.apple.com/app/idYOUR_APP_ID") {
                    await UIApplication.shared.open(url)
                }
            }
            .padding(.horizontal, AppTokens.Spacing.lg)
        }
        .padding(AppTokens.Spacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(AppTokens.Color.background.ignoresSafeArea())
    }
}
