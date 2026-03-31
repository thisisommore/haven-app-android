package com.example.haven.ui.pages.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.DatabaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Data class representing a chat with its last message preview
 */
data class ChatWithPreview(
    val id: String,
    val title: String,
    val preview: String,
    val senderName: String? = null,
    val unreadCount: Int,
    val timestamp: Long = 0L,
    val isIncoming: Boolean? = null
)

/**
 * ViewModel for Home screen with real database integration
 */
class HomeViewModel(
    private val repository: DatabaseRepository
) : ViewModel() {

    // All chats from database with their last message preview
    val chatsWithPreview: Flow<List<ChatWithPreview>> = repository.getAllChats().map { chats ->
        chats.map { chat ->
            // Get the most recent message for preview
            val recentMessages = repository.getRecentMessages(chat.id, 1)
            val lastMessage = recentMessages.firstOrNull()
            
            // Get sender name for the last message
            val senderName = if (lastMessage != null) {
                if (lastMessage.isIncoming) {
                    lastMessage.senderId?.let { senderId ->
                        repository.getSenderById(senderId)?.let { sender ->
                            sender.nickname?.takeIf { it.isNotBlank() } 
                                ?: sender.codename
                        }
                    } ?: "Unknown"
                } else {
                    "you"
                }
            } else null
            
            ChatWithPreview(
                id = chat.id,
                title = chat.name,
                preview = lastMessage?.message ?: "No messages yet",
                senderName = senderName,
                unreadCount = chat.unreadCount,
                timestamp = lastMessage?.timestamp?.time ?: chat.joinedAt.time,
                isIncoming = lastMessage?.isIncoming
            )
        }.sortedByDescending { it.timestamp }
    }

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered chats based on search
    val filteredChats: Flow<List<ChatWithPreview>> = combine(
        chatsWithPreview,
        searchQuery
    ) { chats, query ->
        if (query.isBlank()) {
            chats
        } else {
            chats.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.preview.contains(query, ignoreCase = true) 
            }
        }
    }

    /**
     * Update search query
     */
    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    /**
     * Clear unread count for a chat
     */
    fun clearUnreadCount(chatId: String) {
        viewModelScope.launch {
            repository.clearUnreadCount(chatId)
        }
    }

    /**
     * Factory for creating ViewModel with repository
     */
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(DatabaseRepository(context.applicationContext)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
