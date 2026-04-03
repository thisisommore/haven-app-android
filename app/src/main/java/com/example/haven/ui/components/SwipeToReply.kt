package com.example.haven.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Swipe to reply gesture handler
 * Mirrors iOS MessageBubbleSwipe functionality
 *
 * @param onSwipe Triggered when swipe threshold is crossed
 * @param content The content composable to apply swipe to
 */
@Composable
fun SwipeToReplyContainer(
    onSwipe: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val offsetX = remember { Animatable(0f) }
    var hasCrossedThreshold = remember { false }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        hasCrossedThreshold = false
                    },
                    onDragEnd = {
                        if (hasCrossedThreshold) {
                            onSwipe()
                        }
                        // Spring back to original position
                        scope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)
                            )
                        }
                        hasCrossedThreshold = false
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetX.value + dragAmount).coerceIn(0f, 100f)
                        scope.launch {
                            offsetX.snapTo(newOffset)
                        }

                        // Check threshold and trigger haptic
                        if (newOffset >= 60f && !hasCrossedThreshold) {
                            hasCrossedThreshold = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else if (newOffset < 60f && hasCrossedThreshold) {
                            hasCrossedThreshold = false
                        }
                    }
                )
            }
    ) {
        // Background reply icon (shown during swipe)
        if (offsetX.value > 0) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Reply,
                contentDescription = "Reply",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (offsetX.value * 0.3f).dp)
            )
        }

        // Content with offset
        Box(
            modifier = Modifier.offset { IntOffset(offsetX.value.roundToInt(), 0) }
        ) {
            content()
        }
    }
}
