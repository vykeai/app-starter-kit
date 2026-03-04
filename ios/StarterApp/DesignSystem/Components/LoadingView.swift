import SwiftUI

struct LoadingView: View {
    var body: some View {
        ProgressView()
            .tint(AppTokens.Color.primary)
            .scaleEffect(1.5)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AppTokens.Color.background.opacity(0.8))
    }
}
