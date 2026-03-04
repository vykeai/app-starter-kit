import SwiftUI

struct HomeView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: AppTokens.Spacing.lg) {
                Text("Welcome!")
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTokens.Color.textPrimary)

                Text("Your app is ready.")
                    .font(.body)
                    .foregroundStyle(AppTokens.Color.textSecondary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppTokens.Color.background.ignoresSafeArea())
            .navigationTitle("Home")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
