package com.universal.calulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // App start par disk se saved theme load karega
            val themeState = remember {
                mutableStateOf(ThemePreferenceManager.getSavedTheme(this@MainActivity))
            }
            val hapticState = remember { mutableStateOf(true) }

            CompositionLocalProvider(
                LocalAppTheme provides themeState,
                LocalHapticEnabled provides hapticState
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = themeState.value.bg
                ) {
                    MainAppNavigation()
                }
            }
        }
    }
}