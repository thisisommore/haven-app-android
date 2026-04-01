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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haven.data.DatabaseModule
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.ui.views.EmojiPicker
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.haven.ui.views.MessageBubble
import com.example.haven.ui.views.MessageInputBar
import com.example.haven.ui.views.ReplyPreview

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
    getSenderName: (String?) -> String = { "" },
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

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val showEmojiPicker = remember { mutableStateOf(false) }
    val recentEmojiStore = remember { DatabaseModule.provideRecentEmojiStore(context) }
    val recentEmojis by recentEmojiStore.recentEmojis.collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            title = {
                Text(
                    text = when (chat?.name) {
                        null -> "Chat"
                        "<self>" -> "Notes"
                        else -> chat.name
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable(onClick = onBackClick)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            actions = {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable(onClick = { /* TODO: Show info */ })
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Bottom,
            reverseLayout = true
        ) {
            items(
                items = messages.asReversed(),
                key = { it.id }
            ) { message ->
                val reversedMessages = messages.asReversed()
                val index = reversedMessages.indexOf(message)
                val previousMessage = if (index > 0) reversedMessages[index - 1] else null
                val nextMessage = if (index < reversedMessages.size - 1) reversedMessages[index + 1] else null
                
                // Cluster logic: consecutive messages from same sender
                val isFirstInCluster = previousMessage?.senderId != message.senderId
                val isLastInCluster = nextMessage?.senderId != message.senderId
                
                MessageBubble(
                    message = message,
                    onReplyClick = { onReplyClick(message) },
                    isReplyingTo = replyingTo?.id == message.id,
                    senderName = getSenderName(message.senderId),
                    showSenderName = isFirstInCluster,
                    isFirstInCluster = isFirstInCluster,
                    isLastInCluster = isLastInCluster,
                    isNewSender = isFirstInCluster
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

        val isImeVisible = WindowInsets.isImeVisible
        
        MessageInputBar(
            value = inputText,
            onValueChange = onInputChange,
            onSend = onSendClick,
            onEmojiClick = {
                if (showEmojiPicker.value) {
                    showEmojiPicker.value = false
                    keyboardController?.show()
                } else {
                    showEmojiPicker.value = true
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .then(if (!isImeVisible && !showEmojiPicker.value) Modifier.navigationBarsPadding() else Modifier)
                .imePadding()
        )
        
        if (showEmojiPicker.value) {
            EmojiPicker(
                onEmojiSelected = { emoji ->
                    onInputChange(inputText + emoji)
                    coroutineScope.launch {
                        recentEmojiStore.addRecentEmoji(emoji)
                    }
                },
                recentEmojis = recentEmojis,
                modifier = Modifier.navigationBarsPadding()
            )
        }
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
