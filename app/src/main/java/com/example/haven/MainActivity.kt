package com.example.haven

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import com.example.haven.ui.theme.HavenTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge-to-edge with peach background and dark icons
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.parseColor("#FFEEE2"),  // peach header
                darkScrim = Color.parseColor("#FFEEE2")
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = android.graphics.Color.WHITE,
                darkScrim = android.graphics.Color.WHITE
            )
        )
        
        setContent { HavenTheme { HavenApp() } }
    }
}
