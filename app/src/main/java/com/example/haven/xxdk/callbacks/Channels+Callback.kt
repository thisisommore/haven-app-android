package com.example.haven.xxdk.callbacks

import android.content.Context
import android.util.Base64
import android.util.Log
import bindings.EventModel
import bindings.EventModelBuilder
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.DatabaseModule
import com.example.haven.data.model.ChannelMutedUserModel
import com.example.haven.data.model.MessageReactionModel
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.data.model.MessageStatus
import com.example.haven.xxdk.MessageDecoding

import com.example.haven.xxdk.callbacks.ReceiverHelpers
import kotlinx.coroutines.runBlocking
import java.util.Date

import java.util.UUID

/**
 * Channel EventModel implementation for processing channel messages
 * Mirrors iOS ChannelEventModelBuilder implementation
 */
class ChannelEventModelBuilder(private val context: Context) : EventModel, EventModelBuilder {
    companion object {
        private const val TAG = "ChannelEventModel"
    }

    private val repository by lazy { DatabaseModule.provideRepository(context) }
    private val receiverHelpers by lazy { ReceiverHelpers.getInstance(context) }

    override fun build(path: String?): EventModel {
        return this
    }

    override fun updateFromMessageID(fromMessageID: ByteArray, messageUpdateInfoJSON: ByteArray): Long {
        // Not used in iOS
        return 0L
    }

