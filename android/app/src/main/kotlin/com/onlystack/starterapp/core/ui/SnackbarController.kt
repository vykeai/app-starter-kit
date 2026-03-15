package com.onlystack.starterapp.core.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class SnackbarStyle { SUCCESS, ERROR, INFO, WARNING }

data class SnackbarEvent(
    val message: String,
    val style: SnackbarStyle = SnackbarStyle.INFO,
    val actionLabel: String? = null,
)

/**
 * Application-wide snackbar bus.
 *
 * Any layer of the app can call [show] without holding a reference to a
 * Scaffold or SnackbarHostState. [StarterAppRoot] collects [events] and
 * delegates to the Material 3 SnackbarHost.
 *
 * Usage:
 *   SnackbarController.show("Saved!", SnackbarStyle.SUCCESS)
 */
object SnackbarController {
    private val _events = MutableSharedFlow<SnackbarEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<SnackbarEvent> = _events.asSharedFlow()

    fun show(
        message: String,
        style: SnackbarStyle = SnackbarStyle.INFO,
        actionLabel: String? = null,
    ) {
        _events.tryEmit(SnackbarEvent(message, style, actionLabel))
    }
}
