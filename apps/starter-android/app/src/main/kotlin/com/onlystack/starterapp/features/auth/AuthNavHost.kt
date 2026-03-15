package com.onlystack.starterapp.features.auth

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
import com.onlystack.starterapp.core.deeplink.DeepLinkViewModel
import com.onlystack.starterapp.core.deeplink.PendingRoute
import com.onlystack.starterapp.features.home.HomeScreen
import com.onlystack.starterapp.features.more.ProfileScreen

@Composable
fun AuthNavHost(
    viewModel: AuthViewModel = hiltViewModel(),
    deepLinkViewModel: DeepLinkViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val pendingRoute by deepLinkViewModel.pendingRoute.collectAsState()

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

    LaunchedEffect(pendingRoute) {
        when (val route = pendingRoute ?: return@LaunchedEffect) {
            is PendingRoute.Auth -> {
                val payload = route.payload
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
            }
            PendingRoute.Home -> {
                navController.navigate("home") {
                    launchSingleTop = true
                }
            }
        }

        deepLinkViewModel.consumePendingRoute()
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
                onOpenProfile = {
                    navController.navigate("profile")
                },
            )
        }
        composable("profile") {
            ProfileScreen(
                onLogout = {
                    viewModel.logout {
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
