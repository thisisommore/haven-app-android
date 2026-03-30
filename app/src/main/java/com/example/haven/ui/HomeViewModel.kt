package com.example.haven.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.db.ChatEntity
import com.example.haven.data.db.ChatMessageEntity
import com.example.haven.data.db.DatabaseRepository
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
    val unreadCount: Int,
    val timestamp: Long = 0L
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
            
            ChatWithPreview(
                id = chat.id,
                title = chat.name,
                preview = lastMessage?.message ?: "No messages yet",
                unreadCount = chat.unreadCount,
                timestamp = lastMessage?.timestamp?.time ?: chat.joinedAt.time
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
