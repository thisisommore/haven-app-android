package com.example.haven.xxdk.callbacks

import android.content.Context
import android.util.Log
import com.example.haven.data.DatabaseModule
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.data.model.MessageStatus
import com.example.haven.xxdk.Parser
import bindings.Bindings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

/**
 * Common utilities shared between DMReceiver and EventModel (Channels)
 */
class ReceiverHelpers private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: ReceiverHelpers? = null

        fun getInstance(context: Context): ReceiverHelpers {
            return instance ?: synchronized(this) {
                instance ?: ReceiverHelpers(context.applicationContext).also { instance = it }
            }
        }

        fun clearInstance() {
            instance = null
        }

        private var cachedSelfChatPubKey: ByteArray? = null
    }

    private val repository by lazy { DatabaseModule.provideRepository(context) }

    /**
     * Parse identity from pubKey and codeset, returning codename and color
     * Mirrors iOS ReceiverHelpers.parseIdentity
     */
    fun parseIdentity(pubKey: ByteArray, codeset: Long): Pair<String, Int> {
        val identityJson = try {
            Bindings.constructIdentity(pubKey, codeset)
        } catch (e: Exception) {
            throw IllegalStateException("Failed to construct identity: ${e.message}")
        }

        val identity = Parser.decodeIdentity(identityJson)
            ?: throw IllegalStateException("Failed to decode identity")

        val colorStr = identity.color.removePrefix("0x").removePrefix("0X")
        val color = colorStr.toIntOrNull(16) ?: 0xE97451

        return Pair(identity.codename, color)
    }

    /**
     * Check if sender's pubKey matches the "<self>" chat pubKey
     * Lazy-loads from database if not cached
     *
     * Note: This is a suspend function to avoid blocking threads.
     * Call from coroutines only, never from synchronous callbacks.
     */
    suspend fun isSenderSelf(senderPubKey: ByteArray?): Boolean {
        if (senderPubKey == null) return false

        // Lazy-load self pubkey if not cached
        if (cachedSelfChatPubKey == null) {
            try {
                cachedSelfChatPubKey = withContext(Dispatchers.IO) {
                    repository.getAllChats()
                        .firstOrNull { chats ->
                            chats.any { it.name == "<self>" }
                        }
                        ?.firstOrNull { it.name == "<self>" }
                        ?.pubKey
                }
                Log.d("ReceiverHelpers", "Self pubkey loaded: ${cachedSelfChatPubKey != null}")
            } catch (e: Exception) {
                Log.e("ReceiverHelpers", "Failed to load self pubkey: ${e.message}")
            }
        }

        return cachedSelfChatPubKey?.contentEquals(senderPubKey) ?: false
    }

    /**
     * Synchronous version of isSenderSelf for use in legacy callbacks.
     * Uses cached value only - returns false if not cached.
     *
     * ⚠️ DEPRECATED: Prefer the suspend version isSenderSelf()
     * This method will return incorrect results until preloadSelfPubKey() is called.
     */
    fun isSenderSelfSync(senderPubKey: ByteArray?): Boolean {
        if (senderPubKey == null) return false
        return cachedSelfChatPubKey?.contentEquals(senderPubKey) ?: false
    }
    
    /**
     * Preload the self pubkey cache
     * Call this after login/setup to ensure cache is ready
     */
    suspend fun preloadSelfPubKey() {
        if (cachedSelfChatPubKey != null) return
        
        try {
            repository.getAllChats().collect { chats ->
                chats.firstOrNull { it.name == "<self>" }?.let {
                    cachedSelfChatPubKey = it.pubKey
                    android.util.Log.d("ReceiverHelpers", "Self pubkey preloaded")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ReceiverHelpers", "Failed to preload self pubkey: ${e.message}")
        }
    }
    
    /**
     * Clear the cached self pubkey
     * Call on logout
     */
    fun clearSelfPubKeyCache() {
        cachedSelfChatPubKey = null
    }

    /**
     * Clear cached self chat ID (call after user switches)
     */
    fun clearSelfChatCache() {
        cachedSelfChatPubKey = null
    }

    /**
     * Fetch or create a sender, updating dmToken and nickname if exists
     */
    suspend fun upsertSender(
        pubKey: ByteArray,
        codename: String,
        nickname: String? = null,
        dmToken: Int,
        color: Int
    ): MessageSenderModel {
        val existing = repository.getSenderByPubKey(pubKey)

        return if (existing != null) {
            val updated = existing.copy(
                dmToken = dmToken,
                nickname = nickname?.takeIf { it.isNotEmpty() } ?: existing.nickname
            )
            repository.updateSender(updated)
            updated
        } else {
            val sender = MessageSenderModel(
                pubkey = pubKey,
                codename = codename,
                nickname = nickname,
                dmToken = dmToken,
                color = color
            )
            repository.insertSender(sender)
            sender
        }
    }

    /**
     * Insert a new text message
     */
    suspend fun insertMessage(
        chat: ChatModel,
        sender: MessageSenderModel?,
        text: String,
        messageId: String,
        id: Long,
        senderPubKey: ByteArray?,
        replyTo: String? = null,
        timestamp: Long? = null,
        status: Long
    ): ChatMessageModel {
        android.util.Log.d("ReceiverHelpers", "insertMessage: id=$id, msgId=$messageId, senderId=${sender?.id}")
        
        // Check if message is incoming (not from self)
        val isIncoming = senderPubKey == null || !isSenderSelf(senderPubKey)
        
        android.util.Log.d("ReceiverHelpers", "isIncoming: $isIncoming")

        val msg = if (timestamp != null) {
            ChatMessageModel(
                id = id,
                externalId = messageId,
                message = text.trim(),
                timestamp = Date(timestamp / 1_000_000), // Convert nanoseconds to milliseconds
                isIncoming = isIncoming,
                isRead = !isIncoming,
                status = MessageStatus.fromValue(status.toInt()).value,
                senderId = sender?.id,
                chatId = chat.id,
                replyTo = replyTo,
                isPlain = !containsHTML(text)
            )
        } else {
            ChatMessageModel(
                id = id,
                externalId = messageId,
                message = text,
                timestamp = Date(),
                isIncoming = isIncoming,
                isRead = !isIncoming,
                status = MessageStatus.fromValue(status.toInt()).value,
                senderId = sender?.id,
                chatId = chat.id,
                replyTo = replyTo,
                isPlain = !containsHTML(text)
            )
        }

        android.util.Log.d("ReceiverHelpers", "Inserting message to repository...")
        repository.insertMessage(msg)
        android.util.Log.d("ReceiverHelpers", "Message inserted successfully")

        // Update unread count if incoming and after joinedAt
        if (isIncoming && msg.timestamp.after(chat.joinedAt)) {
            repository.incrementUnreadCount(chat.id)
        }

        return msg
    }

    /**
     * Persist an incoming message: upserts sender and inserts message
     */
    suspend fun persistIncomingMessage(
        chat: ChatModel,
        text: String,
        messageId: String,
        senderPubKey: ByteArray?,
        senderCodename: String?,
        nickname: String? = null,
        dmToken: Int,
        color: Int,
        replyTo: String? = null,
        timestamp: Long? = null,
        status: Long
    ): ChatMessageModel {
        android.util.Log.d("ReceiverHelpers", "persistIncomingMessage: chat=${chat.id}, msgId=$messageId, sender=$senderCodename")
        
        val sender = senderCodename?.let { codename ->
            senderPubKey?.let { pubKey ->
                android.util.Log.d("ReceiverHelpers", "Upserting sender: $codename")
                upsertSender(
                    pubKey = pubKey,
                    codename = codename,
                    nickname = nickname,
                    dmToken = dmToken,
                    color = color
                )
            }
        }
        
        android.util.Log.d("ReceiverHelpers", "Sender upserted: ${sender?.id}")

        val id = System.currentTimeMillis() // Generate unique ID
        android.util.Log.d("ReceiverHelpers", "Inserting message with id: $id")
        
        return insertMessage(
            chat = chat,
            sender = sender,
            text = text,
            messageId = messageId,
            id = id,
            senderPubKey = senderPubKey,
            replyTo = replyTo,
            timestamp = timestamp,
            status = status
        )
    }

    /**
     * Check if text contains HTML
     */
    private fun containsHTML(text: String): Boolean {
        return text.contains("<", ignoreCase = true) && 
               text.contains(">", ignoreCase = true)
    }
}
