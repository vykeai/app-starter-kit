package com.onlystack.starterapp.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.onlystack.starterapp.design.tokens.AppColors
import com.onlystack.starterapp.design.tokens.AppSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home", color = AppColors.TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface,
                ),
                actions = {
                    IconButton(onClick = { showSignOutDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Account",
                            tint = AppColors.TextPrimary,
                        )
                    }
                },
            )
        },
        containerColor = AppColors.Background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = "Your app is ready.",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
            )
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign out?",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.TextPrimary,
                )
            },
            text = {
                Text(
                    text = "You will be returned to the login screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onLogout()
                }) {
                    Text("Sign out", color = AppColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            },
            containerColor = AppColors.Surface,
        )
    }
}
