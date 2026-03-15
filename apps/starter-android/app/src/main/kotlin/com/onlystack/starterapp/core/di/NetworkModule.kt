package com.onlystack.starterapp.core.di

import com.onlystack.starterapp.core.network.ApiClient
import com.onlystack.starterapp.core.analytics.AnalyticsApiService
import com.onlystack.starterapp.core.billing.BillingApiService
import com.onlystack.starterapp.core.media.MediaApiService
import com.onlystack.starterapp.core.notifications.NotificationApiService
import com.onlystack.starterapp.core.user.UserApiService
import com.onlystack.starterapp.features.auth.AuthApiService
import com.onlystack.starterapp.features.auth.AuthRepository
import com.onlystack.starterapp.features.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    companion object {
        @Provides
        @Singleton
        fun provideAuthApiService(apiClient: ApiClient): AuthApiService =
            apiClient.create()

        @Provides
        @Singleton
        fun provideNotificationApiService(apiClient: ApiClient): NotificationApiService =
            apiClient.create()

        @Provides
        @Singleton
        fun provideUserApiService(apiClient: ApiClient): UserApiService =
            apiClient.create()

        @Provides
        @Singleton
        fun provideMediaApiService(apiClient: ApiClient): MediaApiService =
            apiClient.create()

        @Provides
        @Singleton
        fun provideBillingApiService(apiClient: ApiClient): BillingApiService =
            apiClient.create()

        @Provides
        @Singleton
        fun provideAnalyticsApiService(apiClient: ApiClient): AnalyticsApiService =
            apiClient.create()
    }
}
