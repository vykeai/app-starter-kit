package com.appstarterkit.app.core.network

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FixtureInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val method = request.method.uppercase()
        val requestId = request.header("X-Request-Id") ?: "fixture-${UUID.randomUUID()}"

        val (statusCode, body) = when {
            method == "POST" && path.endsWith("/auth/magic-link/request") -> {
                201 to """
                {
                  "message": "Code sent",
                  "deliveryMode": "disabled",
                  "email": "test@example.com",
                  "code": "12345678",
                  "linkToken": "fixture-link-token",
                  "linkUrl": "appstarterkit://auth/verify?email=test%40example.com&code=12345678&linkToken=fixture-link-token"
                }
                """.trimIndent()
            }
            method == "POST" && path.endsWith("/auth/magic-link/verify") -> {
                201 to authResponseJson()
            }
            method == "POST" && path.endsWith("/auth/social") -> {
                201 to authResponseJson()
            }
            method == "POST" && path.endsWith("/auth/refresh") -> {
                201 to """
                {
                  "accessToken": "fixture-access-token",
                  "refreshToken": "fixture-refresh-token"
                }
                """.trimIndent()
            }
            method == "DELETE" && path.endsWith("/auth/session") -> {
                200 to """{"message":"Signed out"}"""
            }
            method == "GET" && path.endsWith("/health") -> {
                200 to """{"status":"ok","timestamp":"2026-03-15T00:00:00.000Z"}"""
            }
            else -> {
                404 to """{"message":"No fixture mapped for $method $path"}"""
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(statusCode)
            .message(if (statusCode < 400) "OK" else "Fixture Not Found")
            .header("Content-Type", "application/json")
            .header("X-Request-Id", requestId)
            .body(body.toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun authResponseJson(): String {
        val user = JSONObject()
            .put("id", "fixture-user")
            .put("email", "test@example.com")
            .put("displayName", "Starter Test User")

        return JSONObject()
            .put("accessToken", "fixture-access-token")
            .put("refreshToken", "fixture-refresh-token")
            .put("user", user)
            .toString()
    }
}
