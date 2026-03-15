import SwiftUI

struct AppTextField: View {
    let label: String
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    var isSecure: Bool = false
    var errorMessage: String? = nil

    var body: some View {
        VStack(alignment: .leading, spacing: AppTokens.Spacing.xs) {
            Text(label)
                .font(.caption.bold())
                .foregroundStyle(AppTokens.Color.textSecondary)

            Group {
                if isSecure {
                    SecureField(placeholder, text: $text)
                } else {
                    TextField(placeholder, text: $text)
                        .keyboardType(keyboardType)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                }
            }
            .padding(AppTokens.Spacing.md)
            .background(AppTokens.Color.surface)
            .cornerRadius(AppTokens.Radius.md)
            .overlay(
                RoundedRectangle(cornerRadius: AppTokens.Radius.md)
                    .stroke(
                        errorMessage != nil ? AppTokens.Color.error : Color.white.opacity(0.1),
                        lineWidth: 1
                    )
            )
            .foregroundStyle(AppTokens.Color.textPrimary)

            if let error = errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(AppTokens.Color.error)
            }
        }
    }
}
