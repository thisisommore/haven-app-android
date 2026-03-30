package com.example.haven

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.haven.data.db.ChatEntity
import com.example.haven.data.db.ChatMessageEntity
import com.example.haven.ui.components.MessageBubble
import com.example.haven.ui.components.MessageInputBar
import com.example.haven.ui.components.ReplyPreview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    chat: ChatEntity?,
    messages: List<ChatMessageEntity>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onReplyClick: (ChatMessageEntity) -> Unit,
    onBackClick: () -> Unit = {},
    replyingTo: ChatMessageEntity? = null,
    onCancelReply: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    val isAtBottom by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 || 
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == messages.size - 1
        }
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = chat?.name ?: "Chat",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (chat?.pubKey != null) {
                        Text(
                            text = "Direct Message",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (chat?.channelId != null) {
                        Text(
                            text = "Channel",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
            reverseLayout = true
        ) {
            items(
                items = messages.asReversed(),
                key = { it.id }
            ) { message ->
                MessageBubble(
                    message = message,
                    onReplyClick = { onReplyClick(message) },
                    isReplyingTo = replyingTo?.id == message.id
                )
            }
        }

        if (replyingTo != null) {
            ReplyPreview(
                message = replyingTo,
                onCancel = onCancelReply,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        MessageInputBar(
            value = inputText,
            onValueChange = onInputChange,
            onSend = onSendClick,
            placeholder = "Message...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// Legacy ChatPage for backward compatibility
@Composable
internal fun ChatPage(
    modifier: Modifier,
    messages: List<ChatMessageEntity>
) {
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true
    ) {
        items(messages.asReversed(), key = { it.id }) { msg ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (msg.isIncoming) Arrangement.Start else Arrangement.End
            ) {
                androidx.compose.material3.Surface(
                    color = if (msg.isIncoming)
                        MaterialTheme.colorScheme.surfaceVariant
                    else
                        MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    com.example.haven.ui.components.HtmlText(
                        html = msg.message,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
