import SwiftUI

struct RootView: View {
    @Environment(AppState.self) private var appState

    var body: some View {
        ZStack(alignment: .top) {
            Group {
                if appState.forceUpdateChecker.isHardUpdateRequired {
                    HardUpdateView()
                } else if appState.isAuthenticated {
                    MainTabView()
                } else {
                    AuthFlowView()
                }
            }

            if !appState.networkMonitor.isConnected {
                OfflineBanner()
                    .transition(.move(edge: .top))
            }

            if appState.forceUpdateChecker.isSoftUpdateAvailable &&
               !appState.forceUpdateChecker.isHardUpdateRequired &&
               !appState.forceUpdateChecker.isSoftBannerDismissed {
                SoftUpdateBanner()
                    .transition(.move(edge: .top))
            }
        }
        .animation(.easeInOut(duration: 0.3), value: appState.networkMonitor.isConnected)
        .task {
            await appState.forceUpdateChecker.checkForUpdate()
        }
    }
}
