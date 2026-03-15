package com.appstarterkit.app.features.auth

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST

data class RequestMagicLinkBody(val email: String)
data class VerifyMagicLinkBody(
    val email: String? = null,
    val code: String? = null,
    val linkToken: String? = null,
)
data class AuthResponse(val accessToken: String, val refreshToken: String)
data class MessageResponse(val message: String)
data class SocialAuthRequest(val provider: String, val idToken: String)

interface AuthApiService {
    @POST("auth/magic-link/request")
    suspend fun requestMagicLink(@Body body: RequestMagicLinkBody): MessageResponse

    @POST("auth/magic-link/verify")
    suspend fun verifyMagicLink(@Body body: VerifyMagicLinkBody): AuthResponse

    /** Fire-and-forget session termination. Server invalidates the refresh token. */
    @DELETE("auth/session")
    suspend fun deleteSession(): MessageResponse

    @POST("auth/social")
    suspend fun authenticateWithSocial(@Body body: SocialAuthRequest): AuthResponse
}
