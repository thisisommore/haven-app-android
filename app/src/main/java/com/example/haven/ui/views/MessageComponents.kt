package com.example.haven.ui.views

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.ui.unit.sp
import com.example.haven.ui.theme.InterFontFamily
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.haven.data.model.ChatMessageModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessageModel,
    onReplyClick: () -> Unit,
    onReactClick: () -> Unit = {},
    isReplyingTo: Boolean,
    senderName: String? = null,
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
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .width(IntrinsicSize.Min)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                        onLongClick = { showMenu = true }
                    )
            ) {
                // Context Menu
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    DropdownMenuItem(
                        text = { Text("Reply") },
                        onClick = {
                            onReplyClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("React") },
                        onClick = {
                            onReactClick()
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Face,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy") },
                        onClick = {
                            copyToClipboard(context, message.message)
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.MailOutline,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                }

                // Sender name for incoming messages
                if (!isMe && senderName != null) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 0.dp)
                    )
                }

                if (message.replyTo != null) {
                    ReplyIndicator(replyToId = message.replyTo, isMe = isMe, modifier = Modifier.padding(bottom = 8.dp))
                }

                HtmlText(
                    html = message.message,
                    color = contentColor,
                    textSize = 20f,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                MessageFooter(
                    message = message,
                    isMe = isMe,
                    contentColor = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth().wrapContentHeight()
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Message", text)
    clipboard.setPrimaryClip(clip)
}

@Composable
private fun MessageFooter(
    message: ChatMessageModel,
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
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = com.example.haven.ui.theme.InterFontFamily,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
            ),
            color = contentColor.copy(alpha = 0.8f),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

@Composable
fun ReplyIndicator(replyToId: String, isMe: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reply to message",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = com.example.haven.ui.theme.InterFontFamily,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                ),
                color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ReplyPreview(
    message: ChatMessageModel,
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
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = com.example.haven.ui.theme.InterFontFamily,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stripHtmlTags(message.message),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = com.example.haven.ui.theme.InterFontFamily,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Normal
                    ),
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
