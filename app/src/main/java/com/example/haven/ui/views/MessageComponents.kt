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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
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
import com.example.haven.data.model.MessageReactionModel
import com.example.haven.data.model.MessageStatus
import com.example.haven.ui.components.SwipeToReplyContainer
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessageModel,
    reactions: List<MessageReactionModel> = emptyList(),
    onReplyClick: () -> Unit,
    onReactClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    canDelete: Boolean = false,
    isReplyingTo: Boolean,
    senderName: String? = null,
    showSenderName: Boolean = true,
    isFirstInCluster: Boolean = true,
    isLastInCluster: Boolean = true,
    enableSwipeToReply: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isMe = !message.isIncoming

    // Corner radius based on position in message cluster
    val bubbleShape = when {
        // First message in cluster: round top (20), medium bottom (12)
        isFirstInCluster && !isLastInCluster -> {
            RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = 12.dp,
                bottomEnd = 12.dp
            )
        }
        // Middle message: all corners medium (12)
        !isFirstInCluster && !isLastInCluster -> {
            RoundedCornerShape(12.dp)
        }
        // Last message (or single message): top depends on if first, bottom depends on direction
        else -> {
            val topRadius = if (isFirstInCluster) 20.dp else 12.dp
            val bottomOuterRadius = 20.dp  // side away from sender
            val bottomInnerRadius = 0.dp   // side toward sender (sharp)

            if (isMe) {
                // Outgoing: sharp bottom-right
                RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomStart = bottomOuterRadius,
                    bottomEnd = bottomInnerRadius
                )
            } else {
                // Incoming: sharp bottom-left
                RoundedCornerShape(
                    topStart = topRadius,
                    topEnd = topRadius,
                    bottomStart = bottomInnerRadius,
                    bottomEnd = bottomOuterRadius
                )
            }
        }
    }

    val backgroundColor = when {
        isReplyingTo -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        isMe -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer

    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Content composable that will be wrapped with swipe
    @Composable
    fun MessageContent() {
        Card(
            shape = bubbleShape,
            colors = CardDefaults.cardColors(containerColor = backgroundColor, contentColor = contentColor),
            modifier = Modifier
                .widthIn(max = 280.dp)
                .wrapContentWidth()
                .animateContentSize()
        ) {
            val showSender = !isMe && senderName != null && showSenderName
            Column(
                modifier = Modifier
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = if (showSender) 12.dp else 8.dp,
                        bottom = 12.dp
                    )
                    .width(IntrinsicSize.Min)
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { },
                        onLongClick = { showMenu = true }
                    )
            ) {
                // Context Menu - Material 3 Expressive (Vibrant)
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    shape = RoundedCornerShape(12.dp),
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp
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
                                modifier = Modifier.size(24.dp)
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
                                modifier = Modifier.size(24.dp)
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    )
                    if (canDelete) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error,
                                leadingIconColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }

                // Sender name for incoming messages (only show for first message in group)
                if (showSender) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.7f)
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

                // Reactions row
                if (reactions.isNotEmpty()) {
                    ReactionsRow(
                        reactions = reactions,
                        isMe = isMe,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    // Wrap with swipe-to-reply if enabled
    if (enableSwipeToReply) {
        SwipeToReplyContainer(
            onSwipe = onReplyClick,
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
            ) {
                MessageContent()
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
        ) {
            MessageContent()
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
    val status = message.getMessageStatus()

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
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )

        // Status indicator for outgoing messages
        if (isMe) {
            Spacer(modifier = Modifier.width(4.dp))
            when (status) {
                MessageStatus.UNSENT -> {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Sending",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                MessageStatus.SENT -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Sent",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                MessageStatus.DELIVERED -> {
                    // Double checkmark for delivered
                    Row {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Delivered",
                            tint = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = contentColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp).padding(start = (-8).dp)
                        )
                    }
                }
                MessageStatus.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                else -> {}
            }
        }
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
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date).replace(" ", "").lowercase()
}

@Composable
private fun ReactionsRow(
    reactions: List<MessageReactionModel>,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    // Group reactions by emoji and count them
    val groupedReactions = reactions.groupBy { it.emoji }
        .mapValues { it.value.size }
        .toList()

    Row(
        modifier = modifier,
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        groupedReactions.forEach { (emoji, count) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = emoji,
                        fontSize = 14.sp
                    )
                    if (count > 1) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun stripHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
}
