package com.onlystack.starterapp.core.deeplink

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class PendingAuthPayload(
    val email: String? = null,
    val code: String? = null,
    val linkToken: String? = null,
)

sealed interface PendingRoute {
    data class Auth(val payload: PendingAuthPayload) : PendingRoute
    data object Home : PendingRoute
}

/**
 * Activity-scoped ViewModel that bridges incoming deep-link intents to the
 * Compose navigation tree.
 *
 * [MainActivity] writes auth payloads extracted from incoming URIs here.
 * [AuthNavHost] consumes them and decides whether to one-tap verify or route
 * to the manual code-entry screen.
 */
@HiltViewModel
class DeepLinkViewModel @Inject constructor() : ViewModel() {

    private val _pendingRoute = MutableStateFlow<PendingRoute?>(null)
    val pendingRoute: StateFlow<PendingRoute?> = _pendingRoute.asStateFlow()

    /**
     * Called from [MainActivity.onCreate] and [MainActivity.onNewIntent].
     *
     * Supported URI patterns:
     *  - https://yourapp.com/auth/verify?email=user@example.com&code=XXXXXXXX&linkToken=...
     *  - appstarterkit://auth/verify?email=user@example.com&code=XXXXXXXX&linkToken=...
     */
    fun handleUri(uri: Uri?) {
        if (uri == null) return
        val isAuthRoute =
            (uri.scheme == "appstarterkit" && uri.host == "auth" && uri.path == "/verify") ||
                ((uri.scheme == "https" || uri.scheme == "http") && uri.path == "/auth/verify")

        if ((uri.scheme == "appstarterkit" && (uri.host == "home" || uri.path == "/home")) ||
            ((uri.scheme == "https" || uri.scheme == "http") && uri.path == "/home")
        ) {
            _pendingRoute.value = PendingRoute.Home
            return
        }

        if (!isAuthRoute) return

        val payload = PendingAuthPayload(
            email = uri.getQueryParameter("email"),
            code = uri.getQueryParameter("code"),
            linkToken = uri.getQueryParameter("linkToken"),
        )
        if (!payload.email.isNullOrBlank() || !payload.code.isNullOrBlank() || !payload.linkToken.isNullOrBlank()) {
            _pendingRoute.value = PendingRoute.Auth(payload)
        }
    }

    /** Must be called after the auth payload has been consumed to avoid re-applying it. */
    fun consumePendingRoute() {
        _pendingRoute.value = null
    }
}
