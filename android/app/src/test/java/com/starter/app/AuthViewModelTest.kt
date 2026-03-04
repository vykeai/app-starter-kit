package com.starter.app

import com.starter.app.features.auth.AuthRepository
import com.starter.app.features.auth.AuthResponse
import com.starter.app.features.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(dispatcher)
        authRepository = mockk()
        viewModel = AuthViewModel(authRepository)
    }

    @AfterEach
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `requestCode success sets isCodeSent true`() = runTest {
        coEvery { authRepository.requestMagicLink(any()) } returns Result.success(Unit)
        viewModel.requestCode("test@example.com")
        dispatcher.scheduler.advanceUntilIdle()
        assert(viewModel.uiState.value.isCodeSent)
    }

    @Test
    fun `verifyCode success sets isAuthenticated true`() = runTest {
        coEvery { authRepository.verifyMagicLink(any(), any()) } returns Result.success(
            AuthResponse("access_token", "refresh_token")
        )
        viewModel.verifyCode("test@example.com", "12345678")
        dispatcher.scheduler.advanceUntilIdle()
        assert(viewModel.uiState.value.isAuthenticated)
    }
}
