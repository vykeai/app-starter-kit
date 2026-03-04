package com.appstarterkit.app.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.appstarterkit.app.design.components.AppButton
import com.appstarterkit.app.design.tokens.AppColors
import com.appstarterkit.app.design.tokens.AppSpacing

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    onSignInWithGoogle: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(AppSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "AppStarterKit",
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
        Spacer(modifier = Modifier.height(AppSpacing.md))

        // "or" divider
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                "  or  ",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(AppSpacing.md))

        // Sign in with Google button
        OutlinedButton(
            onClick = onSignInWithGoogle,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        ) {
            // TODO: Replace Icon with real Google G logo asset
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color(0xFF4285F4),
            )
            Spacer(Modifier.width(8.dp))
            Text("Sign in with Google", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
