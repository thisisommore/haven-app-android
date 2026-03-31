package com.example.haven.ui.pages.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.DatabaseRepository
import com.example.haven.xxdk.XXDK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Chat screen with real database integration and XXDK sending
 */
class ChatViewModel(
    private val repository: DatabaseRepository,
    private val xxdk: XXDK
) : ViewModel() {

    // Current chat being viewed
    private val _currentChat = MutableStateFlow<ChatModel?>(null)
    val currentChat: StateFlow<ChatModel?> = _currentChat.asStateFlow()

    // Messages for current chat
    var messages: Flow<List<ChatMessageModel>> = MutableStateFlow(emptyList())
        private set

    // Input message text
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Load a chat by its ID and start observing messages
     */
    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val chat = repository.getChatById(chatId)
            _currentChat.value = chat
            if (chat != null) {
                messages = repository.getMessagesByChatId(chatId)
                // Mark all messages as read when opening chat
                repository.markAllMessagesAsRead(chatId)
            }
            _isLoading.value = false
        }
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
                android.util.Log.e("ChatViewModel", "Failed to send message: ${e.message}")
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
                android.util.Log.e("ChatViewModel", "Failed to send reply: ${e.message}")
            }
            
            _inputText.value = ""
        }
    }

    /**
     * Factory for creating ViewModel with repository and XXDK
     */
    class Factory(
        private val context: Context,
        private val xxdk: XXDK
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                return ChatViewModel(
                    DatabaseRepository(context.applicationContext),
                    xxdk
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Extension for collecting Flow in Compose
 */
fun <T> Flow<T>.asMutableStateFlow(): MutableStateFlow<T> {
    return this as? MutableStateFlow<T> ?: MutableStateFlow<T?>(null) as MutableStateFlow<T>
}
