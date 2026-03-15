package com.onlystack.starterapp.core.network

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
            method == "GET" && path.endsWith("/user/me") -> {
                200 to """
                {
                  "id": "fixture-user",
                  "email": "test@example.com",
                  "displayName": "Starter Test User",
                  "preferences": {
                    "theme": "moss",
                    "pushMarketingEnabled": false,
                    "pushActivityEnabled": true,
                    "pushTransactionalEnabled": true,
                    "pushSystemEnabled": true,
                    "emailNotificationsEnabled": true
                  }
                }
                """.trimIndent()
            }
            method == "PATCH" && path.endsWith("/user/me") -> {
                200 to """
                {
                  "id": "fixture-user",
                  "email": "test@example.com",
                  "displayName": "Starter Test User",
                  "preferences": {
                    "theme": "moss",
                    "pushMarketingEnabled": false,
                    "pushActivityEnabled": true,
                    "pushTransactionalEnabled": true,
                    "pushSystemEnabled": true,
                    "emailNotificationsEnabled": true
                  }
                }
                """.trimIndent()
            }
            method == "GET" && path.endsWith("/notifications/preferences") -> {
                200 to """
                {
                  "theme": "moss",
                  "pushMarketingEnabled": false,
                  "pushActivityEnabled": true,
                  "pushTransactionalEnabled": true,
                  "pushSystemEnabled": true,
                  "emailNotificationsEnabled": true,
                  "pushEnabled": true,
                  "emailEnabled": true,
                  "enabledCategories": ["activity", "transactional"],
                  "quietHoursEnabled": true,
                  "quietHoursStart": "22:00",
                  "quietHoursEnd": "07:00",
                  "urgentBreaksQuietHours": true,
                  "batchSoonNotifications": false,
                  "updatedAt": "2026-03-15T00:00:00.000Z"
                }
                """.trimIndent()
            }
            method == "PATCH" && path.endsWith("/notifications/preferences") -> {
                200 to """
                {
                  "theme": "moss",
                  "pushMarketingEnabled": false,
                  "pushActivityEnabled": true,
                  "pushTransactionalEnabled": true,
                  "pushSystemEnabled": true,
                  "emailNotificationsEnabled": true,
                  "pushEnabled": true,
                  "emailEnabled": true,
                  "enabledCategories": ["activity", "transactional"],
                  "quietHoursEnabled": true,
                  "quietHoursStart": "22:00",
                  "quietHoursEnd": "07:00",
                  "urgentBreaksQuietHours": true,
                  "batchSoonNotifications": false,
                  "updatedAt": "2026-03-15T00:00:00.000Z"
                }
                """.trimIndent()
            }
            method == "GET" && path.endsWith("/billing/entitlements") -> {
                200 to """
                {
                  "tier": "tracker",
                  "source": "android",
                  "features": ["auth-core", "notifications-core", "billing-core"],
                  "renewsAt": "2026-04-14T12:00:00Z"
                }
                """.trimIndent()
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
