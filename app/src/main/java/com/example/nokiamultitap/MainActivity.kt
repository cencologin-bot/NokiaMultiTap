package com.example.nokiamultitap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(0xFFF1F4F2.toInt())
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)

        fun heading(text: String, size: Float = 20f) {
            content.addView(TextView(this).apply {
                this.text = text
                textSize = size
                setTextColor(0xFF18352A.toInt())
                setPadding(0, dp(12), 0, dp(8))
            })
        }
        fun button(text: String, action: () -> Unit) {
            content.addView(Button(this).apply {
                this.text = text
                setOnClickListener { action() }
            })
        }

        heading("NOKIA MULTI-TAP", 26f)
        content.addView(TextView(this).apply {
            text = "Gboard original + conversión automática"
            textSize = 14f
            setTextColor(0xFF52645A.toInt())
        })

        heading("1. Probar escritura")
        val input = EditText(this).apply {
            hint = "Escribí aquí usando Gboard..."
            minLines = 2
            gravity = Gravity.TOP
        }
        content.addView(input, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        heading("Código Multi-Tap")
        val encoded = TextView(this).apply {
            text = "El código aparecerá aquí"
            textSize = 17f
            setTextColor(0xFF176B45.toInt())
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        content.addView(encoded)
        input.addTextChangedListener(SimpleTextWatcher { encoded.text = MultiTapCodec.encode(it) })
        button("Copiar código") {
            val clip = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clip.setPrimaryClip(android.content.ClipData.newPlainText("Multi-Tap", encoded.text))
        }

        heading("2. Leer código")
        val codeInput = EditText(this).apply {
            hint = "Ej.: 44 666 555 2 / 6 666 555 3 666"
            minLines = 2
            gravity = Gravity.TOP
        }
        content.addView(codeInput, ViewGroup.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT))
        val decoded = TextView(this).apply {
            text = "El mensaje descifrado aparecerá aquí"
            textSize = 17f
            setTextColor(0xFF18352A.toInt())
            setPadding(dp(12), dp(14), dp(12), dp(14))
            setBackgroundColor(0xFFFFFFFF.toInt())
        }
        content.addView(decoded)
        val invalid = TextView(this).apply { textSize = 12f; setTextColor(0xFF9B3B32.toInt()) }
        content.addView(invalid)
        codeInput.addTextChangedListener(SimpleTextWatcher {
            val result = MultiTapCodec.decode(it)
            decoded.text = result.text
            invalid.text = if (result.invalidTokens.isEmpty()) "" else
                "Tokens no reconocidos: ${result.invalidTokens.joinToString(", ")}"
        })
        button("Copiar texto") {
            val clip = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clip.setPrimaryClip(android.content.ClipData.newPlainText("Texto", decoded.text))
        }

        heading("3. Activar panel sobre Gboard")
        content.addView(TextView(this).apply {
            text = "Android requiere que habilites manualmente el servicio de accesibilidad. El panel solo podrá leer campos compatibles y nunca debe usarse en contraseñas."
            textSize = 14f
            setTextColor(0xFF52645A.toInt())
        })
        button("Abrir ajustes de accesibilidad") {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
        button("Abrir ajustes de teclado") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(page)
    }
}
