package com.appstarterkit.app.features.auth

import com.appstarterkit.app.core.storage.SecurePreferences
import javax.inject.Inject

interface AuthRepository {
    suspend fun requestMagicLink(email: String): Result<Unit>
    suspend fun verifyMagicLink(email: String, code: String): Result<AuthResponse>
    /** Invalidates the server session and clears all locally stored tokens. */
    suspend fun logout()
    suspend fun authenticateWithSocial(provider: String, idToken: String): Result<Unit>
}

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val securePreferences: SecurePreferences,
) : AuthRepository {
    override suspend fun requestMagicLink(email: String): Result<Unit> = runCatching {
        apiService.requestMagicLink(RequestMagicLinkBody(email))
    }

    override suspend fun verifyMagicLink(email: String, code: String): Result<AuthResponse> = runCatching {
        val response = apiService.verifyMagicLink(VerifyMagicLinkBody(email, code))
        securePreferences.saveAccessToken(response.accessToken)
        securePreferences.saveRefreshToken(response.refreshToken)
        response
    }

    override suspend fun logout() {
        // Fire-and-forget: tell the server to invalidate the session.
        // Ignore any errors — the local state is always cleared regardless.
        runCatching { apiService.deleteSession() }
        securePreferences.clearAll()
    }

    override suspend fun authenticateWithSocial(provider: String, idToken: String): Result<Unit> = runCatching {
        val response = apiService.authenticateWithSocial(SocialAuthRequest(provider, idToken))
        securePreferences.saveAccessToken(response.accessToken)
        securePreferences.saveRefreshToken(response.refreshToken)
    }
}
