package com.example.nokiamultitap.ui

import android.os.SystemClock
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

private val KEY_LETTERS = mapOf(
    '1' to ".,?!:;'\"-",
    '2' to "abc", '3' to "def", '4' to "ghi", '5' to "jkl",
    '6' to "mno", '7' to "pqrs", '8' to "tuv", '9' to "wxyz"
)

private const val CYCLE_WINDOW_MS = 1000L

/** Reproduce el ciclo de letras de un teclado Nokia: pulsar la misma tecla cambia la letra. */
class MultiTapTyper {
    private var lastKey: Char? = null
    private var cycle = 0
    private var lastTime = 0L

    fun press(current: String, key: Char, now: Long = SystemClock.uptimeMillis()): String {
        val letters = KEY_LETTERS[key] ?: return current
        val repeat = key == lastKey && now - lastTime < CYCLE_WINDOW_MS && current.isNotEmpty()
        val next = if (repeat) {
            cycle = (cycle + 1) % letters.length
            current.dropLast(1) + letters[cycle]
        } else {
            cycle = 0
            current + letters[0]
        }
        lastKey = key
        lastTime = now
        return next
    }

    fun reset() {
        lastKey = null
        cycle = 0
    }
}

@Composable
fun NokiaKeypad(
    encodeMode: Boolean,
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { d -> DigitKey(d, Modifier.weight(1f)) { onDigit(d) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KeyButton(Modifier.weight(1f), onClick = onBackspace) {
                Icon(NtIcons.Backspace, contentDescription = "Borrar", modifier = Modifier.size(22.dp))
            }
            DigitKey('0', Modifier.weight(1f)) { onDigit('0') }
            KeyButton(Modifier.weight(1f), onClick = onAction) {
                if (encodeMode) {
                    Icon(NtIcons.Close, contentDescription = "Borrar todo", modifier = Modifier.size(22.dp))
                } else {
                    Text("Siguiente", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun DigitKey(digit: Char, modifier: Modifier, onClick: () -> Unit) {
    val hint = when {
        digit == '0' -> "espacio"
        digit == '1' -> ".,?!"
        else -> KEY_LETTERS[digit].orEmpty().uppercase()
    }
    KeyButton(modifier, onClick) {
        Text(digit.toString(), style = MaterialTheme.typography.titleLarge)
        Text(
            hint,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun KeyButton(
    modifier: Modifier,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val cs = MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "keyScale"
    )
    val container by animateColorAsState(
        targetValue = if (pressed) cs.primaryContainer else cs.surfaceContainerHighest,
        animationSpec = tween(90),
        label = "keyColor"
    )
    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .height(58.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = MaterialTheme.shapes.large,
        color = container,
        interactionSource = interaction
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
    }
}
