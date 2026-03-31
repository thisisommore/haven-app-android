package com.example.haven.ui.components

import android.content.Intent
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.core.text.getSpans

/**
 * Trim trailing whitespace/newlines from Spanned text
 */
private fun Spanned.trimEnd(): Spanned {
    val str = this.toString()
    val end = str.length
    var newEnd = end
    while (newEnd > 0 && str[newEnd - 1].isWhitespace()) {
        newEnd--
    }
    return if (newEnd < end) {
        SpannableStringBuilder(this).delete(newEnd, end) as Spanned
    } else {
        this
    }
}

/**
 * Composable that displays HTML formatted text.
 * Uses Android's built-in HtmlCompat.fromHtml() for parsing.
 */
@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = Int.MAX_VALUE
) {
    val context = LocalContext.current
    var pendingUrl by remember { mutableStateOf<String?>(null) }
    
    // Show external link warning dialog
    pendingUrl?.let { url ->
        val linkPreview = if (url.length > 100) {
            "${url.take(50)}..."
        } else {
            url
        }
        
        AlertDialog(
            onDismissRequest = { pendingUrl = null },
            title = { Text("Leaving Haven") },
            text = {
                Text(
                    """
                    You are about to open an external link. Haven's privacy and security protections do not apply.
                    
                    Link: $linkPreview
                    """.trimIndent()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingUrl = null
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Open")
                }
            },
            dismissButton = {
                Button(
                    onClick = { pendingUrl = null },
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            TextView(ctx).apply {
                this.maxLines = maxLines
                // Enable links to be clickable
                movementMethod = LinkMovementMethod.getInstance()
                // Links need clickable enabled, but we handle long-press at parent level
                isLongClickable = false
            }
        },
        update = { textView ->
            val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            // Remove trailing newlines added by block elements (p, div, br, etc.)
            val trimmed = spanned.trimEnd()
            
            // Replace URLSpans with custom clickable spans that show warning dialog
            val spannable = SpannableStringBuilder(trimmed)
            val urlSpans = trimmed.getSpans(0, trimmed.length, URLSpan::class.java)
            
            for (urlSpan in urlSpans) {
                val start = trimmed.getSpanStart(urlSpan)
                val end = trimmed.getSpanEnd(urlSpan)
                val url = urlSpan.url
                
                spannable.removeSpan(urlSpan)
                spannable.setSpan(
                    object : ClickableSpan() {
                        override fun onClick(widget: View) {
                            pendingUrl = url
                        }
                    },
                    start,
                    end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            
            textView.text = spannable
            // Set text color if specified
            if (color != Color.Unspecified) {
                textView.setTextColor(color.toArgb())
            }
        }
    )
}

/**
 * Strip HTML tags to get plain text for previews.
 */
fun stripHtml(html: String): String {
    return HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
        .toString()
        .trim()
}
