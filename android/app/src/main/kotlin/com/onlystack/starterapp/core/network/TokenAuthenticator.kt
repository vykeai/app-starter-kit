package com.onlystack.starterapp.core.network

import com.onlystack.starterapp.core.storage.SecurePreferences
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles transparent JWT access-token refresh on HTTP 401 responses.
 *
 * OkHttp calls [authenticate] on every 401. The implementation must be
 * synchronous — no suspend functions, no coroutines — because OkHttp
 * dispatches this on its own thread pool.
 *
 * Flow:
 *  1. Retrieve the refresh token from [SecurePreferences].
 *  2. POST synchronously to /auth/refresh.
 *  3. On success  → persist new tokens, replay the original request with the
 *                    updated Authorization header.
 *  4. On failure  → clear all stored tokens and return null so OkHttp
 *                    propagates the 401 to the caller.
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val securePreferences: SecurePreferences,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite retry loops: if the retry itself returns 401, stop.
        if (responseCount(response) >= 2) {
            securePreferences.clearAll()
            return null
        }

        val refreshToken = securePreferences.getRefreshToken() ?: run {
            securePreferences.clearAll()
            return null
        }

        // Build a lightweight OkHttpClient specifically for the refresh call so
        // we do not go through the authenticator again (which would cause a loop).
        val refreshClient = OkHttpClient()

        val baseUrl = response.request.url.run {
            "${scheme}://${host}${if (port != 80 && port != 443) ":$port" else ""}"
        }

        val body = JSONObject().put("refreshToken", refreshToken)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val refreshRequest = Request.Builder()
            .url("$baseUrl/auth/refresh")
            .post(body)
            .build()

        return try {
            val refreshResponse = refreshClient.newCall(refreshRequest).execute()
            if (!refreshResponse.isSuccessful) {
                securePreferences.clearAll()
                return null
            }

            val responseBody = refreshResponse.body?.string() ?: run {
                securePreferences.clearAll()
                return null
            }

            val json = JSONObject(responseBody)
            val newAccessToken = json.optString("accessToken").takeIf { it.isNotEmpty() } ?: run {
                securePreferences.clearAll()
                return null
            }
            val newRefreshToken = json.optString("refreshToken").takeIf { it.isNotEmpty() }

            securePreferences.saveAccessToken(newAccessToken)
            if (newRefreshToken != null) {
                securePreferences.saveRefreshToken(newRefreshToken)
            }

            // Replay the original request with the freshly obtained access token.
            response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        } catch (_: Exception) {
            securePreferences.clearAll()
            null
        }
    }

    /** Count how many times this request has already been retried. */
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
