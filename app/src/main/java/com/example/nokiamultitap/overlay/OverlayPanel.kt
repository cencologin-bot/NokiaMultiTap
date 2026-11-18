package com.example.nokiamultitap.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nokiamultitap.MultiTapCodec
import com.example.nokiamultitap.ui.NtIcons
import com.example.nokiamultitap.ui.PulsingDot

/** Panel flotante: se usa tanto en el servicio como en la vista previa de la app. */
@Composable
fun OverlayPanel(
    visible: Boolean,
    text: String,
    code: String,
    onSend: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = spring<IntOffset>(dampingRatio = 0.72f, stiffness = Spring.StiffnessMediumLow)
        ) { -it } +
            fadeIn(tween(160)) +
            scaleIn(spring<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow), initialScale = 0.94f),
        exit = slideOutVertically(tween(190)) { -it / 2 } + fadeOut(tween(140))
    ) {
        PanelCard(text, code, onSend, onCopy, onDismiss)
    }
}

@Composable
private fun PanelCard(
    text: String,
    code: String,
    onSend: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = cs.surfaceContainerHigh.copy(alpha = 0.95f),
        contentColor = cs.onSurface,
        tonalElevation = 6.dp,
        shadowElevation = 14.dp,
        border = BorderStroke(0.5.dp, cs.outlineVariant)
    ) {
        Column(Modifier.padding(start = 18.dp, top = 12.dp, end = 10.dp, bottom = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PulsingDot(color = cs.primary, dotSize = 5.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "MULTI-TAP  ·  EN VIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.primary,
                    letterSpacing = 1.2.sp
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(NtIcons.Close, contentDescription = "Cerrar panel", modifier = Modifier.size(18.dp))
                }
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 4.dp, end = 8.dp)
                    .animateContentSize()
            )
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = cs.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Código listo para enviar",
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurfaceVariant
                    )
                    Text(
                        MultiTapCodec.abbreviate(code),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        "${MultiTapCodec.tapCount(code)} pulsaciones",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCopy) {
                    Icon(NtIcons.Copy, contentDescription = "Copiar código", modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(2.dp))
                SendButton(onSend)
            }
        }
    }
}

@Composable
private fun SendButton(onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "sendScale"
    )
    FilledIconButton(
        onClick = onClick,
        interactionSource = interaction,
        shape = CircleShape,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Icon(NtIcons.Send, contentDescription = "Enviar código Multi-Tap", modifier = Modifier.size(22.dp))
    }
}
