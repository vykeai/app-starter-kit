package com.starter.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.starter.app.design.components.AppButton
import com.starter.app.design.tokens.AppColors
import com.starter.app.design.tokens.AppRadius
import com.starter.app.design.tokens.AppSpacing

@Composable
fun CodeEntryScreen(
    viewModel: AuthViewModel,
    email: String,
    onAuthenticated: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    var code by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
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
            text = "Check your email",
            style = MaterialTheme.typography.headlineLarge,
            color = AppColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Text(
            text = "Enter the 8-digit code sent to\n$email",
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))

        BasicTextField(
            value = code,
            onValueChange = { newValue ->
                val filtered = newValue.text.filter { it.isDigit() }.take(8)
                code = TextFieldValue(filtered, TextRange(filtered.length))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    repeat(8) { index ->
                        val char = code.text.getOrNull(index)?.toString() ?: ""
                        val isFocused = index == code.text.length
                        Box(
                            modifier = Modifier
                                .size(36.dp, 48.dp)
                                .background(AppColors.Surface, RoundedCornerShape(AppRadius.sm))
                                .border(
                                    1.5.dp,
                                    if (isFocused) AppColors.Primary else Color.Transparent,
                                    RoundedCornerShape(AppRadius.sm),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.TextPrimary,
                            )
                        }
                    }
                }
            },
        )

        if (uiState.errorMessage != null) {
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Text(
                text = uiState.errorMessage!!,
                color = AppColors.Error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(AppSpacing.xl))
        AppButton(
            label = "Verify Code",
            onClick = { viewModel.verifyCode(email, code.text) },
            isLoading = uiState.isLoading,
            enabled = code.text.length == 8,
        )
    }
}
