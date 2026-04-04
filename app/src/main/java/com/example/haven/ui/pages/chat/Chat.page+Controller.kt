package com.example.haven.ui.pages.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.model.MessageReactionModel
import com.example.haven.data.DatabaseRepository
import com.example.haven.xxdk.XXDK
import com.example.haven.xxdk.callbacks.ReceiverHelpers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Controller for Chat screen with real database integration and XXDK sending
 * Matches iOS ChatPageController
 */
class ChatPageController(
    private val context: Context,
    private val repository: DatabaseRepository,
    private val xxdk: XXDK
) : ViewModel() {

    // Current chat being viewed
    private val _currentChat = MutableStateFlow<ChatModel?>(null)
    val currentChat: StateFlow<ChatModel?> = _currentChat.asStateFlow()

    // Messages for current chat
    private val _messages = MutableStateFlow<List<ChatMessageModel>>(emptyList())
    val messages: StateFlow<List<ChatMessageModel>> = _messages.asStateFlow()

    // Reactions for current chat messages
    private val _reactions = MutableStateFlow<Map<String, List<MessageReactionModel>>>(emptyMap())
    val reactions: StateFlow<Map<String, List<MessageReactionModel>>> = _reactions.asStateFlow()
    private var reactionsCollectionJob: kotlinx.coroutines.Job? = null
    
    // Job for collecting messages
    private var messagesCollectionJob: kotlinx.coroutines.Job? = null

    // Input message text
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Current user muted state
    private val _isCurrentUserMuted = MutableStateFlow(false)
    val isCurrentUserMuted: StateFlow<Boolean> = _isCurrentUserMuted.asStateFlow()

    // Job for collecting muted users
    private var mutedUsersCollectionJob: kotlinx.coroutines.Job? = null

    /**
     * Load a chat by its ID and start observing messages
     */
    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val chat = repository.getChatById(chatId)
            _currentChat.value = chat
            if (chat != null) {
                // Preload self pubkey for incoming/outgoing detection
                ReceiverHelpers.getInstance(context).preloadSelfPubKey()
                
                // Cancel previous collections
                messagesCollectionJob?.cancel()
                reactionsCollectionJob?.cancel()
                mutedUsersCollectionJob?.cancel()

                // Start collecting messages from the new chat
                messagesCollectionJob = viewModelScope.launch {
                    repository.getMessagesByChatId(chatId).collect { messageList ->
                        _messages.value = messageList
                    }
                }

                // Start collecting reactions for messages in this chat
                reactionsCollectionJob = viewModelScope.launch {
                    repository.getReactionsByChatId(chatId).collect { reactionList ->
                        _reactions.value = reactionList.groupBy { it.targetMessageId }
                    }
                }
                
                // Start collecting muted users for channels
                if (chat.channelId != null) {
                    mutedUsersCollectionJob = viewModelScope.launch {
                        repository.getMutedUsersByChannelId(chat.channelId).collectLatest { mutedUsers ->
                            _isCurrentUserMuted.value = checkIfCurrentUserMuted(mutedUsers)
                        }
                    }
                } else {
                    _isCurrentUserMuted.value = false
                }
                
                // Mark all messages as read when opening chat
                repository.markAllMessagesAsRead(chatId)
            }
            _isLoading.value = false
        }
    }

    /**
     * Check if the current user is muted in the channel
     * Compares against self user's pubkey from XXDK
     */
    private fun checkIfCurrentUserMuted(mutedUsers: List<com.example.haven.data.model.ChannelMutedUserModel>): Boolean {
        // Get current user's public key from XXDK
        val selfPubKey = try {
            val privateIdentity = xxdk.loadSavedPrivateIdentity()
            val publicIdentityBytes = bindings.Bindings.getPublicChannelIdentityFromPrivate(privateIdentity)
            val publicIdentity = com.example.haven.xxdk.Parser.decodeIdentity(publicIdentityBytes)
            // PubKey is base64 encoded string, decode to ByteArray
            android.util.Base64.decode(publicIdentity.pubKey, android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Log.e("ChatPageController", "Failed to get self pubkey: ${e.message}")
            return false
        }
        
        return mutedUsers.any { it.pubkey.contentEquals(selfPubKey) }
    }

    /**
     * Update input text
     */
    fun onInputChange(text: String) {
        _inputText.value = text
    }

    /**
     * Send a message - uses XXDK to send.
     * Message will be added to DB via callbacks when network confirms.
     */
    fun sendMessage() {
        val text = _inputText.value.trim()
        val chat = _currentChat.value ?: return
        
        if (text.isBlank()) return

        viewModelScope.launch {
            // Send via XXDK based on chat type (only if clients are initialized)
            // Message will be added to DB via receive callbacks
            try {
                if (chat.pubKey != null && chat.dmToken != null) {
                    // DM chat
                    xxdk.dm.send(text, chat.pubKey, chat.dmToken)
                } else if (chat.channelId != null) {
                    // Channel chat
                    xxdk.channel.msg.send(text, chat.channelId)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatPageController", "Failed to send message: ${e.message}")
            }
            
            _inputText.value = ""
        }
    }

    /**
     * Send a reply to a specific message.
     * Reply will be added to DB via callbacks when network confirms.
     */
    fun sendReply(replyToMessageId: String) {
        val text = _inputText.value.trim()
        val chat = _currentChat.value ?: return
        
        if (text.isBlank()) return

        viewModelScope.launch {
            // Send reply via XXDK (only if clients are initialized)
            // Reply will be added to DB via receive callbacks
            try {
                if (chat.pubKey != null && chat.dmToken != null) {
                    xxdk.dm.reply(text, chat.pubKey, chat.dmToken, replyToMessageId)
                } else if (chat.channelId != null) {
                    xxdk.channel.msg.reply(text, chat.channelId, replyToMessageId)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatPageController", "Failed to send reply: ${e.message}")
            }
            
            _inputText.value = ""
        }
    }

    /**
     * Get sender name from senderId (similar to iOS getName(from:))
     * senderId is the MessageSenderModel UUID (not pubkey)
     */
    suspend fun getSenderName(senderId: String?): String {
        if (senderId == null) return ""

        val sender = repository.getSenderById(senderId)
        return sender?.let {
            if (!it.nickname.isNullOrBlank()) it.nickname else it.codename
        } ?: "Unknown"
    }

    /**
     * Synchronous version for Compose UI (uses cached data or returns empty).
     * For async loading, use getSenderName() within a coroutine.
     */
    fun getSenderNameSync(senderId: String?): String {
        // Return cached value immediately - UI should observe sender updates separately
        return ""
    }

    /**
     * Send a reaction (emoji) to a message.
     * Reaction will be added to DB via callbacks when network confirms.
     */
    fun sendReaction(messageId: String, emoji: String) {
        val chat = _currentChat.value ?: return

        viewModelScope.launch {
            try {
                if (chat.pubKey != null && chat.dmToken != null) {
                    // DM reaction
                    xxdk.dm.react(emoji, messageId, chat.pubKey, chat.dmToken)
                } else if (chat.channelId != null) {
                    // Channel reaction
                    xxdk.channel.msg.react(emoji, messageId, chat.channelId)
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatPageController", "Failed to send reaction: ${e.message}")
            }
        }
    }

    /**
     * Delete a message.
     * Message deletion will be synced via callbacks.
     */
    fun deleteMessage(messageId: String) {
        val chat = _currentChat.value ?: return

        viewModelScope.launch {
            try {
                if (chat.channelId != null) {
                    xxdk.channel.msg.delete(messageId, chat.channelId)
                }
                // For DMs, deletion might not be supported or work differently
            } catch (e: Exception) {
                android.util.Log.e("ChatPageController", "Failed to delete message: ${e.message}")
            }
        }
    }

    /**
     * Factory for creating Controller with repository and XXDK
     * Matches iOS pattern
     */
    class Factory(
        private val context: Context,
        private val xxdk: XXDK
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatPageController::class.java)) {
                return ChatPageController(
                    context.applicationContext,
                    DatabaseRepository(context.applicationContext),
                    xxdk
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

