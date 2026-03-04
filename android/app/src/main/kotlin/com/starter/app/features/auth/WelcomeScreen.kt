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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.starter.app.design.components.AppButton
import com.starter.app.design.tokens.AppColors
import com.starter.app.design.tokens.AppSpacing

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "StarterApp",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "Your app, ready to build.",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        AppButton(label = "Get Started", onClick = onGetStarted)
    }
}
