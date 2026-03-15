package com.onlystack.starterapp.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlystack.starterapp.BuildConfig
import com.onlystack.starterapp.core.auth.GoogleSignInResult
import com.onlystack.starterapp.core.notifications.PushRegistrationManager
import com.onlystack.starterapp.core.subscription.SubscriptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isBootstrappingSession: Boolean = true,
    val errorMessage: String? = null,
    val isCodeSent: Boolean = false,
    val isAuthenticated: Boolean = false,
    val pendingCode: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val subscriptionManager: SubscriptionManager,
    private val pushRegistrationManager: PushRegistrationManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private var lastEmail: String? = null

    init {
        bootstrapSession()
    }

    fun requestCode(email: String) {
        if (email.isBlank()) return
        lastEmail = email.trim()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.requestMagicLink(email)
                .onSuccess { _uiState.value = _uiState.value.copy(isLoading = false, isCodeSent = true) }
                .onFailure { _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = it.message) }
        }
    }

    fun verifyCode(email: String, code: String) {
        lastEmail = email
        verifyMagicLink(email = email, code = code)
    }

    fun verifyLinkToken(linkToken: String) {
        verifyMagicLink(linkToken = linkToken)
    }

    fun primePendingCode(code: String) {
        val normalized = code.filter { it.isDigit() }.take(8)
        _uiState.update { it.copy(pendingCode = normalized.ifEmpty { null }) }
    }

    fun clearPendingCode() {
        _uiState.update { it.copy(pendingCode = null) }
    }

    fun resetCodeSent() {
        _uiState.update { it.copy(isCodeSent = false) }
    }

    fun rememberEmail(email: String) {
        if (email.isNotBlank()) {
            lastEmail = email
        }
    }

    fun lastRequestedEmail(): String? = lastEmail

    private fun bootstrapSession() {
        viewModelScope.launch {
            if (!authRepository.hasStoredSession()) {
                _uiState.update { it.copy(isBootstrappingSession = false) }
                return@launch
            }

            authRepository.fetchCurrentUser()
                .onSuccess {
                    refreshAuthenticatedServices()
                    _uiState.update {
                        it.copy(
                            isAuthenticated = true,
                            isBootstrappingSession = false,
                            errorMessage = null,
                        )
                    }
                }
                .onFailure {
                    authRepository.logout()
                    _uiState.update {
                        it.copy(
                            isAuthenticated = false,
                            isBootstrappingSession = false,
                            errorMessage = null,
                        )
                    }
                }
        }
    }

    private fun verifyMagicLink(
        email: String? = null,
        code: String? = null,
        linkToken: String? = null,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            authRepository.verifyMagicLink(email = email, code = code, linkToken = linkToken)
                .onSuccess {
                    refreshAuthenticatedServices()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        pendingCode = null,
                    )
                }
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
            clearAuthenticatedServices()
            authRepository.logout()
            // Reset UI state so a fresh login flow starts cleanly.
            _uiState.value = AuthUiState(isBootstrappingSession = false)
            lastEmail = null
            onComplete()
        }
    }

    fun onGoogleSignInResult(result: GoogleSignInResult) {
        when (result) {
            is GoogleSignInResult.Success -> {
                viewModelScope.launch {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    authRepository.authenticateWithSocial("google", result.credential.idToken)
                        .onSuccess {
                            refreshAuthenticatedServices()
                            _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                        }
                        .onFailure { e -> _uiState.update { it.copy(isLoading = false, errorMessage = e.message) } }
                }
            }
            is GoogleSignInResult.Cancelled -> { /* ignore */ }
            is GoogleSignInResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.message) }
        }
    }

    private fun refreshAuthenticatedServices() {
        viewModelScope.launch {
            subscriptionManager.refreshEntitlements()
            if (BuildConfig.RUNTIME_FIXTURE_MODE) {
                pushRegistrationManager.registerCurrentDevice("fixture-android-push-token")
            }
        }
    }

    private suspend fun clearAuthenticatedServices() {
        subscriptionManager.resetEntitlements()
        if (BuildConfig.RUNTIME_FIXTURE_MODE) {
            pushRegistrationManager.revokeCurrentDevice("fixture-android-push-token")
        }
    }
}
