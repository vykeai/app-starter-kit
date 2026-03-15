package com.appstarterkit.app

import com.appstarterkit.app.features.auth.AuthRepository
import com.appstarterkit.app.features.auth.AuthResponse
import com.appstarterkit.app.features.auth.AuthViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
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

    // -------------------------------------------------------------------------
    // requestCode / requestMagicLink
    // -------------------------------------------------------------------------

    @Test
    fun `requestMagicLink_success_updatesStateToCodeSent`() = runTest {
        coEvery { authRepository.requestMagicLink(any()) } returns Result.success(Unit)

        viewModel.requestCode("test@example.com")

        val state = viewModel.uiState.value
        assertTrue(state.isCodeSent, "isCodeSent should be true after a successful magic link request")
        assertFalse(state.isLoading, "isLoading should be false after the call completes")
        assertNull(state.errorMessage, "errorMessage should be null on success")
    }

    @Test
    fun `requestMagicLink_emptyEmail_doesNotCallRepository`() = runTest {
        // No mock setup — if the repository is called, mockk will throw, which
        // would fail the test. This verifies the guard inside requestCode().
        viewModel.requestCode("")

        coVerify(exactly = 0) { authRepository.requestMagicLink(any()) }

        val state = viewModel.uiState.value
        assertFalse(state.isCodeSent, "isCodeSent should remain false for an empty email")
        assertFalse(state.isLoading, "isLoading should remain false for an empty email")
    }

    // -------------------------------------------------------------------------
    // verifyCode / verifyMagicLink
    // -------------------------------------------------------------------------

    @Test
    fun `verifyMagicLink_success_updatesStateToAuthenticated`() = runTest {
        coEvery { authRepository.verifyMagicLink(any(), any(), any()) } returns Result.success(
            AuthResponse(accessToken = "access_abc", refreshToken = "refresh_xyz"),
        )

        viewModel.verifyCode("test@example.com", "12345678")

        val state = viewModel.uiState.value
        assertTrue(state.isAuthenticated, "isAuthenticated should be true after successful verification")
        assertFalse(state.isLoading, "isLoading should be false after the call completes")
        assertNull(state.errorMessage, "errorMessage should be null on success")
    }

    @Test
    fun `verifyMagicLink_failure_setsErrorState`() = runTest {
        coEvery { authRepository.verifyMagicLink(any(), any(), any()) } returns Result.failure(
            RuntimeException("401 Unauthorized"),
        )

        viewModel.verifyCode("test@example.com", "00000000")

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated, "isAuthenticated should remain false on failure")
        assertFalse(state.isLoading, "isLoading should be false after the call completes")
        assertNotNull(state.errorMessage, "errorMessage should be set on failure")
        assertEquals("Invalid code. Please try again.", state.errorMessage)
    }
}
