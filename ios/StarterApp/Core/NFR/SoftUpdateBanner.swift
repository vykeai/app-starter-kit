import SwiftUI

struct SoftUpdateBanner: View {
    @Environment(AppState.self) private var appState

    var body: some View {
        HStack(spacing: AppTokens.Spacing.md) {
            Text("Update available")
                .font(.subheadline.bold())
                .foregroundStyle(.white)

            Spacer()

            Button("Update") {
                if let url = URL(string: "https://apps.apple.com/app/idYOUR_APP_ID") {
                    Task { await UIApplication.shared.open(url) }
                }
            }
            .font(.subheadline.bold())
            .foregroundStyle(AppTokens.Color.primary)

            Button("Later") {
                appState.forceUpdateChecker.isSoftBannerDismissed = true
            }
            .font(.subheadline)
            .foregroundStyle(AppTokens.Color.textSecondary)
        }
        .padding(.horizontal, AppTokens.Spacing.md)
        .padding(.vertical, AppTokens.Spacing.sm)
        .background(AppTokens.Color.surfaceElevated)
    }
}
