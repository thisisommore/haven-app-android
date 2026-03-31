package com.example.haven

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.haven.xxdk.XXDKStorage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Landing page showing app branding and loading progress.
 * Matches iOS LandingPage design.
 */
@Composable
internal fun LandingPage(
    modifier: Modifier,
    status: String,
    statusPercentage: Int,
    isSetupComplete: Boolean,
    onLoadingComplete: () -> Unit = {},
    appStorage: XXDKStorage? = null,
) {
    // Observe isSetupComplete changes for new users
    val setupComplete by appStorage?.isSetupCompleteFlow?.collectAsState(initial = isSetupComplete) 
        ?: remember { mutableStateOf(isSetupComplete) }
    var showProgress by remember { mutableStateOf(false) }
    var isLoadingDone by remember { mutableStateOf(false) }
    var moveUp by remember { mutableStateOf(false) }

    // Animation: move content up
    val offsetY by animateFloatAsState(
        targetValue = if (moveUp) -40f else 0f,
        animationSpec = tween(durationMillis = 2000),
        label = "moveUp"
    )

    // 1) Delay before showing progress
    LaunchedEffect(Unit) {
        delay(1000)
        moveUp = true
        delay(300)
        showProgress = true
    }

    // Mark loading done when complete and navigate to home
    LaunchedEffect(showProgress, statusPercentage, setupComplete) {
        if (showProgress && statusPercentage == 100 && setupComplete && !isLoadingDone) {
            isLoadingDone = true
            delay(500) // Small delay to show completion
            onLoadingComplete()
        }
    }

    LaunchedEffect(setupComplete) {
        if (setupComplete && !isLoadingDone) {
            isLoadingDone = true
            delay(500)
            onLoadingComplete()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = offsetY.dp)
        ) {
            // Title section - left aligned like iOS
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "XXNetwork",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = "Haven App.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 12.sp
                )
            }

            // Progress section
            AnimatedVisibility(
                visible = showProgress && !isLoadingDone,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) { -it } + fadeIn(animationSpec = tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { statusPercentage / 100f },
                        modifier = Modifier.width(120.dp),
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = status,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
