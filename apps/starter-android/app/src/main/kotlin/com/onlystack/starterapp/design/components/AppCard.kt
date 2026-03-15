package com.onlystack.starterapp.design.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.onlystack.starterapp.design.tokens.AppColors
import com.onlystack.starterapp.design.tokens.AppRadius
import com.onlystack.starterapp.design.tokens.AppSpacing

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(AppRadius.md),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
    ) {
        Box(modifier = Modifier.padding(AppSpacing.md)) {
            content()
        }
    }
}
