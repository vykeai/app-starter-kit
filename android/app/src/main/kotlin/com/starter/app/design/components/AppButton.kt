package com.starter.app.design.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.starter.app.design.tokens.AppColors
import com.starter.app.design.tokens.AppRadius

enum class AppButtonStyle { Primary, Secondary, Destructive }

@Composable
fun AppButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    style: AppButtonStyle = AppButtonStyle.Primary,
) {
    val backgroundColor = when (style) {
        AppButtonStyle.Primary -> AppColors.Primary
        AppButtonStyle.Secondary -> Color.Transparent
        AppButtonStyle.Destructive -> AppColors.Error
    }
    val contentColor = when (style) {
        AppButtonStyle.Secondary -> AppColors.Primary
        else -> Color.White
    }
    val shape = RoundedCornerShape(AppRadius.md)

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(
                if (style == AppButtonStyle.Secondary)
                    Modifier.border(1.5.dp, AppColors.Primary, shape)
                else Modifier
            ),
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
        ),
        shape = shape,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
