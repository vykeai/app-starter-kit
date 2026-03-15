import SwiftUI

// MARK: - Toast model types

enum ToastStyle {
    case success
    case error
    case info
    case warning

    var iconName: String {
        switch self {
        case .success: return "checkmark.circle.fill"
        case .error:   return "xmark.circle.fill"
        case .info:    return "info.circle.fill"
        case .warning: return "exclamationmark.triangle.fill"
        }
    }

    var tintColor: SwiftUI.Color {
        switch self {
        case .success: return AppTokens.Color.success
        case .error:   return AppTokens.Color.error
        case .info:    return AppTokens.Color.primary
        case .warning: return AppTokens.Color.warning
        }
    }
}

struct ToastMessage: Identifiable {
    let id = UUID()
    let message: String
    let style: ToastStyle
    var duration: Double = 3.0
}

// MARK: - ToastManager

@Observable
final class ToastManager {
    var current: ToastMessage? = nil

    private var dismissTask: Task<Void, Never>?

    /// Displays a toast, cancelling any currently-visible one first.
    func show(_ message: String, style: ToastStyle = .info, duration: Double = 3.0) {
        dismissTask?.cancel()
        current = ToastMessage(message: message, style: style, duration: duration)
        dismissTask = Task { @MainActor in
            try? await Task.sleep(for: .seconds(duration))
            if !Task.isCancelled {
                withAnimation(.easeInOut(duration: 0.3)) {
                    current = nil
                }
            }
        }
    }

    func dismiss() {
        dismissTask?.cancel()
        withAnimation(.easeInOut(duration: 0.3)) {
            current = nil
        }
    }
}

// MARK: - Toast view

private struct ToastView: View {
    let toast: ToastMessage

    var body: some View {
        HStack(spacing: AppTokens.Spacing.sm) {
            Image(systemName: toast.style.iconName)
                .foregroundStyle(toast.style.tintColor)
                .font(.system(size: 18, weight: .semibold))

            Text(toast.message)
                .font(.subheadline.weight(.medium))
                .foregroundStyle(AppTokens.Color.textPrimary)
                .multilineTextAlignment(.leading)
                .lineLimit(3)
        }
        .padding(.horizontal, AppTokens.Spacing.md)
        .padding(.vertical, AppTokens.Spacing.sm + 2)
        .background(AppTokens.Color.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: AppTokens.Radius.md))
        .shadow(color: .black.opacity(0.4), radius: 8, y: 4)
        .padding(.horizontal, AppTokens.Spacing.md)
    }
}

// MARK: - ToastOverlay ViewModifier

struct ToastOverlay: ViewModifier {
    @Environment(ToastManager.self) private var toastManager

    func body(content: Content) -> some View {
        content
            .overlay(alignment: .top) {
                if let toast = toastManager.current {
                    ToastView(toast: toast)
                        .transition(
                            .asymmetric(
                                insertion: .move(edge: .top).combined(with: .opacity),
                                removal: .move(edge: .top).combined(with: .opacity)
                            )
                        )
                        .onTapGesture {
                            HapticsHelper.selection()
                            toastManager.dismiss()
                        }
                        .padding(.top, AppTokens.Spacing.sm)
                        .zIndex(999)
                }
            }
            .animation(.spring(response: 0.4, dampingFraction: 0.75), value: toastManager.current?.id)
    }
}

extension View {
    func toastOverlay() -> some View {
        modifier(ToastOverlay())
    }
}
