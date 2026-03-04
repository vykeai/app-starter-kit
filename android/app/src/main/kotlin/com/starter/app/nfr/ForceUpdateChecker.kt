package com.starter.app.nfr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.starter.app.core.network.ApiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject

data class VersionCheckResponse(
    val isUpdateRequired: Boolean,
    val isUpdateRecommended: Boolean,
    val minimumVersion: String,
    val latestVersion: String,
)

interface VersionCheckApiService {
    @GET("app/version-check")
    suspend fun checkVersion(@Query("platform") platform: String, @Query("version") version: String): VersionCheckResponse
}

data class ForceUpdateState(
    val isHardUpdateRequired: Boolean = false,
    val isSoftUpdateAvailable: Boolean = false,
    val isSoftBannerDismissed: Boolean = false,
    val latestVersion: String = "",
)

@HiltViewModel
class ForceUpdateViewModel @Inject constructor(
    private val apiClient: ApiClient,
) : ViewModel() {
    private val _state = MutableStateFlow(ForceUpdateState())
    val state: StateFlow<ForceUpdateState> = _state.asStateFlow()

    private val versionCheckService by lazy { apiClient.create<VersionCheckApiService>() }

    fun checkForUpdate() {
        viewModelScope.launch {
            try {
                val response = versionCheckService.checkVersion("android", "1.0.0")
                _state.value = _state.value.copy(
                    isHardUpdateRequired = response.isUpdateRequired,
                    isSoftUpdateAvailable = response.isUpdateRecommended,
                    latestVersion = response.latestVersion,
                )
            } catch (_: Exception) {
                // Silent failure
            }
        }
    }

    fun dismissSoftUpdate() {
        _state.value = _state.value.copy(isSoftBannerDismissed = true)
    }
}
