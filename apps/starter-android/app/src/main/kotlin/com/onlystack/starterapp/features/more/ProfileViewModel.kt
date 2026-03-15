package com.onlystack.starterapp.features.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlystack.starterapp.BuildConfig
import com.onlystack.starterapp.core.billing.BillingRepository
import com.onlystack.starterapp.core.billing.EntitlementState
import com.onlystack.starterapp.core.media.MediaAsset
import com.onlystack.starterapp.core.media.MediaRepository
import com.onlystack.starterapp.core.notifications.NotificationPreferences
import com.onlystack.starterapp.core.notifications.PushRegistrationManager
import com.onlystack.starterapp.core.notifications.NotificationRepository
import com.onlystack.starterapp.core.user.UserProfile
import com.onlystack.starterapp.core.user.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserProfile? = null,
    val notifications: NotificationPreferences? = null,
    val entitlement: EntitlementState? = null,
    val mediaAssets: List<MediaAsset> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isUploadingMedia: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val billingRepository: BillingRepository,
    private val mediaRepository: MediaRepository,
    private val pushRegistrationManager: PushRegistrationManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        if (_uiState.value.isLoading || _uiState.value.user != null) return
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val userResult = userRepository.fetchMe()
            val notificationsResult = notificationRepository.fetchPreferences()
            val entitlementResult = billingRepository.fetchEntitlements()
            val mediaResult = mediaRepository.listAssets()

            _uiState.update {
                it.copy(
                    user = userResult.getOrNull() ?: it.user,
                    notifications = notificationsResult.getOrNull() ?: it.notifications,
                    entitlement = entitlementResult.getOrNull() ?: it.entitlement,
                    mediaAssets = mediaResult.getOrNull() ?: it.mediaAssets,
                    isLoading = false,
                    errorMessage = userResult.exceptionOrNull()?.message
                        ?: notificationsResult.exceptionOrNull()?.message
                        ?: entitlementResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun saveDisplayName(displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            userRepository.updateDisplayName(displayName)
                .onSuccess { profile ->
                    _uiState.update { it.copy(user = profile, isSaving = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = error.message) }
                }
        }
    }

    fun setPushEnabled(enabled: Boolean) {
        updateNotifications(pushEnabled = enabled)
    }

    fun setEmailEnabled(enabled: Boolean) {
        updateNotifications(emailEnabled = enabled)
    }

    fun simulateAvatarUpload() {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingMedia = true, errorMessage = null) }

            mediaRepository.prepareUpload(
                kind = "avatar",
                mimeType = "image/webp",
                fileName = "profile-photo.webp",
                sizeBytes = 262_144,
                visibility = "public",
            ).onSuccess { preparation ->
                mediaRepository.completeUpload(
                    assetId = preparation.assetId,
                    width = 1024,
                    height = 1024,
                )
                val mediaResult = mediaRepository.listAssets()
                _uiState.update {
                    it.copy(
                        mediaAssets = mediaResult.getOrDefault(it.mediaAssets),
                        isUploadingMedia = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isUploadingMedia = false, errorMessage = error.message) }
            }
        }
    }

    private fun updateNotifications(
        pushEnabled: Boolean? = null,
        emailEnabled: Boolean? = null,
    ) {
        viewModelScope.launch {
            notificationRepository.updatePreferences(
                pushEnabled = pushEnabled,
                emailEnabled = emailEnabled,
            ).onSuccess { preferences ->
                if (BuildConfig.RUNTIME_FIXTURE_MODE) {
                    if (pushEnabled == true) {
                        pushRegistrationManager.registerCurrentDevice("fixture-android-push-token")
                    } else if (pushEnabled == false) {
                        pushRegistrationManager.revokeCurrentDevice("fixture-android-push-token")
                    }
                }
                _uiState.update { it.copy(notifications = preferences) }
            }.onFailure { error ->
                _uiState.update { it.copy(errorMessage = error.message) }
            }
        }
    }
}
