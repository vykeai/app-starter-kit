import SwiftUI

struct PaywallView: View {
    @Environment(\.dismiss) var dismiss
    @State private var subscriptionManager = SubscriptionManager.shared

    var body: some View {
        NavigationStack {
            VStack(spacing: AppTokens.Spacing.xl) {
                // Header
                VStack(spacing: AppTokens.Spacing.sm) {
                    Image(systemName: "bolt.fill")
                        .font(.system(size: 56))
                        .foregroundStyle(AppTokens.Color.primary)

                    Text("Unlock Tracker")
                        .font(.largeTitle.bold())
                        .foregroundStyle(AppTokens.Color.textPrimary)

                    Text("Get unlimited access to all features")
                        .font(.body)
                        .foregroundStyle(AppTokens.Color.textSecondary)
                        .multilineTextAlignment(.center)
                }

                // Feature list
                VStack(alignment: .leading, spacing: AppTokens.Spacing.md) {
                    FeatureRow(icon: "doc.on.doc", title: "Unlimited templates")
                    FeatureRow(icon: "chart.bar.xaxis", title: "Advanced analytics")
                    FeatureRow(icon: "square.and.arrow.up", title: "Export your data")
                }
                .padding(AppTokens.Spacing.md)
                .background(AppTokens.Color.surface)
                .cornerRadius(AppTokens.Radius.lg)

                Spacer()

                // CTA buttons
                VStack(spacing: AppTokens.Spacing.sm) {
                    AppButton(label: "Start 7-day free trial", style: .primary) {
                        try? await subscriptionManager.purchase(productId: "tracker_monthly")
                    }

                    Button("Restore purchases") {
                        Task { try? await subscriptionManager.restorePurchases() }
                    }
                    .font(.caption)
                    .foregroundStyle(AppTokens.Color.textSecondary)
                }
            }
            .padding(AppTokens.Spacing.lg)
            .background(AppTokens.Color.background.ignoresSafeArea())
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Close") { dismiss() }
                }
            }
        }
    }
}

// MARK: - FeatureRow

private struct FeatureRow: View {
    let icon: String
    let title: String

    var body: some View {
        HStack(spacing: AppTokens.Spacing.md) {
            Image(systemName: icon)
                .foregroundStyle(AppTokens.Color.primary)
                .frame(width: 24)
            Text(title)
                .font(.body)
                .foregroundStyle(AppTokens.Color.textPrimary)
            Spacer()
            Image(systemName: "checkmark")
                .foregroundStyle(AppTokens.Color.success)
        }
    }
}
