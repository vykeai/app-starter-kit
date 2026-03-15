package com.onlystack.starterapp.nfr

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.onlystack.starterapp.design.components.AppButton
import com.onlystack.starterapp.design.tokens.AppColors
import com.onlystack.starterapp.design.tokens.AppSpacing

object ForceUpdateComponents {

    @Composable
    fun HardUpdateScreen() {
        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Background)
                .padding(AppSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Update Required",
                style = MaterialTheme.typography.headlineLarge,
                color = AppColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = "A new version is required to continue. Please update from the Play Store.",
                style = MaterialTheme.typography.bodyLarge,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxl))
            AppButton(label = "Update Now", onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.onlystack.starterapp"))
                context.startActivity(intent)
            })
        }
    }

    @Composable
    fun SoftUpdateBanner(modifier: Modifier = Modifier, onDismiss: () -> Unit) {
        val context = LocalContext.current
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(AppColors.SurfaceElevated)
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Update available",
                style = MaterialTheme.typography.bodyMedium,
                color = AppColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.onlystack.starterapp"))
                context.startActivity(intent)
            }) {
                Text("Update", color = AppColors.Primary)
            }
            TextButton(onClick = onDismiss) {
                Text("Later", color = AppColors.TextSecondary)
            }
        }
    }
}
