import SwiftUI

struct AuthFlowView: View {
    @State private var viewModel = AuthViewModel()
    @Environment(AppState.self) private var appState

    var body: some View {
        Group {
            switch viewModel.step {
            case .welcome:
                WelcomeView(viewModel: viewModel)
            case .enterEmail:
                EmailInputView(viewModel: viewModel)
            case .enterCode(let email):
                CodeEntryView(viewModel: viewModel, email: email) {
                    appState.isAuthenticated = true
                }
            }
        }
        .background(AppTokens.Color.background.ignoresSafeArea())
    }
}
