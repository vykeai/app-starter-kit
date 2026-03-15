package com.appstarterkit.app.features.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import android.net.Uri
import com.appstarterkit.app.core.deeplink.DeepLinkViewModel
import com.appstarterkit.app.features.home.HomeScreen

@Composable
fun AuthNavHost(
    viewModel: AuthViewModel = hiltViewModel(),
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val pendingAuthPayload by deepLinkViewModel.pendingAuthPayload.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            navController.navigate("home") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(pendingAuthPayload) {
        val payload = pendingAuthPayload ?: return@LaunchedEffect

        when {
            !payload.linkToken.isNullOrBlank() -> {
                payload.email?.let(viewModel::rememberEmail)
                viewModel.verifyLinkToken(payload.linkToken)
            }
            !payload.email.isNullOrBlank() -> {
                val email = payload.email
                viewModel.rememberEmail(email)
                payload.code?.let(viewModel::primePendingCode)
                navController.navigate("enter-code/${Uri.encode(email)}") {
                    launchSingleTop = true
                }
            }
            !payload.code.isNullOrBlank() -> {
                viewModel.primePendingCode(payload.code)
                navController.navigate("enter-email") {
                    launchSingleTop = true
                }
            }
        }

        deepLinkViewModel.consumePendingAuth()
    }

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onGetStarted = { navController.navigate("enter-email") })
        }
        composable("enter-email") {
            EmailInputScreen(
                viewModel = viewModel,
                onCodeSent = { email ->
                    viewModel.resetCodeSent()
                    navController.navigate("enter-code/${Uri.encode(email)}")
                },
            )
        }
        composable("enter-code/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""

            CodeEntryScreen(
                viewModel = viewModel,
                email = email,
                onAuthenticated = { },
            )
        }
        composable("home") {
            HomeScreen(
                onLogout = {
                    // Delegate to AuthViewModel which calls AuthRepository.logout()
                    // and then resets navigation to the auth root.
                    viewModel.logout {
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
            )
        }
    }
}
