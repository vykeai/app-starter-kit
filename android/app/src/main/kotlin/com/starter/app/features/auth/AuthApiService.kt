package com.starter.app.features.auth

import retrofit2.http.Body
import retrofit2.http.POST

data class RequestMagicLinkBody(val email: String)
data class VerifyMagicLinkBody(val email: String, val code: String)
data class AuthResponse(val accessToken: String, val refreshToken: String)
data class MessageResponse(val message: String)

interface AuthApiService {
    @POST("auth/magic-link/request")
    suspend fun requestMagicLink(@Body body: RequestMagicLinkBody): MessageResponse

    @POST("auth/magic-link/verify")
    suspend fun verifyMagicLink(@Body body: VerifyMagicLinkBody): AuthResponse
}
