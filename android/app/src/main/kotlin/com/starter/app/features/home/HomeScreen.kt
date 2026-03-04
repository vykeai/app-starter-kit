package com.starter.app.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.starter.app.design.tokens.AppColors

@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Welcome!",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your app is ready.",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
        )
    }
}
