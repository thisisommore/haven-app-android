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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.SpeakerNotesOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.haven.ui.components.MessageDeleteSheet
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.haven.ui.views.MessageBubble
import com.example.haven.ui.views.MessageInputBar
import com.example.haven.ui.views.ReplyPreview
import com.example.haven.ui.pages.chat.ReactionPickerSheet

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun ChatView(
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
    showOptionsSheet: Boolean = false,
    onOptionsDismiss: () -> Unit = {},
    onLeaveChannel: () -> Unit = {},
    onDeleteChat: () -> Unit = {},
    onInfoClick: () -> Unit = {},
    optionsController: ChannelOptionsController? = null,
    isCurrentUserMuted: Boolean = false,
    onSendReaction: (String, String) -> Unit = { _, _ -> },
    onDeleteMessage: (String) -> Unit = {},
    reactions: Map<String, List<com.example.haven.data.model.MessageReactionModel>> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val reversedMessages = remember(messages) { messages.asReversed() }
    
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

    // Reaction picker state
    var showReactionPicker by remember { mutableStateOf(false) }
    var selectedMessageForReaction by remember { mutableStateOf<ChatMessageModel?>(null) }
    val recentReactionsStore = remember { DatabaseModule.provideRecentReactionsStore(context) }
    val recentReactions by recentReactionsStore.recentReactions.collectAsState(initial = emptyList())

    // Delete confirmation state
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<ChatMessageModel?>(null) }
    
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
                        .clickable(
                            enabled = chat != null,
                            onClick = onInfoClick
                        )
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

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                val isNotes = chat?.name == "<self>"
                Text(
                    text = if (isNotes) "Drop your notes here" else "No messages yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.Bottom,
                reverseLayout = true
            ) {
                itemsIndexed(
                    items = reversedMessages,
                    key = { _, it -> it.id }
                ) { index, message ->
                    val previousMessage = if (index > 0) reversedMessages[index - 1] else null
                    val nextMessage = if (index < reversedMessages.size - 1) reversedMessages[index + 1] else null
                    
                    // nextMessage is physically ABOVE (chronologically older)
                    // previousMessage is physically BELOW (chronologically newer)
                    
                    val isSameAsAbove = nextMessage != null && 
                                        nextMessage.isIncoming == message.isIncoming && 
                                        nextMessage.senderId == message.senderId
                                        
                    val isSameAsBelow = previousMessage != null && 
                                        previousMessage.isIncoming == message.isIncoming && 
                                        previousMessage.senderId == message.senderId

                    val isFirstInCluster = !isSameAsAbove
                    val isLastInCluster = !isSameAsBelow
                    
                    // Gap between this message and the one ABOVE it (nextMessage)
                    val paddingTop = if (nextMessage != null) {
                        if (isSameAsAbove) 4.dp else 12.dp
                    } else {
                        0.dp
                    }
                    
                    Box(modifier = Modifier.padding(top = paddingTop)) {
                        MessageBubble(
                            message = message,
                            reactions = reactions[message.externalId] ?: emptyList(),
                            onReplyClick = { onReplyClick(message) },
                            onReactClick = {
                                selectedMessageForReaction = message
                                showReactionPicker = true
                            },
                            onDeleteClick = {
                                messageToDelete = message
                                showDeleteConfirmation = true
                            },
                            canDelete = !message.isIncoming,
                            isReplyingTo = replyingTo?.id == message.id,
                            senderName = getSenderName(message.senderId),
                            showSenderName = isFirstInCluster,
                            isFirstInCluster = isFirstInCluster,
                            isLastInCluster = isLastInCluster
                        )
                    }
                }
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
        
        if (isCurrentUserMuted) {
            // Show muted chip when user is muted
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .then(if (!isImeVisible) Modifier.navigationBarsPadding() else Modifier)
                    .imePadding(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SpeakerNotesOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "You are muted in this channel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
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
        }
        
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

        // Reaction Picker Sheet
        if (showReactionPicker && selectedMessageForReaction != null) {
            ReactionPickerSheet(
                onDismiss = { showReactionPicker = false },
                onReactionSelected = { emoji ->
                    selectedMessageForReaction?.let { message ->
                        onSendReaction(message.externalId, emoji)
                        coroutineScope.launch {
                            recentReactionsStore.addRecentReaction(emoji)
                        }
                    }
                    showReactionPicker = false
                    selectedMessageForReaction = null
                },
                recentReactions = recentReactions
            )
        }

        // Delete Confirmation Sheet
        if (showDeleteConfirmation && messageToDelete != null) {
            MessageDeleteSheet(
                onDismiss = {
                    showDeleteConfirmation = false
                    messageToDelete = null
                },
                onConfirm = {
                    messageToDelete?.let { onDeleteMessage(it.externalId) }
                    showDeleteConfirmation = false
                    messageToDelete = null
                }
            )
        }

        // Channel Options Sheet
        if (showOptionsSheet && chat != null && optionsController != null) {
            ChannelOptionsSheet(
                chat = chat,
                viewModel = optionsController,
                onDismiss = onOptionsDismiss,
                onLeaveChannel = onLeaveChannel,
                onDeleteChat = onDeleteChat
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
