package com.example.haven.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haven.ui.pages.home.ChatWithPreview
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Locale

// Chat list colors
private val ChatNameColor = Color(0xFF735943)
private val SecondaryTextColor = Color(0xFFB9B9B9)

@Composable
fun ChatListItem(
    chat: ChatWithPreview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // For Notes (self chat) with no messages, don't show preview
    val isNotesEmpty = chat.title == "<self>" && chat.preview == "No messages yet"
    
    ListItem(
        leadingContent = if (chat.title == "<self>") {
            {
                Icon(
                    imageVector = Icons.Filled.Bookmark,
                    contentDescription = "Notes",
                    modifier = Modifier.size(44.dp).offset(x = (-8).dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else null,
        headlineContent = {
            Text(
                text = if (chat.title == "<self>") "Notes" else chat.title,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Medium,
                color = ChatNameColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = if (isNotesEmpty) null else {
            {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    // Sender name above message (like iOS)
                    chat.senderName?.let { sender ->
                        Text(
                            text = sender,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = SecondaryTextColor,
                            modifier = Modifier.padding(top = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = stripHtml(chat.preview),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = SecondaryTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        trailingContent = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (chat.timestamp > 0) {
                    Text(
                        text = formatChatTime(chat.timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                        color = if (chat.unreadCount > 0) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            SecondaryTextColor
                        }
                    )
                }
                if (chat.unreadCount > 0) {
                    UnreadBadge(count = chat.unreadCount)
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick)
    )
}

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
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyChatsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.size(16.dp))
        
        Text(
            text = "No messages yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Start a new conversation",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
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
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        }
        diff < 2 * dayInMillis -> "Yesterday"
        diff < 7 * dayInMillis -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date)
    }
}
