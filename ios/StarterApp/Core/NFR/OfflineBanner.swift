import SwiftUI

struct OfflineBanner: View {
    var body: some View {
        HStack(spacing: AppTokens.Spacing.sm) {
            Image(systemName: "wifi.slash")
                .foregroundStyle(.white)
            Text("You're offline -- changes saved locally")
                .font(.caption.bold())
                .foregroundStyle(.white)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, AppTokens.Spacing.sm)
        .background(AppTokens.Color.warning)
    }
}
