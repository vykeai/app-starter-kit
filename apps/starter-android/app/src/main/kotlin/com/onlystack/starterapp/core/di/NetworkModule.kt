package com.onlystack.starterapp.core.di

import com.onlystack.starterapp.core.network.ApiClient
import com.onlystack.starterapp.core.notifications.NotificationApiService
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
    }
}
