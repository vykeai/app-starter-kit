package com.onlystack.starterapp

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.onlystack.starterapp.features.auth.AuthApiService
import com.onlystack.starterapp.features.auth.RequestMagicLinkBody
import kotlinx.coroutines.test.runTest

class MockWebServerTest {
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

    @Test
    fun `requestMagicLink sends POST to correct path`() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("{\"message\":\"Code sent\"}").setResponseCode(200))
        authApiService.requestMagicLink(RequestMagicLinkBody("test@example.com"))
        val request = mockWebServer.takeRequest()
        assert(request.path?.contains("auth/magic-link/request") == true)
    }
}
