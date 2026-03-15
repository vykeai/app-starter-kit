import SwiftUI

struct MainTabView: View {
    @Environment(AppState.self) private var appState

    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("Home", systemImage: "house.fill")
                }
        }
        .preferredColorScheme(.dark)
        .onAppear {
            if case .home = appState.pendingRoute {
                appState.pendingRoute = nil
            }
        }
    }
}
