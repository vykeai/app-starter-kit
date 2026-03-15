import SwiftUI

struct EmptyStateView: View {
    let title: String
    let subtitle: String
    var actionLabel: String? = nil
    var action: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: AppTokens.Spacing.lg) {
            Text(title)
                .font(.title2.bold())
                .foregroundStyle(AppTokens.Color.textPrimary)
                .multilineTextAlignment(.center)

            Text(subtitle)
                .font(.body)
                .foregroundStyle(AppTokens.Color.textSecondary)
                .multilineTextAlignment(.center)

            if let label = actionLabel, let act = action {
                AppButton(label: label, action: { act() })
                    .frame(maxWidth: 240)
            }
        }
        .padding(AppTokens.Spacing.xl)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
