package com.starter.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
}
