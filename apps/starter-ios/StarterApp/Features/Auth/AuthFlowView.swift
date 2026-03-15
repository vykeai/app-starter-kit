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
                    Task {
                        await appState.completeAuthentication(user: viewModel.authenticatedUser)
                    }
                }
            }
        }
        .background(AppTokens.Color.background.ignoresSafeArea())
        .onChange(of: viewModel.authSucceeded) { _, succeeded in
            if succeeded {
                Task {
                    await appState.completeAuthentication(user: viewModel.authenticatedUser)
                }
            }
        }
        .task {
            await processPendingAuthIfNeeded()
        }
        .onChange(of: appState.pendingAuthLink) { _, _ in
            Task {
                await processPendingAuthIfNeeded()
            }
        }
    }

    @MainActor
    private func processPendingAuthIfNeeded() async {
        guard let pendingAuthLink = appState.pendingAuthLink else { return }
        appState.pendingAuthLink = nil
        let success = await viewModel.consumePendingAuth(pendingAuthLink)
        if success {
            await appState.completeAuthentication(user: viewModel.authenticatedUser)
        }
    }
}
