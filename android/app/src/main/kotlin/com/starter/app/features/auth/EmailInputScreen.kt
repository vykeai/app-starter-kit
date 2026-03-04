package com.starter.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.starter.app.design.components.AppButton
import com.starter.app.design.components.AppTextField
import com.starter.app.design.tokens.AppColors
import com.starter.app.design.tokens.AppSpacing

@Composable
fun EmailInputScreen(
    viewModel: AuthViewModel,
    onCodeSent: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isCodeSent) {
        if (uiState.isCodeSent) onCodeSent(email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Sign In",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "We'll send an 8-digit code to your email.",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            placeholder = "you@example.com",
            isError = uiState.errorMessage != null,
            errorMessage = uiState.errorMessage,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        AppButton(
            label = "Send Code",
            onClick = { viewModel.requestCode(email) },
            isLoading = uiState.isLoading,
        )
    }
}
