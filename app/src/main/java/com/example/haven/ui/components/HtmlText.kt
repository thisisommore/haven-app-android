package com.example.haven.ui.components

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat

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
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                this.maxLines = maxLines
                // Disable touch handling so parent can receive long-press
                isClickable = false
                isLongClickable = false
                setOnTouchListener { _, _ -> false }
            }
        },
        update = { textView ->
            val spanned = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
            // Remove trailing newlines added by block elements (p, div, br, etc.)
            textView.text = spanned.trimEnd()
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
