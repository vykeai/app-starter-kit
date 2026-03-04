package com.appstarterkit.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appstarterkit.app.core.auth.GoogleSignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isCodeSent: Boolean = false,
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun requestCode(email: String) {
        if (email.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.requestMagicLink(email)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, isCodeSent = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
        }
    }

    fun verifyCode(email: String, code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.verifyMagicLink(email, code)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, isAuthenticated = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Invalid code. Please try again.") }
        }
    }

    /**
     * Signs the user out. Calls [AuthRepository.logout] (which fires DELETE /auth/session
     * and clears local tokens), then invokes [onComplete] so the NavHost can navigate
     * back to the welcome screen.
     */
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            // Reset UI state so a fresh login flow starts cleanly.
            _uiState.value = AuthUiState()
            onComplete()
        }
    }

    fun onGoogleSignInResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Success -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    authRepository.authenticateWithSocial("google", result.credential.idToken)
                        .onSuccess { _uiState.update { it.copy(isLoading = false, isAuthenticated = true) } }
                        .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                }
            }
            is GoogleSignInResult.Cancelled -> { /* ignore */ }
            is GoogleSignInResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.message) }
        }
    }
}
