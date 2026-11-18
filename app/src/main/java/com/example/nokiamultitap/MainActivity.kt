package com.example.nokiamultitap

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.nokiamultitap.ui.HomeScreen
import com.example.nokiamultitap.ui.NokiaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NokiaTheme {
                HomeScreen(
                    onOpenAccessibilitySettings = { open(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    onOpenAppInfo = {
                        open(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            )
                        )
                    },
                    onOpenKeyboardSettings = { open(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
                )
            }
        }
    }

    private fun open(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
        }
    }
}
