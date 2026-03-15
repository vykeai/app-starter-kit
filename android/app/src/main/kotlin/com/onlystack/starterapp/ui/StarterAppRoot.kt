package com.onlystack.starterapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.onlystack.starterapp.core.network.NetworkMonitor
import com.onlystack.starterapp.core.ui.SnackbarController
import com.onlystack.starterapp.core.ui.SnackbarStyle
import com.onlystack.starterapp.design.tokens.AppColors
import com.onlystack.starterapp.features.auth.AuthNavHost
import com.onlystack.starterapp.nfr.ForceUpdateComponents
import com.onlystack.starterapp.nfr.ForceUpdateViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun StarterAppRoot(
    networkMonitor: NetworkMonitor = LocalContext.current.let {
        remember { NetworkMonitor(it) }
    },
    forceUpdateViewModel: ForceUpdateViewModel = hiltViewModel(),
) {
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    val forceUpdateState by forceUpdateViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect application-wide snackbar events emitted via SnackbarController.show(…).
    LaunchedEffect(Unit) {
        SnackbarController.events.collectLatest { event ->
            snackbarHostState.showSnackbar(
                message = event.message,
                actionLabel = event.actionLabel,
                duration = SnackbarDuration.Short,
            )
        }
    }

    AppTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { snackbarData ->
                    // Look up the most recent style from the SnackbarController so the
                    // background color matches SUCCESS / ERROR / WARNING / INFO.
                    // We tag the style into the visuals by matching the message; for a
                    // production app consider using a custom SnackbarVisuals subclass.
                    val containerColor = when {
                        snackbarData.visuals.message.contains("error", ignoreCase = true) ||
                        snackbarData.visuals.actionLabel == SnackbarStyle.ERROR.name ->
                            AppColors.Error
                        snackbarData.visuals.message.contains("success", ignoreCase = true) ||
                        snackbarData.visuals.actionLabel == SnackbarStyle.SUCCESS.name ->
                            AppColors.Success
                        snackbarData.visuals.actionLabel == SnackbarStyle.WARNING.name ->
                            AppColors.Warning
                        else -> AppColors.SurfaceElevated
                    }

                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = containerColor,
                        contentColor = AppColors.TextPrimary,
                        actionColor = AppColors.Primary,
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                when {
                    forceUpdateState.isHardUpdateRequired -> ForceUpdateComponents.HardUpdateScreen()
                    else -> AuthNavHost()
                }

                if (!isConnected) {
                    OfflineBanner(modifier = Modifier.align(Alignment.TopCenter))
                }

                if (forceUpdateState.isSoftUpdateAvailable && !forceUpdateState.isHardUpdateRequired) {
                    ForceUpdateComponents.SoftUpdateBanner(
                        modifier = Modifier.align(Alignment.TopCenter),
                        onDismiss = { forceUpdateViewModel.dismissSoftUpdate() },
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        forceUpdateViewModel.checkForUpdate()
    }
}