    override fun updateFromUUID(fromUUID: Long, messageUpdateInfoJSON: ByteArray) {
        runBlocking {
            try {
                val updateInfo = MessageUpdateInfo.fromJson(messageUpdateInfoJSON)

                // Try to find and update message
                val message = repository.getMessageById(fromUUID)
                if (message != null) {
                    if (updateInfo.statusSet && updateInfo.status != null) {
                        val newStatus = MessageStatus.fromValue(updateInfo.status.toInt())
                        if (newStatus == MessageStatus.FAILED) {
                            repository.deleteMessage(message)
                            return@runBlocking
                        }
                    }

                    var updatedMessage = message
                    if (updateInfo.messageIdSet && updateInfo.messageId != null) {
                        updatedMessage = updatedMessage.copy(externalId = updateInfo.messageId)
                    }
                    if (updateInfo.statusSet && updateInfo.status != null) {
                        updatedMessage = updatedMessage.copy(status = updateInfo.status.toInt())
                    }
                    repository.updateMessage(updatedMessage)
                    return@runBlocking
                }

                // Try reaction
                val reaction = repository.getReactionById(fromUUID)
                if (reaction != null) {
                    if (updateInfo.statusSet && updateInfo.status != null) {
                        val newStatus = MessageStatus.fromValue(updateInfo.status.toInt())
                        if (newStatus == MessageStatus.FAILED) {
                            repository.deleteReaction(reaction)
                            return@runBlocking
                        }
                    }

                    var updatedReaction = reaction
                    if (updateInfo.messageIdSet && updateInfo.messageId != null) {
                        updatedReaction = updatedReaction.copy(externalId = updateInfo.messageId)
                    }
                    if (updateInfo.statusSet && updateInfo.status != null) {
                        updatedReaction = updatedReaction.copy(status = updateInfo.status.toInt())
                    }
                    repository.updateReaction(updatedReaction)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update message/reaction: ${e.message}")
            }
        }
    }

    override fun joinChannel(channelId: String?) {
        // Not implemented in iOS
    }

    override fun leaveChannel(channelId: ByteArray?) {
        // Not implemented in iOS
    }

    override fun receiveMessage(
        channelID: ByteArray,
        messageID: ByteArray,
        nickname: String,
        text: String,
        pubKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        lease: Long,
        roundID: Long,
        messageType: Long,
        status: Long,
        hidden: Boolean
    ): Long {
        val messageIdB64 = Base64.encodeToString(messageID, Base64.NO_WRAP)

        runBlocking {
            if (repository.getMessageByExternalId(messageIdB64) != null) {
                return@runBlocking
            }
        }

        return try {
            val (codename, color) = receiverHelpers.parseIdentity(pubKey, codeset)
            val channelIdB64 = Base64.encodeToString(channelID, Base64.NO_WRAP)

            MessageDecoding.decodeMessage(text)?.let { decodedText ->
                persistMessage(
                    channelId = channelIdB64,
                    text = decodedText,
                    senderCodename = codename,
                    senderPubKey = pubKey,
                    messageIdB64 = messageIdB64,
                    timestamp = timestamp,
                    dmToken = dmToken,
                    color = color,
                    nickname = nickname,
                    status = status
                )
            } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Failed to receive message: ${e.message}")
            0L
        }
    }

    override fun receiveReaction(
        channelID: ByteArray,
        messageID: ByteArray,
        reactionTo: ByteArray,
        nickname: String,
        reaction: String,
        pubKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        lease: Long,
        roundID: Long,
        messageType: Long,
        status: Long,
        hidden: Boolean
    ): Long {
        val targetMessageIdB64 = Base64.encodeToString(reactionTo, Base64.NO_WRAP)
        val reactionMessageId = Base64.encodeToString(messageID, Base64.NO_WRAP)

        return runBlocking {
            try {
                val (codename, color) = receiverHelpers.parseIdentity(pubKey, codeset)

                // Check for existing reaction
                val existing = repository.getReactionByExternalId(reactionMessageId)
                if (existing != null) {
                    return@runBlocking existing.id
                }

                val sender = receiverHelpers.upsertSender(
                    pubKey = pubKey,
                    codename = codename,
                    nickname = nickname,
                    dmToken = dmToken,
                    color = color
                )

                val isSelfSender = receiverHelpers.isSenderSelf(pubKey)
                val reactionSenderId = if (isSelfSender) {
                    UUID(0L, 0L).toString()
                } else {
                    sender.id
                }

                // De-duplicate by message target + emoji + sender
                val newReaction = MessageReactionModel(
                    externalId = reactionMessageId,
                    targetMessageId = targetMessageIdB64,
                    emoji = reaction,
                    senderId = reactionSenderId,
                    status = status.toInt()
                )

                repository.insertReaction(newReaction)
                newReaction.id
            } catch (e: Exception) {
                Log.e(TAG, "Failed to store reaction: ${e.message}")
                0L
            }
        }
    }

    override fun receiveReply(
        channelID: ByteArray,
        messageID: ByteArray,
        reactionTo: ByteArray,
        nickname: String,
        text: String,
        pubKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        lease: Long,
        roundID: Long,
        messageType: Long,
        status: Long,
        hidden: Boolean
    ): Long {
        val messageIdB64 = Base64.encodeToString(messageID, Base64.NO_WRAP)

        runBlocking {
            if (repository.getMessageByExternalId(messageIdB64) != null) {
                return@runBlocking
            }
        }

        val (nick, color) = try {
            receiverHelpers.parseIdentity(pubKey, codeset)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse identity in receiveReply: ${e.message}")
            return 0L
        }

        return MessageDecoding.decodeMessage(text)?.let { decodedReply ->
            persistMessage(
                channelId = Base64.encodeToString(channelID, Base64.NO_WRAP),
                text = decodedReply,
                senderCodename = nick,
                senderPubKey = pubKey,
                messageIdB64 = messageIdB64,
                replyTo = Base64.encodeToString(reactionTo, Base64.NO_WRAP),
                timestamp = timestamp,
                dmToken = dmToken,
                color = color,
                nickname = nickname,
                status = status
            )
        } ?: 0L
    }

    override fun getMessage(messageID: ByteArray): ByteArray {
        val messageIdB64 = Base64.encodeToString(messageID, Base64.NO_WRAP)

        return runBlocking {
            try {
                // Try to find message
                val message = repository.getMessageByExternalId(messageIdB64)
                if (message != null && message.senderId != null) {
                    val sender = repository.getSenderById(message.senderId)
                    if (sender != null) {
                        val modelMsg = ModelMessage(
                            pubKey = sender.pubkey,
                            messageID = messageID
                        )
                        return@runBlocking modelMsg.toJson()
                    }
                }

                // Try reaction
                val reaction = repository.getReactionByExternalId(messageIdB64)
                if (reaction != null) {
                    val sender = repository.getSenderById(reaction.senderId)
                    if (sender != null) {
                        val modelMsg = ModelMessage(
                            pubKey = sender.pubkey,
                            messageID = messageID
                        )
                        return@runBlocking modelMsg.toJson()
                    }
                }

                // Return empty JSON instead of throwing
                Log.e(TAG, "Message not found for getMessage: $messageIdB64")
                """{"error":"Message not found"}""".toByteArray()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get message: ${e.message}")
                """{"error":"${e.message}"}""".toByteArray()
            }
        }
    }

    override fun deleteMessage(messageID: ByteArray) {
        val messageIdB64 = Base64.encodeToString(messageID, Base64.NO_WRAP)

        runBlocking {
            try {
                // Try to find and delete message
                val message = repository.getMessageByExternalId(messageIdB64)
                if (message != null) {
                    repository.deleteMessage(message)
                    return@runBlocking
                }

                // Try reaction
                val reaction = repository.getReactionByExternalId(messageIdB64)
                if (reaction != null) {
                    repository.deleteReaction(reaction)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete message/reaction: ${e.message}")
            }
        }
    }

    override fun muteUser(channelID: ByteArray?, pubkey: ByteArray?, unmute: Boolean) {
        if (channelID == null || pubkey == null) {
            Log.e(TAG, "muteUser called with null channelID or pubkey")
            return
        }

        val channelIdB64 = Base64.encodeToString(channelID, Base64.NO_WRAP)

        runBlocking {
            try {
                // Verify channel exists
                val chat = repository.getChatByChannelId(channelIdB64)
                if (chat == null) {
                    Log.e(TAG, "muteUser: Channel not found: $channelIdB64")
                    return@runBlocking
                }

                val existing = repository.getMutedUserByChannelIdAndPubkey(channelIdB64, pubkey)

                if (unmute) {
                    if (existing != null) {
                        repository.deleteMutedUser(existing)
                        Log.d(TAG, "User unmuted in channel: $channelIdB64")
                    }
                } else {
                    if (existing == null) {
                        val mutedUser = ChannelMutedUserModel(
                            channelId = channelIdB64,
                            pubkey = pubkey
                        )
                        repository.insertMutedUser(mutedUser)
                        Log.d(TAG, "User muted in channel: $channelIdB64")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist mute state: ${e.message}")
            }
        }
    }

    private suspend fun fetchChannel(channelId: String): ChatModel? {
        return repository.getChatByChannelId(channelId)
    }

    private fun persistMessage(
        channelId: String,
        text: String,
        senderCodename: String?,
        senderPubKey: ByteArray?,
        messageIdB64: String?,
        replyTo: String? = null,
        timestamp: Long,
        dmToken: Int?,
        color: Int,
        nickname: String?,
        status: Long
    ): Long {
        if (messageIdB64.isNullOrEmpty()) {
            Log.e(TAG, "No message id provided")
            return 0L
        }

        return runBlocking {
            try {
                val chat = fetchChannel(channelId)
                if (chat == null) {
                    Log.e(TAG, "Channel not found: $channelId")
                    return@runBlocking 0L
                }

                val msg = receiverHelpers.persistIncomingMessage(
                    chat = chat,
                    text = text,
                    messageId = messageIdB64,
                    senderPubKey = senderPubKey,
                    senderCodename = senderCodename,
                    nickname = nickname,
                    dmToken = dmToken ?: 0,
                    color = color,
                    replyTo = replyTo,
                    timestamp = timestamp,
                    status = status
                )

                msg.id
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist message: ${e.message}")
                0L
            }
        }
    }

}
