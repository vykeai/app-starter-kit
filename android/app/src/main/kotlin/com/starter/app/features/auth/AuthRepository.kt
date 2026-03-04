package com.starter.app.features.auth

import com.starter.app.core.storage.SecurePreferences
import javax.inject.Inject

interface AuthRepository {
    suspend fun requestMagicLink(email: String): Result<Unit>
    suspend fun verifyMagicLink(email: String, code: String): Result<AuthResponse>
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
}
