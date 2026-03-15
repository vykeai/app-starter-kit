package com.onlystack.starterapp.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.onlystack.starterapp.design.tokens.AppColors
import com.onlystack.starterapp.design.tokens.AppRadius
import com.onlystack.starterapp.design.tokens.AppSpacing

/**
 * Applies an animated shimmer gradient to any composable while [isLoading] is true.
 *
 * Usage:
 * ```
 * Box(
 *     modifier = Modifier
 *         .fillMaxWidth()
 *         .height(20.dp)
 *         .shimmer(isLoading = isLoading)
 * )
 * ```
 */
fun Modifier.shimmer(isLoading: Boolean): Modifier = composed {
    if (!isLoading) return@composed this

    val shimmerColors = listOf(
        AppColors.Surface,
        AppColors.SurfaceElevated,
        AppColors.Surface,
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + 600f, 0f),
    )

    background(brush)
}

// ---------------------------------------------------------------------------
// ShimmerRow
// ---------------------------------------------------------------------------

/**
 * A skeleton placeholder that mimics a standard list row layout:
 *   [Circle avatar]  [Title rect]
 *                    [Subtitle rect]
 *
 * Wrap a column of [ShimmerRow]s inside a parent with
 * `Modifier.shimmer(isLoading = true)` applied per-row or on a containing Box.
 *
 * @param avatarSize    Diameter of the leading circular placeholder.
 * @param titleWidth    Width of the primary text placeholder.
 * @param subtitleWidth Width of the secondary text placeholder.
 */
@Composable
fun ShimmerRow(
    avatarSize: Dp = 40.dp,
    titleWidth: Dp = 160.dp,
    subtitleWidth: Dp = 100.dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Leading circle — avatar / icon skeleton
        Box(
            modifier = Modifier
                .size(avatarSize)
                .clip(CircleShape)
                .shimmer(isLoading = true),
        )

        Spacer(modifier = Modifier.width(AppSpacing.md))

        // Text placeholders column
        androidx.compose.foundation.layout.Column {
            // Title skeleton
            Box(
                modifier = Modifier
                    .width(titleWidth)
                    .height(14.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .shimmer(isLoading = true),
            )

            Spacer(modifier = Modifier.height(AppSpacing.xs))

            // Subtitle skeleton
            Box(
                modifier = Modifier
                    .width(subtitleWidth)
                    .height(10.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .shimmer(isLoading = true),
            )
        }
    }
}
