import SwiftUI

// MARK: - SyncableRecord Example
//
// To make a model sync-aware, conform it to SyncableRecord:
//
//   @Observable
//   final class HomeModel: SyncableRecord {
//       var id: String = UUID().uuidString
//       var updatedAt: Date = .now
//       var pendingSync: Bool = false
//       var syncAction: SyncAction? = nil
//       var lastSyncedAt: Date? = nil
//
//       // Your model's own fields:
//       var title: String = ""
//   }
//
// Then inject SyncEngine from AppState and call syncEngine.sync() on appear.

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
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    NavigationLink {
                        ProfileView()
                    } label: {
                        Image(systemName: "person.circle")
                            .foregroundStyle(AppTokens.Color.textPrimary)
                            .accessibilityLabel("Account")
                    }
                }
            }
        }
    }
}
