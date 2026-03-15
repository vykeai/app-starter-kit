import SwiftUI

struct AppCard<Content: View>: View {
    let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        content
            .padding(AppTokens.Spacing.md)
            .background(AppTokens.Color.surface)
            .cornerRadius(AppTokens.Radius.md)
    }
}
