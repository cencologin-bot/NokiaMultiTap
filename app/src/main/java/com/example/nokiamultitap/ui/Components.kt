package com.example.nokiamultitap.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PulsingDot(
    color: Color,
    modifier: Modifier = Modifier,
    pulsing: Boolean = true,
    dotSize: Dp = 8.dp
) {
    Box(modifier.size(dotSize * 2), contentAlignment = Alignment.Center) {
        if (pulsing) PulseRing(color, dotSize)
        Box(Modifier.size(dotSize).background(color, CircleShape))
    }
}

@Composable
private fun PulseRing(color: Color, dotSize: Dp) {
    val transition = rememberInfiniteTransition(label = "dot")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label = "pulse"
    )
    Box(
        Modifier
            .size(dotSize)
            .graphicsLayer {
                val s = 1f + pulse
                scaleX = s
                scaleY = s
                alpha = 0.5f * (1f - pulse)
            }
            .background(color, CircleShape)
    )
}
