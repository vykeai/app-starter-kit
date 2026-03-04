package com.starter.app.features.auth

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.starter.app.features.home.HomeScreen

@Composable
fun AuthNavHost(viewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "welcome") {
        composable("welcome") {
            WelcomeScreen(onGetStarted = { navController.navigate("enter-email") })
        }
        composable("enter-email") {
            EmailInputScreen(
                viewModel = viewModel,
                onCodeSent = { email -> navController.navigate("enter-code/$email") },
            )
        }
        composable("enter-code/{email}") { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            CodeEntryScreen(
                viewModel = viewModel,
                email = email,
                onAuthenticated = { navController.navigate("home") { popUpTo(0) } },
            )
        }
        composable("home") {
            HomeScreen()
        }
    }
}
