package com.example.haven.ui.pages.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon

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
import androidx.compose.ui.graphics.Color
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.ui.views.MessageBubble
import com.example.haven.ui.views.MessageInputBar
import com.example.haven.ui.views.ReplyPreview

private val ChatBgColor = Color(0xFFFFF8F5) // Light peach background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    chat: ChatModel?,
    messages: List<ChatMessageModel>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onReplyClick: (ChatMessageModel) -> Unit,
    onBackClick: () -> Unit = {},
    replyingTo: ChatMessageModel? = null,
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
            .background(ChatBgColor)
            .navigationBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(40.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (chat?.name) {
                                null -> "Chat"
                                "<self>" -> "Notes"
                                else -> chat.name
                            },
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
                    Spacer(modifier = Modifier.width(40.dp))
                }
            },
            navigationIcon = {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = Color(0xFF5D4127),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable(onClick = onBackClick)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(
                            color = Color(0xFF5D4127),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable(onClick = { /* TODO: Show info */ })
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChatBgColor
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

// Legacy ChatPage for backward compatibility
@Composable
internal fun ChatPage(
    modifier: Modifier,
    messages: List<ChatMessageModel>
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
                    com.example.haven.ui.views.HtmlText(
                        html = msg.message,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
