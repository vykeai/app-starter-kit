package com.starter.app.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.starter.app.design.tokens.AppColors
import com.starter.app.design.tokens.AppSpacing

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder, color = AppColors.TextSecondary) },
            isError = isError,
            singleLine = singleLine,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Primary,
                unfocusedBorderColor = AppColors.TextSecondary,
                focusedLabelColor = AppColors.Primary,
                unfocusedLabelColor = AppColors.TextSecondary,
                cursorColor = AppColors.Primary,
                focusedTextColor = AppColors.TextPrimary,
                unfocusedTextColor = AppColors.TextPrimary,
                errorBorderColor = AppColors.Error,
            ),
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = AppColors.Error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = AppSpacing.sm, top = AppSpacing.xs),
            )
        }
    }
}
