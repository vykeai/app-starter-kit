package com.onlystack.starterapp.core.user

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiService: UserApiService,
) {
    suspend fun fetchMe(): Result<UserProfile> = runCatching {
        apiService.fetchMe()
    }

    suspend fun updateDisplayName(displayName: String): Result<UserProfile> = runCatching {
        apiService.updateMe(UpdateUserProfileBody(displayName = displayName))
    }
}
