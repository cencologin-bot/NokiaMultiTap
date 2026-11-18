package com.example.nokiamultitap.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.example.nokiamultitap.MultiTapCodec
import com.example.nokiamultitap.TextObserverService
import com.example.nokiamultitap.overlay.OverlayPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun isServiceEnabled(context: Context): Boolean {
    val enabled = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ).orEmpty()
    if (enabled.isEmpty()) return false
    val component = ComponentName(context, TextObserverService::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenAccessibilitySettings: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onOpenKeyboardSettings: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val scroll = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val cs = MaterialTheme.colorScheme

    var encodeMode by rememberSaveable { mutableStateOf(true) }
    var text by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }
    var serviceEnabled by remember { mutableStateOf(isServiceEnabled(context)) }
    val typer = remember { MultiTapTyper() }

    LifecycleResumeEffect(Unit) {
        serviceEnabled = isServiceEnabled(context)
        onPauseOrDispose { }
    }
    LaunchedEffect(copied) {
        if (copied) {
            delay(1600)
            copied = false
        }
    }

    val encoded = remember(text) { MultiTapCodec.encode(text) }
    val decoded = remember(code) { MultiTapCodec.decode(code) }
    val result = if (encodeMode) encoded else decoded.text
    val taps = if (encodeMode) MultiTapCodec.tapCount(encoded) else MultiTapCodec.tapCount(code)

    fun notify(message: String) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            snackbar.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        containerColor = cs.background,
        topBar = {
            LargeTopAppBar(
                title = { Text("Nokia Multi-Tap") },
                scrollBehavior = scroll,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = cs.background,
                    scrolledContainerColor = cs.surfaceContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .consumeWindowInsets(inner)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Reveal(0) {
                StatusCard(serviceEnabled, onOpenAccessibilitySettings, onOpenAppInfo)
            }

            Reveal(1) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = encodeMode,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                typer.reset()
                                encodeMode = true
                            },
                            shape = SegmentedButtonDefaults.itemShape(0, 2)
                        ) { Text("Texto a código") }
                        SegmentedButton(
                            selected = !encodeMode,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                typer.reset()
                                encodeMode = false
                            },
                            shape = SegmentedButtonDefaults.itemShape(1, 2)
                        ) { Text("Código a texto") }
                    }
                    val value = if (encodeMode) text else code
                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            typer.reset()
                            if (encodeMode) text = it else code = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        minLines = 2,
                        maxLines = 5,
                        label = { Text(if (encodeMode) "Tu mensaje" else "Código Multi-Tap") },
                        placeholder = {
                            Text(if (encodeMode) "Escribí con Gboard o con el teclado Nokia" else "44 666 555 2 / 6 88 66 3 666")
                        },
                        trailingIcon = {
                            AnimatedVisibility(value.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                                IconButton(onClick = {
                                    typer.reset()
                                    if (encodeMode) text = "" else code = ""
                                }) {
                                    Icon(NtIcons.Close, contentDescription = "Borrar", modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    )
                }
            }

            Reveal(2) {
                ResultCard(
                    encodeMode = encodeMode,
                    result = result,
                    taps = taps,
                    invalid = decoded.invalidTokens,
                    copied = copied,
                    onCopy = {
                        if (result.isBlank()) {
                            notify("Todavía no hay nada para copiar")
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val cm = context.getSystemService(ClipboardManager::class.java)
                            cm.setPrimaryClip(ClipData.newPlainText("Multi-Tap", result))
                            copied = true
                        }
                    },
                    onShare = {
                        if (result.isBlank()) {
                            notify("Todavía no hay nada para compartir")
                        } else {
                            val send = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, result)
                            }
                            context.startActivity(Intent.createChooser(send, null))
                        }
                    }
                )
            }

            Reveal(3) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column {
                            Text("Teclado Nokia", style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (encodeMode) "Tocá varias veces una tecla para cambiar de letra."
                                else "Tocá los números y usá Siguiente para separar letras.",
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        NokiaKeypad(
                            encodeMode = encodeMode,
                            onDigit = { d ->
                                if (encodeMode) {
                                    if (d == '0') {
                                        typer.reset()
                                        text += " "
                                    } else {
                                        text = typer.press(text, d)
                                    }
                                } else {
                                    code += d
                                }
                            },
                            onBackspace = {
                                typer.reset()
                                if (encodeMode) text = text.dropLast(1) else code = code.dropLast(1)
                            },
                            onAction = {
                                typer.reset()
                                if (encodeMode) text = "" else code += " "
                            }
                        )
                    }
                }
            }

            Reveal(4) {
                PreviewCard(
                    text = text.ifBlank { "hola mundo" },
                    onOpenKeyboardSettings = onOpenKeyboardSettings
                )
            }

            Reveal(5) {
                Text(
                    "Todo se procesa en tu dispositivo. El servicio nunca lee campos de contraseña y no guarda ni envía lo que escribís.",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun Reveal(index: Int, content: @Composable () -> Unit) {
    var shown by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!shown) delay(70L * index)
        shown = true
    }
    AnimatedVisibility(
        visible = shown,
        enter = fadeIn(tween(320)) +
            slideInVertically(
                animationSpec = spring<IntOffset>(dampingRatio = 0.82f, stiffness = Spring.StiffnessLow)
            ) { it / 6 }
    ) { content() }
}

