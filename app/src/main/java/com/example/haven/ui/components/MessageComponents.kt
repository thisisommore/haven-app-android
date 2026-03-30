package com.example.haven.ui.components

import androidx.compose.animation.animateContentSize

import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons


import androidx.compose.material.icons.filled.Close


import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.haven.data.db.ChatMessageEntity

import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MessageBubble(
    message: ChatMessageEntity,
    onReplyClick: () -> Unit,
    isReplyingTo: Boolean,
    modifier: Modifier = Modifier
) {
    val isMe = !message.isIncoming
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 4.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
    }

    val backgroundColor = when {
        isReplyingTo -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        isMe -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .wrapContentWidth()
                .animateContentSize()
        ) {
            Column(modifier = Modifier.padding(12.dp).wrapContentWidth()) {
                if (message.replyTo != null) {
                    ReplyIndicator(replyToId = message.replyTo, isMe = isMe, modifier = Modifier.padding(bottom = 8.dp))
                }

                HtmlText(
                    html = message.message,
                    color = contentColor,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                MessageFooter(message = message, isMe = isMe, contentColor = contentColor)
            }
        }


    }
}

@Composable
private fun MessageFooter(
    message: ChatMessageEntity,
    isMe: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTime(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.7f)
        )


    }
}

@Composable
fun ReplyIndicator(replyToId: String, isMe: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply icon removed
            
            Text(
                text = "Reply to message",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ReplyPreview(
    message: ChatMessageEntity,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reply icon removed
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stripHtmlTags(message.message),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel reply",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTime(date: java.util.Date): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}

private fun stripHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
}
