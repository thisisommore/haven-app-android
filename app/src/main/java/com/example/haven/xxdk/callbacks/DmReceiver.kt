package com.example.haven.xxdk.callbacks

import android.content.Context
import android.util.Base64
import android.util.Log
import bindings.DMReceiver
import com.example.haven.data.model.ChatModel
import com.example.haven.data.DatabaseModule
import com.example.haven.data.model.MessageStatus
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.xxdk.MessageDecoding
import com.example.haven.xxdk.Parser
import com.example.haven.xxdk.ReceiverHelpers
import kotlinx.coroutines.runBlocking

/**
 * DMReceiver implementation for message processing
 * Mirrors iOS DMReceiver implementation
 */
class DmReceiver(private val context: Context) : DMReceiver {
    companion object {
        private const val TAG = "DmReceiver"
    }

    private val repository by lazy { DatabaseModule.provideRepository(context) }
    private val receiverHelpers by lazy { ReceiverHelpers.getInstance(context) }
    private var msgCnt: Long = 0

    override fun updateSentStatus(
        uuid: Long,
        messageID: ByteArray?,
        timestamp: Long,
        roundID: Long,
        status: Long
    ) {
        val parsedStatus = MessageStatus.fromValue(status.toInt())

        runBlocking {
            try {
                // Try to find message by UUID first
                var message = repository.getMessageById(uuid)

                // If not found by UUID, try by messageID
                if (message == null && messageID != null) {
                    val messageIdB64 = Base64.encodeToString(messageID, Base64.NO_WRAP)
                    message = repository.getMessageByExternalId(messageIdB64)
                }

                if (message != null) {
                    if (parsedStatus == MessageStatus.FAILED) {
                        repository.deleteMessage(message)
                    } else {
                        repository.updateMessageStatus(message.id, parsedStatus)
                    }
                    return@runBlocking
                }

                // Try reaction
                var reaction = repository.getReactionById(uuid)

                if (reaction == null && messageID != null) {
                    val messageIdB64 = Base64.encodeToString(messageID, Base64.NO_WRAP)
                    reaction = repository.getReactionByExternalId(messageIdB64)
                }

                reaction?.let {
                    if (parsedStatus == MessageStatus.FAILED) {
                        repository.deleteReaction(it)
                    } else {
                        repository.updateReactionStatus(it.id, parsedStatus)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "updateSentStatus db operation failed: ${e.message}")
            }
        }
    }

    override fun deleteMessage(messageID: ByteArray?, senderPubKey: ByteArray?): Boolean {
        Log.d(TAG, "Delete message: ${Base64.encodeToString(messageID, Base64.NO_WRAP)}, " +
                "${Base64.encodeToString(senderPubKey, Base64.NO_WRAP)}")
        return true
    }

    override fun getConversation(senderPubKey: ByteArray?): ByteArray {
        Log.d(TAG, "getConversation: ${Base64.encodeToString(senderPubKey, Base64.NO_WRAP)}")
        return "".toByteArray()
    }

    override fun getConversations(): ByteArray {
        Log.d(TAG, "getConversations")
        return "[]".toByteArray()
    }

    override fun receive(
        messageID: ByteArray,
        nickname: String,
        text: ByteArray,
        partnerKey: ByteArray,
        senderKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        roundId: Long,
        mType: Long,
        status: Long
    ): Long {
        val decodedMessage = MessageDecoding.decodeMessage(Base64.encodeToString(text, Base64.NO_WRAP))
        if (decodedMessage == null) {
            Log.e(TAG, "Failed to decode message in receive(), skipping")
            return 0L
        }

        val (codename, color) = try {
            receiverHelpers.parseIdentity(partnerKey, codeset)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse identity in receive(): ${e.message}")
            return 0L
        }

        return runBlocking {
            try {
                val m = persistIncoming(
                    message = decodedMessage,
                    codename = codename,
                    partnerKey = partnerKey,
                    senderKey = senderKey,
                    dmToken = dmToken,
                    messageId = messageID,
                    color = color,
                    timestamp = timestamp,
                    status = status
                )
                m.id
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist incoming message: ${e.message}")
                0L
            }
        }
    }

    override fun receiveReaction(
        messageID: ByteArray,
        reactionTo: ByteArray,
        nickname: String,
        reaction: String,
        partnerKey: ByteArray,
        senderKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        roundId: Long,
        status: Long
    ): Long {
        // Note: this should be a UUID in your database so
        // you can uniquely identify the message.
        msgCnt += 1
        return msgCnt
    }

    override fun receiveReply(
        messageID: ByteArray,
        reactionTo: ByteArray,
        nickname: String,
        text: String,
        partnerKey: ByteArray,
        senderKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        roundId: Long,
        status: Long
    ): Long {
        val decodedReply = MessageDecoding.decodeMessage(text)
        if (decodedReply == null) {
            Log.e(TAG, "Failed to decode reply, skipping")
            return 0L
        }

        val (codename, color) = try {
            receiverHelpers.parseIdentity(partnerKey, codeset)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse identity in receiveReply(): ${e.message}")
            return 0L
        }

        return runBlocking {
            try {
                val m = persistIncoming(
                    message = decodedReply,
                    codename = codename,
                    partnerKey = partnerKey,
                    senderKey = senderKey,
                    dmToken = dmToken,
                    messageId = messageID,
                    color = color,
                    timestamp = timestamp,
                    status = status
                )
                m.id
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist reply: ${e.message}")
                0L
            }
        }
    }

    override fun receiveText(
        messageID: ByteArray,
        nickname: String,
        text: String,
        partnerKey: ByteArray,
        senderKey: ByteArray,
        dmToken: Int,
        codeset: Long,
        timestamp: Long,
        roundId: Long,
        status: Long
    ): Long {
        val decodedText = MessageDecoding.decodeMessage(text)
        if (decodedText == null) {
            Log.e(TAG, "Failed to decode message, skipping")
            return 0L
        }

        val (codename, color) = try {
            receiverHelpers.parseIdentity(partnerKey, codeset)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse identity: ${e.message}")
            return 0L
        }

        return runBlocking {
            try {
                val m = persistIncoming(
                    message = decodedText,
                    codename = codename,
                    partnerKey = partnerKey,
                    senderKey = senderKey,
                    dmToken = dmToken,
                    messageId = messageID,
                    color = color,
                    timestamp = timestamp,
                    status = status
                )
                m.id
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist text message: ${e.message}")
                0L
            }
        }
    }

    private suspend fun persistIncoming(
        message: String,
        codename: String,
        partnerKey: ByteArray,
        senderKey: ByteArray,
        dmToken: Int,
        messageId: ByteArray,
        color: Int,
        timestamp: Long,
        status: Long
    ): com.example.haven.data.model.ChatMessageModel {

        val name = codename.trim().takeIf { it.isNotEmpty() } ?: "Unknown"

        val chat = fetchOrCreateDMChat(
            codename = name,
            pubKey = partnerKey,
            dmToken = dmToken,
            color = color
        )

        return receiverHelpers.persistIncomingMessage(
            chat = chat,
            text = message,
            messageId = Base64.encodeToString(messageId, Base64.NO_WRAP),
            senderPubKey = senderKey,
            senderCodename = name,
            dmToken = dmToken,
            color = color,
            timestamp = timestamp,
            status = status
        )
    }

    private suspend fun fetchOrCreateDMChat(
        codename: String,
        pubKey: ByteArray?,
        dmToken: Int?,
        color: Int
    ): ChatModel {
        if (pubKey != null) {
            val existingByKey = repository.getChatByPubKey(pubKey)
            if (existingByKey != null) {
                return existingByKey
            } else {
                if (dmToken == null) {
                    throw IllegalStateException("dmToken required")
                }
                val newChat = ChatModel(
                    pubKey = pubKey,
                    name = codename,
                    dmToken = dmToken,
                    color = color
                )
                repository.insertChat(newChat)
                return newChat
            }
        } else {
            // Fallback to codename-based lookup (may collide)
            val allChats = mutableListOf<ChatModel>()
            repository.getAllChats().collect { chats ->
                allChats.addAll(chats)
            }
            return allChats.firstOrNull { it.name == codename }
                ?: throw IllegalStateException("pubkey required")
        }
    }
}
