package com.example.haven.xxdk.callbacks

import android.content.Context
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.DatabaseModule
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.data.model.MessageStatus
import bindings.Bindings
import com.example.haven.xxdk.Parser
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
     */
    suspend fun isSenderSelf(senderPubKey: ByteArray?): Boolean {
        if (senderPubKey == null) return false

        if (cachedSelfChatPubKey == null) {
            val selfChat = repository.getAllChats()
                .collect { chats ->
                    chats.firstOrNull { it.name == "<self>" }?.let {
                        cachedSelfChatPubKey = it.pubKey
                    }
                }
        }

        return cachedSelfChatPubKey?.contentEquals(senderPubKey) ?: false
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
        val isIncoming = !isSenderSelf(senderPubKey)

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

        repository.insertMessage(msg)

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
        val sender = senderCodename?.let { codename ->
            senderPubKey?.let { pubKey ->
                upsertSender(
                    pubKey = pubKey,
                    codename = codename,
                    nickname = nickname,
                    dmToken = dmToken,
                    color = color
                )
            }
        }

        val id = System.currentTimeMillis() // Generate unique ID
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