@Composable
private fun StatusCard(enabled: Boolean, onOpenSettings: () -> Unit, onOpenAppInfo: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (enabled) cs.primaryContainer else cs.tertiaryContainer,
        animationSpec = tween(400),
        label = "statusContainer"
    )
    val onContainer = if (enabled) cs.onPrimaryContainer else cs.onTertiaryContainer
    Card(
        colors = CardDefaults.cardColors(containerColor = container, contentColor = onContainer),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(20.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PulsingDot(color = if (enabled) cs.primary else cs.tertiary, pulsing = enabled)
                AnimatedContent(
                    targetState = enabled,
                    transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                    label = "statusTitle"
                ) { on ->
                    Text(
                        if (on) "Panel flotante activo" else "Panel flotante desactivado",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            Text(
                if (enabled) "Aparecerá sobre Gboard cuando escribas en un campo compatible."
                else "Activá el servicio de accesibilidad para ver la traducción en vivo sobre cualquier chat.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!enabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = onOpenSettings) { Text("Activar servicio") }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = onOpenAppInfo) { Text("Info de la app") }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Text(
                        "Si el interruptor aparece gris: Info de la app, menú ⋮ y Permitir ajustes restringidos.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    encodeMode: Boolean,
    result: String,
    taps: Int,
    invalid: List<String>,
    copied: Boolean,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = cs.secondaryContainer, contentColor = cs.onSecondaryContainer),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            Modifier
                .padding(20.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                if (encodeMode) "Código Multi-Tap" else "Mensaje",
                style = MaterialTheme.typography.labelLarge
            )
            AnimatedContent(
                targetState = result.isEmpty(),
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                label = "resultEmpty"
            ) { empty ->
                if (empty) {
                    Text(
                        if (encodeMode) "Tu código aparecerá aquí" else "El mensaje aparecerá aquí",
                        style = MaterialTheme.typography.titleMedium,
                        color = cs.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                } else {
                    Text(
                        result,
                        style = if (encodeMode) {
                            MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Monospace)
                        } else {
                            MaterialTheme.typography.headlineSmall
                        }
                    )
                }
            }
            if (result.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatPill(if (taps == 1) "1 pulsación" else "$taps pulsaciones")
                }
            }
            AnimatedVisibility(!encodeMode && invalid.isNotEmpty()) {
                Text(
                    "Tokens no reconocidos: ${invalid.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.error
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onCopy) {
                    Icon(
                        if (copied) NtIcons.Check else NtIcons.Copy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (copied) "Copiado" else "Copiar")
                }
                OutlinedButton(onClick = onShare) {
                    Icon(NtIcons.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir")
                }
            }
        }
    }
}

@Composable
private fun StatPill(label: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PreviewCard(text: String, onOpenKeyboardSettings: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(350)
        shown = true
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = cs.surfaceContainerLow),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Así se ve sobre Gboard", style = MaterialTheme.typography.titleMedium)
            Box(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                OverlayPanel(
                    visible = shown,
                    text = text,
                    code = MultiTapCodec.encode(text),
                    onSend = {},
                    onCopy = {},
                    onDismiss = {}
                )
            }
            TextButton(onClick = onOpenKeyboardSettings) { Text("Ajustes de teclado") }
        }
    }
}
