import SwiftUI

// MARK: - Shimmer ViewModifier

struct ShimmerModifier: ViewModifier {
    let isLoading: Bool

    @State private var phase: CGFloat = -1

    func body(content: Content) -> some View {
        if isLoading {
            content
                .redacted(reason: .placeholder)
                .overlay(
                    GeometryReader { geometry in
                        let width = geometry.size.width
                        let gradient = LinearGradient(
                            stops: [
                                .init(color: .clear,                            location: 0),
                                .init(color: .white.opacity(0.12),              location: 0.35),
                                .init(color: .white.opacity(0.28),              location: 0.50),
                                .init(color: .white.opacity(0.12),              location: 0.65),
                                .init(color: .clear,                            location: 1),
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )

                        gradient
                            // The gradient is 3× the view width so it sweeps fully across.
                            .frame(width: width * 3)
                            // `phase` goes from -1 → +1; multiply by view width so the
                            // gradient travels one full width from left edge to right edge.
                            .offset(x: phase * width)
                    }
                    .clipped()
                    .allowsHitTesting(false)
                )
                .onAppear {
                    withAnimation(
                        .linear(duration: 1.5)
                        .repeatForever(autoreverses: false)
                    ) {
                        phase = 1
                    }
                }
                .onDisappear {
                    // Reset so the animation starts fresh if the view reappears.
                    phase = -1
                }
        } else {
            content
        }
    }
}

extension View {
    /// Applies an animated shimmer gradient mask over the view while `isLoading` is true.
    ///
    /// Usage:
    /// ```swift
    /// Text("Loading...")
    ///     .shimmer(when: viewModel.isLoading)
    /// ```
    func shimmer(when isLoading: Bool) -> some View {
        modifier(ShimmerModifier(isLoading: isLoading))
    }
}

// MARK: - ShimmerRow

/// A skeleton placeholder row that mimics a typical list cell layout.
/// Drop it into a `List` or `VStack` while real data is loading.
///
/// Layout:  [circle avatar] [wide title rect]
///                          [narrow subtitle rect]
struct ShimmerRow: View {
    var body: some View {
        HStack(spacing: AppTokens.Spacing.md) {
            // Avatar placeholder
            Circle()
                .fill(AppTokens.Color.surfaceElevated)
                .frame(width: 44, height: 44)

            VStack(alignment: .leading, spacing: AppTokens.Spacing.xs) {
                // Title placeholder
                RoundedRectangle(cornerRadius: AppTokens.Radius.sm)
                    .fill(AppTokens.Color.surfaceElevated)
                    .frame(maxWidth: .infinity)
                    .frame(height: 14)

                // Subtitle placeholder — slightly narrower for a realistic look
                RoundedRectangle(cornerRadius: AppTokens.Radius.sm)
                    .fill(AppTokens.Color.surfaceElevated)
                    .frame(maxWidth: 160)
                    .frame(height: 12)
            }
        }
        .padding(.vertical, AppTokens.Spacing.xs)
        .shimmer(when: true)
    }
}

// MARK: - Preview

#Preview("Shimmer Row") {
    VStack(spacing: 0) {
        ForEach(0..<6, id: \.self) { _ in
            ShimmerRow()
                .padding(.horizontal, AppTokens.Spacing.md)
            Divider()
                .background(AppTokens.Color.surfaceElevated)
        }
    }
    .background(AppTokens.Color.background)
}

#Preview("Shimmer on Text") {
    VStack(spacing: AppTokens.Spacing.lg) {
        Text("Loading content...")
            .font(.title2.bold())
            .foregroundStyle(AppTokens.Color.textPrimary)
            .shimmer(when: true)

        Text("This text is not loading")
            .font(.body)
            .foregroundStyle(AppTokens.Color.textSecondary)
            .shimmer(when: false)
    }
    .padding(AppTokens.Spacing.lg)
    .background(AppTokens.Color.background)
}
