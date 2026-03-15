package com.onlystack.starterapp.core.user

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApiService {
    @GET("user/me")
    suspend fun fetchMe(): UserProfile

    @PATCH("user/me")
    suspend fun updateMe(
        @Body body: UpdateUserProfileBody,
    ): UserProfile
}
