import SwiftUI

enum AppButtonStyle {
    case primary
    case secondary
    case destructive
}

struct AppButton: View {
    let label: String
    let isLoading: Bool
    let style: AppButtonStyle
    let action: () async -> Void

    init(
        label: String,
        isLoading: Bool = false,
        style: AppButtonStyle = .primary,
        action: @escaping () async -> Void
    ) {
        self.label = label
        self.isLoading = isLoading
        self.style = style
        self.action = action
    }

    var body: some View {
        Button {
            let feedbackStyle: UIImpactFeedbackGenerator.FeedbackStyle = style == .destructive ? .rigid : .medium
            HapticsHelper.impact(feedbackStyle)
            Task { await action() }
        } label: {
            ZStack {
                if isLoading {
                    ProgressView()
                        .tint(labelColor)
                } else {
                    Text(label)
                        .font(.body.bold())
                        .foregroundStyle(labelColor)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(backgroundColor)
            .cornerRadius(AppTokens.Radius.md)
            .overlay(
                RoundedRectangle(cornerRadius: AppTokens.Radius.md)
                    .stroke(borderColor, lineWidth: style == .secondary ? 1.5 : 0)
            )
        }
        .disabled(isLoading)
    }

    private var backgroundColor: SwiftUI.Color {
        switch style {
        case .primary: return AppTokens.Color.primary
        case .secondary: return .clear
        case .destructive: return AppTokens.Color.error
        }
    }

    private var labelColor: SwiftUI.Color {
        switch style {
        case .primary: return .white
        case .secondary: return AppTokens.Color.primary
        case .destructive: return .white
        }
    }

    private var borderColor: SwiftUI.Color {
        style == .secondary ? AppTokens.Color.primary : .clear
    }
}
