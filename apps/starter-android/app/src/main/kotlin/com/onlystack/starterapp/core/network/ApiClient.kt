package com.onlystack.starterapp.core.network

import com.onlystack.starterapp.BuildConfig
import com.onlystack.starterapp.core.storage.SecurePreferences
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.UUID
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val securePreferences: SecurePreferences,
    private val tokenAuthenticator: TokenAuthenticator,
    private val fixtureInterceptor: FixtureInterceptor,
) {
    private val authInterceptor = Interceptor { chain ->
        val token = securePreferences.getAccessToken()
        val requestBuilder = chain.request().newBuilder()
            .header("X-Request-Id", "android-${UUID.randomUUID()}")

        val request = if (token != null) {
            requestBuilder
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            requestBuilder.build()
        }
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.RUNTIME_FIXTURE_MODE) {
                addInterceptor(fixtureInterceptor)
            }
        }
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL + "/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    inline fun <reified T> create(): T = retrofit.create(T::class.java)
}
