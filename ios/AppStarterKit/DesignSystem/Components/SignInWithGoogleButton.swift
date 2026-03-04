import SwiftUI

/// A SwiftUI button styled per Google's branding guidelines.
/// Uses `g.circle.fill` as a placeholder for the Google "G" logo —
/// replace with an asset image once the real SVG logo is added to Assets.xcassets.
struct SignInWithGoogleButton: View {
    private let googleBlue = Color(red: 0.26, green: 0.52, blue: 0.96)

    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: AppTokens.Spacing.md) {
                Image(systemName: "g.circle.fill")
                    .font(.system(size: 20, weight: .medium))
                    .foregroundStyle(googleBlue)

                Text("Sign in with Google")
                    .font(.system(.body, design: .default).weight(.medium))
                    .foregroundStyle(AppTokens.Color.textPrimary)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 50)
            .background(AppTokens.Color.surface)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(hex: "#DADCE0").opacity(0.6), lineWidth: 1)
            )
            .shadow(color: .black.opacity(0.15), radius: 4, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}
