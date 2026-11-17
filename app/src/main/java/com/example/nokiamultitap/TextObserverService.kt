package com.example.nokiamultitap

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * Panel auxiliar de accesibilidad. Solo procesa el texto en memoria.
 * Android y cada aplicación pueden limitar qué nodos exponen.
 */
class TextObserverService : AccessibilityService() {
    private var windowManager: WindowManager? = null
    private var panel: View? = null
    private var codeView: TextView? = null
    private var lastText: String = ""

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.packageName?.toString() == packageName) return
        if (event.eventType !in setOf(
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED,
                AccessibilityEvent.TYPE_VIEW_FOCUSED,
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            )) return

        val source = event.source
        if (source?.isPassword == true) {
            hidePanel()
            return
        }

        val text = findFocusedEditable(source) ?: findFocusedEditable(rootInActiveWindow)
        if (text.isNullOrBlank()) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) hidePanel()
            return
        }

        lastText = text
        val code = MultiTapCodec.encode(text)
        if (code.isBlank()) {
            hidePanel()
            return
        }
        showOrUpdatePanel(code)
    }

    private fun findFocusedEditable(start: AccessibilityNodeInfo?): String? {
        if (start == null) return null
        if (start.isPassword) return null
        if (start.isEditable && start.isFocused) return start.text?.toString()
        for (i in 0 until start.childCount) {
            val found = findFocusedEditable(start.getChild(i))
            if (found != null) return found
        }
        return null
    }

    private fun showOrUpdatePanel(code: String) {
        if (panel == null) createPanel()
        codeView?.text = code
        panel?.visibility = View.VISIBLE
    }

    private fun createPanel() {
        val context = this
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 12, 18, 12)
            setBackgroundColor(0xEE20332B.toInt())
        }
        val title = TextView(context).apply {
            text = "NOKIA MULTI-TAP"
            textSize = 11f
            setTextColor(0xFFAED9BE.toInt())
        }
        val output = TextView(context).apply {
            textSize = 16f
            setTextColor(0xFFFFFFFF.toInt())
            maxLines = 2
        }
        codeView = output
        val copy = Button(context).apply {
            text = "COPIAR CÓDIGO"
            setOnClickListener {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Multi-Tap", codeView?.text))
            }
        }
        container.addView(title)
        container.addView(output)
        container.addView(copy)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = (resources.displayMetrics.density * 290).toInt()
        }
        try {
            windowManager?.addView(container, params)
            panel = container
        } catch (_: Exception) {
            panel = null
        }
    }

    private fun hidePanel() {
        panel?.visibility = View.GONE
    }

    override fun onInterrupt() {
        hidePanel()
    }

    override fun onDestroy() {
        try {
            panel?.let { windowManager?.removeView(it) }
        } catch (_: Exception) { }
        panel = null
        super.onDestroy()
    }
}
