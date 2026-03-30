package com.example.haven

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Landing page showing app branding and loading progress.
 * Similar to iOS LandingPage - shows progress but doesn't handle navigation.
 */
@Composable
internal fun LandingPage(
    modifier: Modifier,
    status: String,
    statusPercentage: Int,
    isSetupComplete: Boolean,
) {
    var showProgress by remember { mutableStateOf(false) }
    var isLoadingDone by remember { mutableStateOf(false) }

    // Delay before showing progress (matches iOS: 1s delay)
    LaunchedEffect(Unit) {
        delay(1000)
        showProgress = true
    }

    // Mark loading done when complete (matches iOS behavior)
    LaunchedEffect(showProgress, statusPercentage, isSetupComplete) {
        if (showProgress && statusPercentage == 100 && isSetupComplete && !isLoadingDone) {
            isLoadingDone = true
        }
    }

    // Also mark done when setup becomes complete
    LaunchedEffect(isSetupComplete) {
        if (isSetupComplete && !isLoadingDone) {
            isLoadingDone = true
        }
    }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("XXNetwork", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Haven App.", style = MaterialTheme.typography.bodyMedium)

        AnimatedVisibility(
            visible = showProgress,
            enter = slideInVertically { -it } + fadeIn()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(24.dp))
                // Material 3 wavy indeterminate progress when loading
                if (statusPercentage < 100) {
                    LinearProgressIndicator(
                        modifier = Modifier.width(200.dp),
                    )
                } else {
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.width(200.dp),
                    )
                }
                Text(
                    status, 
                    Modifier.padding(top = 12.dp), 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
