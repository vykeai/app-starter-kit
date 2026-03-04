import SwiftUI

struct CodeEntryView: View {
    let viewModel: AuthViewModel
    let email: String
    let onSuccess: () -> Void

    @FocusState private var focusedIndex: Int?

    private let digitCount = 8
    @State private var digits: [String] = Array(repeating: "", count: 8)

    var body: some View {
        VStack(spacing: AppTokens.Spacing.xl) {
            Spacer()

            VStack(spacing: AppTokens.Spacing.md) {
                Text("Check your email")
                    .font(.largeTitle.bold())
                    .foregroundStyle(AppTokens.Color.textPrimary)

                Text("Enter the 8-digit code sent to\n\(email)")
                    .font(.body)
                    .foregroundStyle(AppTokens.Color.textSecondary)
                    .multilineTextAlignment(.center)
            }

            HStack(spacing: AppTokens.Spacing.sm) {
                ForEach(0..<digitCount, id: \.self) { index in
                    DigitBox(
                        digit: $digits[index],
                        isFocused: focusedIndex == index,
                        onChanged: { handleInput(at: index, value: $0) }
                    )
                    .focused($focusedIndex, equals: index)
                }
            }
            .padding(.horizontal, AppTokens.Spacing.md)

            if let error = viewModel.errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(AppTokens.Color.error)
            }

            Spacer()

            AppButton(label: "Verify Code", isLoading: viewModel.isLoading) {
                viewModel.codeInput = digits.joined()
                let success = await viewModel.verifyCode(email: email)
                if success { onSuccess() }
            }
            .padding(.horizontal, AppTokens.Spacing.lg)
        }
        .padding(AppTokens.Spacing.lg)
        .background(AppTokens.Color.background.ignoresSafeArea())
        .onAppear { focusedIndex = 0 }
    }

    private func handleInput(at index: Int, value: String) {
        if value.count == digitCount {
            let chars = Array(value.prefix(digitCount)).map(String.init)
            for i in 0..<digitCount { digits[i] = chars[i] }
            focusedIndex = nil
            return
        }
        digits[index] = String(value.prefix(1))
        if !digits[index].isEmpty && index < digitCount - 1 {
            focusedIndex = index + 1
        }
    }
}

private struct DigitBox: View {
    @Binding var digit: String
    let isFocused: Bool
    let onChanged: (String) -> Void

    var body: some View {
        TextField("", text: $digit)
            .keyboardType(.numberPad)
            .multilineTextAlignment(.center)
            .font(.title2.bold())
            .foregroundStyle(AppTokens.Color.textPrimary)
            .frame(width: 36, height: 48)
            .background(AppTokens.Color.surface)
            .cornerRadius(AppTokens.Radius.sm)
            .overlay(
                RoundedRectangle(cornerRadius: AppTokens.Radius.sm)
                    .stroke(isFocused ? AppTokens.Color.primary : Color.clear, lineWidth: 2)
            )
            .onChange(of: digit) { _, newValue in
                onChanged(newValue)
            }
    }
}
