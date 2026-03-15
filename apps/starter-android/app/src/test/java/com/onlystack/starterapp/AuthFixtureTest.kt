package com.onlystack.starterapp

// Tests that fixture JSON files match the shapes expected by the Retrofit API service.
// Uses MockWebServer to intercept HTTP at the network layer — same decoding path as production.
// No MockAPIClient. If the backend changes the response shape, these tests break immediately.

import com.onlystack.starterapp.features.auth.AuthApiService
import com.onlystack.starterapp.features.auth.RequestMagicLinkBody
import com.onlystack.starterapp.features.auth.VerifyMagicLinkBody
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthFixtureTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var authApiService: AuthApiService

    @BeforeEach
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        authApiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApiService::class.java)
    }

    @AfterEach
    fun teardown() {
        mockWebServer.shutdown()
    }

    private fun readFixture(name: String): String =
        javaClass.classLoader!!
            .getResourceAsStream("fixtures/$name")!!
            .bufferedReader()
            .readText()

    @Test
    fun `auth-request fixture is parseable by AuthApiService`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(readFixture("auth-request.json"))
                .setResponseCode(200)
        )
        val response = authApiService.requestMagicLink(
            RequestMagicLinkBody("user@example.com")
        )
        assert(response.message == "Code sent") {
            "Expected 'Code sent' but got '${response.message}'"
        }
    }

    @Test
    fun `auth-verify fixture is parseable by AuthApiService`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(readFixture("auth-verify.json"))
                .setResponseCode(200)
        )
        val response = authApiService.verifyMagicLink(
            VerifyMagicLinkBody("user@example.com", "12345678")
        )
        assert(response.accessToken.isNotEmpty()) {
            "accessToken should not be empty"
        }
        assert(response.refreshToken.isNotEmpty()) {
            "refreshToken should not be empty"
        }
    }

    @Test
    fun `request sends correct HTTP method and path`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setBody(readFixture("auth-request.json"))
                .setResponseCode(200)
        )
        authApiService.requestMagicLink(RequestMagicLinkBody("user@example.com"))
        val recordedRequest = mockWebServer.takeRequest()
        assert(recordedRequest.method == "POST") {
            "Expected POST but got ${recordedRequest.method}"
        }
        assert(recordedRequest.path?.contains("auth/magic-link/request") == true) {
            "Unexpected path: ${recordedRequest.path}"
        }
    }
}
