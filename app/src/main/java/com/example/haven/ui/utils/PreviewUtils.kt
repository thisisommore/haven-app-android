package com.example.haven.ui.utils

import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.MessageReactionModel
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.data.model.MessageStatus
import java.util.Date
import java.util.UUID

/**
 * Preview utilities for Compose previews
 * Mirrors iOS PreviewUtils.swift
 */
object PreviewUtils {

    /**
     * Create a sample chat for previews
     */
    fun sampleChat(
        id: String = "chat-1",
        name: String = "Test Chat",
        isChannel: Boolean = false
    ): ChatModel {
        return ChatModel(
            id = id,
            name = name,
            channelId = if (isChannel) "channel-1" else null,
            pubKey = if (!isChannel) byteArrayOf(1, 2, 3) else null,
            dmToken = if (!isChannel) 12345 else null,
            unreadCount = 2
        )
    }

    /**
     * Create a sample message for previews
     */
    fun sampleMessage(
        id: Long = 1,
        text: String = "Hello, World!",
        isIncoming: Boolean = false,
        timestamp: Date = Date(),
        status: MessageStatus = MessageStatus.SENT,
        replyTo: String? = null
    ): ChatMessageModel {
        return ChatMessageModel(
            id = id,
            externalId = "msg-$id",
            message = text,
            timestamp = timestamp,
            isIncoming = isIncoming,
            isRead = true,
            status = status.value,
            senderId = if (isIncoming) "sender-1" else null,
            chatId = "chat-1",
            replyTo = replyTo,
            isPlain = false
        )
    }

    /**
     * Create a sample sender for previews
     */
    fun sampleSender(
        id: String = "sender-1",
        codename: String = "testuser",
        nickname: String? = "Test User"
    ): MessageSenderModel {
        return MessageSenderModel(
            id = id,
            pubkey = byteArrayOf(1, 2, 3),
            codename = codename,
            nickname = nickname,
            color = 0xE97451
        )
    }

    /**
     * Create a sample reaction for previews
     */
    fun sampleReaction(
        id: Long = 1,
        emoji: String = "❤️",
        targetMessageId: String = "msg-1",
        isFromMe: Boolean = false
    ): MessageReactionModel {
        return MessageReactionModel(
            id = id,
            externalId = "react-$id",
            targetMessageId = targetMessageId,
            emoji = emoji,
            timestamp = Date(),
            senderId = if (isFromMe) "self" else "sender-1",
            status = MessageStatus.SENT.value
        )
    }

    /**
     * Sample message list for chat previews
     */
    fun sampleMessages(): List<ChatMessageModel> {
        return listOf(
            sampleMessage(
                id = 1,
                text = "Hey there! How are you doing?",
                isIncoming = true,
                timestamp = Date(System.currentTimeMillis() - 3600000)
            ),
            sampleMessage(
                id = 2,
                text = "I'm doing great, thanks for asking!",
                isIncoming = false,
                timestamp = Date(System.currentTimeMillis() - 3000000)
            ),
            sampleMessage(
                id = 3,
                text = "That's wonderful to hear! Want to grab coffee later?",
                isIncoming = true,
                timestamp = Date(System.currentTimeMillis() - 2400000)
            ),
            sampleMessage(
                id = 4,
                text = "Sure, I'd love to! How about 3pm?",
                isIncoming = false,
                timestamp = Date(System.currentTimeMillis() - 1800000)
            ),
            sampleMessage(
                id = 5,
                text = "Perfect, see you then! ☕",
                isIncoming = true,
                timestamp = Date(System.currentTimeMillis() - 600000)
            )
        )
    }

    /**
     * Sample chat list for home screen previews
     */
    fun sampleChats(): List<ChatModel> {
        return listOf(
            sampleChat(
                id = "chat-1",
                name = "Alice",
                isChannel = false
            ),
            sampleChat(
                id = "chat-2",
                name = "Bob",
                isChannel = false
            ),
            sampleChat(
                id = "chat-3",
                name = "General Channel",
                isChannel = true
            ),
            sampleChat(
                id = "chat-4",
                name = "Random",
                isChannel = true
            )
        )
    }

    /**
     * Sample reactions list for message previews
     */
    fun sampleReactions(): List<MessageReactionModel> {
        return listOf(
            sampleReaction(id = 1, emoji = "❤️", targetMessageId = "msg-1"),
            sampleReaction(id = 2, emoji = "👍", targetMessageId = "msg-1", isFromMe = true),
            sampleReaction(id = 3, emoji = "🔥", targetMessageId = "msg-1")
        )
    }

    /**
     * HTML formatted message for testing HTML rendering
     */
    fun sampleHtmlMessage(): String {
        return "<b>Bold</b> and <i>italic</i> text with <a href=\"https://example.com\">a link</a>"
    }
}
