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
        
        // Set status bar color to match header background (#FFEEE2)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.parseColor("#FFEEE2"),
                darkScrim = Color.parseColor("#FFEEE2")
            )
        )
        
        setContent { HavenTheme { HavenApp() } }
    }
}
