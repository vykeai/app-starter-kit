package com.starter.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.starter.app.core.network.NetworkMonitor
import com.starter.app.features.auth.AuthNavHost
import com.starter.app.nfr.ForceUpdateComponents
import com.starter.app.nfr.ForceUpdateViewModel

@Composable
fun StarterAppRoot(
    networkMonitor: NetworkMonitor = LocalContext.current.let {
        remember { NetworkMonitor(it) }
    },
    forceUpdateViewModel: ForceUpdateViewModel = hiltViewModel(),
) {
    val isConnected by networkMonitor.isConnected.collectAsState(initial = true)
    val forceUpdateState by forceUpdateViewModel.state.collectAsState()

    AppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    onDismiss = { forceUpdateViewModel.dismissSoftUpdate() }
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        forceUpdateViewModel.checkForUpdate()
    }
}
