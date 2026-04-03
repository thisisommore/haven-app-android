package com.example.haven.ui.pages.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.DatabaseRepository
import com.example.haven.xxdk.QRCodeUtils
import com.example.haven.xxdk.Parser
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
    val isIncoming: Boolean? = null,
    val isNotes: Boolean = false
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
                isIncoming = lastMessage?.isIncoming,
                isNotes = chat.name == "<self>"
            )
        }.sortedWith(
            compareByDescending<ChatWithPreview> { it.isNotes }
                .thenByDescending { it.timestamp }
        )
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
     * Handle scanned QR code and create DM chat
     */
    fun handleScannedQR(qrData: QRCodeUtils.QRData) {
        viewModelScope.launch {
            try {
                // Check if chat already exists with this pubkey
                val existingChat = repository.getChatByPubKey(qrData.pubKey)
                if (existingChat != null) {
                    Log.d("HomeViewModel", "Chat already exists for this user")
                    return@launch
                }
                
                // Construct identity from pubkey and codeset
                val identityJson = try {
                    bindings.Bindings.constructIdentity(qrData.pubKey, qrData.codeset.toLong())
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "Failed to construct identity: ${e.message}")
                    return@launch
                }
                
                val identity = Parser.decodeIdentity(identityJson)
                    ?: run {
                        Log.e("HomeViewModel", "Failed to decode identity")
                        return@launch
                    }
                
                // Parse color from identity
                val colorStr = identity.color.removePrefix("0x").removePrefix("0X")
                val color = colorStr.toIntOrNull(16) ?: 0xE97451
                
                // Convert token to Int32 like iOS does
                val dmToken = qrData.token.toInt()
                
                // Create new chat
                val newChat = ChatModel(
                    name = identity.codename,
                    pubKey = qrData.pubKey,
                    dmToken = dmToken,
                    color = color
                )
                
                repository.insertChat(newChat)
                Log.d("HomeViewModel", "Created DM chat with: ${identity.codename}")
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Failed to handle QR code: ${e.message}", e)
            }
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
