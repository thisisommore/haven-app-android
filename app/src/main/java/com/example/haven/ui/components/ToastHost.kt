package com.example.haven.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Toast data class representing a toast message
 */
data class ToastMessage(
    val message: String,
    val durationMillis: Long = 3000L
)

/**
 * ToastHost displays toast messages overlaying the content
 * Mirrors iOS toast overlay system
 */
@Composable
fun ToastHost(
    toast: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        content()

        AnimatedVisibility(
            visible = toast != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            toast?.let { toastData ->
                LaunchedEffect(toastData) {
                    delay(toastData.durationMillis)
                    onDismiss()
                }

                Toast(message = toastData.message)
            }
        }
    }
}

/**
 * Toast component - displays a single toast message
 */
@Composable
private fun Toast(message: String) {
    val havenColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 50.dp)
            .background(
                color = havenColor,
                shape = RoundedCornerShape(25.dp)
            )
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = Color.White
        )
    }
}

/**
 * ViewModel helper for toast state management
 */
class ToastManager {
    private var _toast: ToastMessage? = null
    private var onToastChange: ((ToastMessage?) -> Unit)? = null

    var toast: ToastMessage?
        get() = _toast
        set(value) {
            _toast = value
            onToastChange?.invoke(value)
        }

    fun setOnToastChangeListener(listener: (ToastMessage?) -> Unit) {
        onToastChange = listener
    }

    fun showToast(message: String, durationMillis: Long = 3000L) {
        toast = ToastMessage(message, durationMillis)
    }

    fun dismiss() {
        toast = null
    }
}
