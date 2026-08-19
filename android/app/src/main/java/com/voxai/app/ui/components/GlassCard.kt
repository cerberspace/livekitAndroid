package com.voxai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.voxai.app.ui.theme.NeonBorder
import com.voxai.app.ui.theme.NeonCyan
import com.voxai.app.ui.theme.NeonPurple
import com.voxai.app.ui.theme.NeonSurface

/**
 * A glassmorphism card with neon border accents.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    borderColor: Color = NeonBorder,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonSurface.copy(alpha = 0.7f),
                        NeonSurface.copy(alpha = 0.5f),
                    )
                )
            )
            .border(1.dp, borderColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(contentPadding)
    ) {
        content()
    }
}

/**
 * A neon glow box for accent effects.
 */
@Composable
fun NeonGlowBox(
    modifier: Modifier = Modifier,
    color: Color = NeonCyan,
    size: Int = 200,
    alpha: Float = 0.15f,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha), Color.Transparent)
                )
            )
    )
}
