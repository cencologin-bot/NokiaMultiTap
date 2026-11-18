package com.example.nokiamultitap

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.nokiamultitap.overlay.OverlayLifecycleOwner
import com.example.nokiamultitap.overlay.OverlayPanel
import com.example.nokiamultitap.ui.NokiaTheme

private data class PanelState(
    val visible: Boolean = false,
    val text: String = "",
    val code: String = ""
)

/**
 * Muestra un panel flotante (Compose) mientras se escribe en un campo editable.
 * El texto se procesa localmente. Nunca se lee contenido de campos de contraseña.
 */
class TextObserverService : AccessibilityService() {
    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())
    private val autoHide = Runnable { hidePanel() }

    private var panel by mutableStateOf(PanelState())
    private var overlayView: ComposeView? = null
    private var overlayOwner: OverlayLifecycleOwner? = null
    private var fullCode: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WindowManager::class.java)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) return
        if (event.packageName?.toString() == packageName) return

        val source = event.source
        if (source == null || source.isPassword) {
            hidePanel()
            return
        }
        val field = source.takeIf { it.isEditable } ?: findFocusedEditableNode(rootInActiveWindow)
        if (field == null || field.isPassword) {
            hidePanel()
            return
        }

        val text = if (field.isShowingHintText) "" else field.text?.toString().orEmpty()
        if (text.isBlank()) {
            hidePanel()
            return
        }
        val code = MultiTapCodec.encode(text)
        if (code.isBlank()) {
            hidePanel()
            return
        }

        fullCode = code
        showPanel(text, code)
    }

    private fun showPanel(text: String, code: String) {
        ensureOverlay()
        panel = PanelState(visible = true, text = text, code = code)
        handler.removeCallbacks(autoHide)
        handler.postDelayed(autoHide, AUTO_HIDE_MS)
    }

    private fun hidePanel() {
        handler.removeCallbacks(autoHide)
        if (panel.visible) panel = panel.copy(visible = false)
    }

    private fun ensureOverlay() {
        if (overlayView != null) return
        val owner = OverlayLifecycleOwner()
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                NokiaTheme {
                    OverlayPanel(
                        visible = panel.visible,
                        text = panel.text,
                        code = panel.code,
                        onSend = ::sendCodeToCurrentChat,
                        onCopy = ::copyCode,
                        onDismiss = ::hidePanel
                    )
                }
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = statusBarHeight()
        }
        try {
            windowManager.addView(view, params)
            overlayView = view
            overlayOwner = owner
        } catch (_: Exception) {
            owner.destroy()
        }
    }

    private fun removeOverlay() {
        handler.removeCallbacks(autoHide)
        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (_: Exception) {
            }
        }
        overlayOwner?.destroy()
        overlayView = null
        overlayOwner = null
    }

    private fun statusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    private fun findFocusedEditableNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null || node.isPassword) return null
        if (node.isEditable && node.isFocused) return node
        for (i in 0 until node.childCount) {
            val found = findFocusedEditableNode(node.getChild(i))
            if (found != null) return found
        }
        return null
    }

    private fun copyCode() {
        if (fullCode.isBlank()) return
        val cm = getSystemService(ClipboardManager::class.java)
        cm.setPrimaryClip(ClipData.newPlainText("Multi-Tap", fullCode))
        Toast.makeText(this, "Código copiado", Toast.LENGTH_SHORT).show()
    }

    /** Inserta el código en el campo actual y pulsa un botón de envío accesible, si existe. */
    private fun sendCodeToCurrentChat() {
        val field = findFocusedEditableNode(rootInActiveWindow)
        if (field == null || field.isPassword || fullCode.isBlank()) {
            Toast.makeText(this, "No se encontró un campo de mensaje compatible", Toast.LENGTH_SHORT).show()
            return
        }
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, fullCode)
        }
        if (!field.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)) {
            Toast.makeText(this, "Esta aplicación no permite insertar el código automáticamente", Toast.LENGTH_LONG).show()
            return
        }
        val sendButton = findSendButton(rootInActiveWindow)
        if (sendButton?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) {
            hidePanel()
        } else {
            Toast.makeText(
                this,
                "Código colocado. Esta aplicación no expone un botón de envío compatible; revisá y enviá manualmente",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun findSendButton(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val label = listOfNotNull(node.text?.toString(), node.contentDescription?.toString())
            .joinToString(" ").lowercase()
        val looksLikeSend = SEND_LABELS.any { label.contains(it) }
        if (looksLikeSend && (node.isClickable || node.isFocusable)) return node
        for (i in 0 until node.childCount) {
            val found = findSendButton(node.getChild(i))
            if (found != null) return found
        }
        return null
    }

    override fun onInterrupt() = hidePanel()

    override fun onUnbind(intent: Intent?): Boolean {
        removeOverlay()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        removeOverlay()
        super.onDestroy()
    }

    private companion object {
        const val AUTO_HIDE_MS = 8000L
        val SEND_LABELS = listOf("send", "enviar", "mandar", "submit")
    }
}
