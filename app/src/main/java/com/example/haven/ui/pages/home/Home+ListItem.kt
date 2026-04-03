package com.example.haven.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Admin Badge component
 * Mirrors iOS AdminBadge
 */
@Composable
fun AdminBadge(modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Text(
            text = "Admin",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * Secret Badge component
 * Mirrors iOS SecretBadge
 */
@Composable
fun SecretBadge(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = "Secret",
        modifier = modifier.size(16.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * Unread Badge component
 * Mirrors iOS UnreadBadge
 */
@Composable
fun UnreadBadge(count: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

/**
 * Chat Row View - List item for displaying chat in home screen
 * Mirrors iOS ChatRowView
 */
@Composable
fun ChatRowView(
    chat: ChatWithPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isNotes = chat.isNotes
    val isNotesEmpty = isNotes && chat.preview == "No messages yet"

    if (isNotesEmpty) {
        NotesEmptyRow(onClick = onClick, modifier = modifier)
        return
    }

    ListItem(
        leadingContent = if (isNotes) {
            {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Notes",
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        headlineContent = {
            ChatRowHeader(chat = chat)
        },
        supportingContent = {
            ChatRowSupportingContent(chat = chat, isNotes = isNotes)
        },
        trailingContent = {
            ChatRowTrailingContent(chat = chat)
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable(onClick = onClick)
    )
}

@Composable
private fun ChatRowHeader(
    chat: ChatWithPreview,
    modifier: Modifier = Modifier
) {
    val isNotes = chat.isNotes
    val displayName = if (isNotes) "Notes" else chat.title

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        Text(
            text = displayName,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = if (isNotes) 20.sp else 22.sp
            ),
            fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChatRowSupportingContent(
    chat: ChatWithPreview,
    isNotes: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = modifier
    ) {
        // Show sender name for non-Notes chats
        if (!isNotes) {
            chat.senderName?.let { sender ->
                Text(
                    text = sender,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            text = stripHtmlTags(chat.preview),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChatRowTrailingContent(
    chat: ChatWithPreview,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        if (chat.timestamp > 0) {
            Text(
                text = formatChatTime(chat.timestamp),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                color = if (chat.unreadCount > 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                }
            )
        }
        if (chat.unreadCount > 0) {
            UnreadBadge(count = chat.unreadCount)
        }
    }
}

@Composable
private fun NotesEmptyRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = Icons.Filled.Bookmark,
            contentDescription = "Notes",
            modifier = Modifier.size(38.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Notes",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatChatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val now = java.util.Date()
    val diff = now.time - date.time
    val dayInMillis = 24 * 60 * 60 * 1000

    return when {
        diff < dayInMillis && date.day == now.day -> {
            SimpleDateFormat("h:mma", Locale.getDefault()).format(date).lowercase()
        }
        diff < 2 * dayInMillis -> "Yesterday"
        diff < 7 * dayInMillis -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date)
    }
}

private fun stripHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
}
