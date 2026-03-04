package com.starter.app.core.di

import com.starter.app.core.network.ApiClient
import com.starter.app.features.auth.AuthApiService
import com.starter.app.features.auth.AuthRepository
import com.starter.app.features.auth.AuthRepositoryImpl
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
    }
}
