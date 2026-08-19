package com.voxai.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.voxai.app.ui.theme.NeonCyan
import com.voxai.app.ui.theme.NeonPurple
import kotlin.random.Random

/**
 * Animated audio waveform visualization.
 *
 * When [isActive] is true, the bars animate with random heights.
 * When inactive, bars settle to a minimal height.
 */
@Composable
fun AudioWaveform(
    modifier: Modifier = Modifier,
    barCount: Int = 28,
    isActive: Boolean = true,
    barColor: Brush = Brush.verticalGradient(listOf(NeonCyan, NeonPurple)),
    onColorChange: ((Boolean) -> Color)? = null,
) {
    val transition = rememberInfiniteTransition(label = "waveform")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0 until barCount) {
            val progress by transition.animateFloat(
                initialValue = if (isActive) 0.2f else 0.1f,
                targetValue = if (isActive) 1f else 0.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = if (isActive) 300 + Random(i).nextInt(400) else 1500,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "bar_$i",
            )

            // Random phase offset per bar
            val phaseOffset = (i * 0.15f) % 1f
            val animatedProgress = if (isActive) {
                ((progress + phaseOffset) % 1f).coerceIn(0.15f, 1f)
            } else {
                0.1f
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(4.dp)
                    .height((animatedProgress * 48).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

/**
 * A simpler static waveform that uses Canvas for better performance.
 */
@Composable
fun StaticWaveform(
    modifier: Modifier = Modifier,
    heights: List<Float> = listOf(0.3f, 0.6f, 1f, 0.7f, 0.4f, 0.8f, 0.5f, 0.3f, 0.6f, 1f, 0.7f, 0.4f, 0.8f, 0.5f),
    color: Brush = Brush.verticalGradient(listOf(NeonCyan, NeonPurple)),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        heights.forEachIndexed { _, h ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 1.5.dp)
                    .size(width = 3.dp, height = (h * 32).dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}
