package com.appstarterkit.app.core.di

import com.appstarterkit.app.core.network.ApiClient
import com.appstarterkit.app.core.notifications.NotificationApiService
import com.appstarterkit.app.features.auth.AuthApiService
import com.appstarterkit.app.features.auth.AuthRepository
import com.appstarterkit.app.features.auth.AuthRepositoryImpl
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
    }
}
