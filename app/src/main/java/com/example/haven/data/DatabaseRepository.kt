package com.example.haven.data

import android.content.Context
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.model.MessageReactionModel
import com.example.haven.data.model.MessageStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository class providing database access similar to iOS pattern
 */
class DatabaseRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val chatDao = db.chatDao()
    private val messageSenderDao = db.messageSenderDao()
    private val chatMessageDao = db.chatMessageDao()
    private val messageReactionDao = db.messageReactionDao()

    // Chats
    fun getAllChats(): Flow<List<ChatModel>> = chatDao.getAll()
    suspend fun getChatById(id: String): ChatModel? = chatDao.getById(id)
    suspend fun getChatByChannelId(channelId: String): ChatModel? = chatDao.getByChannelId(channelId)
    suspend fun getChatByPubKey(pubKey: ByteArray): ChatModel? = chatDao.getByPubKey(pubKey)
    suspend fun insertChat(chat: ChatModel) = chatDao.insert(chat)
    suspend fun updateChat(chat: ChatModel) = chatDao.update(chat)
    suspend fun deleteChat(chat: ChatModel) = chatDao.delete(chat)
    suspend fun clearAllChats() = chatDao.deleteAll()
    suspend fun incrementUnreadCount(chatId: String) = chatDao.incrementUnreadCount(chatId)
    suspend fun clearUnreadCount(chatId: String) = chatDao.clearUnreadCount(chatId)
    fun searchChatsByName(query: String): Flow<List<ChatModel>> = chatDao.searchByName(query)

    // Message Senders
    fun getAllSenders(): Flow<List<MessageSenderModel>> = messageSenderDao.getAll()
    suspend fun getSenderById(id: String): MessageSenderModel? = messageSenderDao.getById(id)
    suspend fun getSenderByPubKey(pubKey: ByteArray): MessageSenderModel? = messageSenderDao.getByPubKey(pubKey)
    suspend fun getSenderByCodename(codename: String): MessageSenderModel? = messageSenderDao.getByCodename(codename)
    suspend fun insertSender(sender: MessageSenderModel) = messageSenderDao.insert(sender)
    suspend fun updateSender(sender: MessageSenderModel) = messageSenderDao.update(sender)
    suspend fun updateSenderNickname(id: String, nickname: String?) = messageSenderDao.updateNickname(id, nickname)
    suspend fun deleteSender(sender: MessageSenderModel) = messageSenderDao.delete(sender)
    suspend fun clearAllSenders() = messageSenderDao.deleteAll()

    // Chat Messages
    fun getMessagesByChatId(chatId: String): Flow<List<ChatMessageModel>> = chatMessageDao.getByChatId(chatId)
    suspend fun getRecentMessages(chatId: String, limit: Int): List<ChatMessageModel> = 
        chatMessageDao.getRecentByChatId(chatId, limit)
    suspend fun getMessageById(id: Long): ChatMessageModel? = chatMessageDao.getById(id)
    suspend fun getMessageByExternalId(externalId: String): ChatMessageModel? = 
        chatMessageDao.getByExternalId(externalId)
    suspend fun insertMessage(message: ChatMessageModel) = chatMessageDao.insert(message)
    suspend fun updateMessage(message: ChatMessageModel) = chatMessageDao.update(message)
    suspend fun deleteMessage(message: ChatMessageModel) = chatMessageDao.delete(message)
    suspend fun deleteMessagesByChatId(chatId: String) = chatMessageDao.deleteByChatId(chatId)
    suspend fun markAllMessagesAsRead(chatId: String) = chatMessageDao.markAllAsRead(chatId)
    suspend fun updateMessageStatus(id: Long, status: MessageStatus) = 
        chatMessageDao.updateStatus(id, status.value)
    suspend fun updateMessageStatusByExternalId(externalId: String, status: MessageStatus) = 
        chatMessageDao.updateStatusByExternalId(externalId, status.value)
    suspend fun getUnreadMessageCount(chatId: String): Int = chatMessageDao.getUnreadCount(chatId)

    // Message Reactions
    fun getReactionsByTargetMessageId(targetMessageId: String): Flow<List<MessageReactionModel>> = 
        messageReactionDao.getByTargetMessageId(targetMessageId)
    suspend fun getReactionById(id: Long): MessageReactionModel? = messageReactionDao.getById(id)
    suspend fun getReactionByExternalId(externalId: String): MessageReactionModel? = 
        messageReactionDao.getByExternalId(externalId)
    suspend fun insertReaction(reaction: MessageReactionModel) = messageReactionDao.insert(reaction)
    suspend fun updateReaction(reaction: MessageReactionModel) = messageReactionDao.update(reaction)
    suspend fun deleteReaction(reaction: MessageReactionModel) = messageReactionDao.delete(reaction)
    suspend fun deleteReactionsByTargetMessageId(targetMessageId: String) = 
        messageReactionDao.deleteByTargetMessageId(targetMessageId)
    suspend fun updateReactionStatus(id: Long, status: MessageStatus) = 
        messageReactionDao.updateStatus(id, status.value)
    suspend fun updateReactionStatusByExternalId(externalId: String, status: MessageStatus) = 
        messageReactionDao.updateStatusByExternalId(externalId, status.value)

    // Clear all data (for logout)
    suspend fun clearAllData() {
        chatDao.deleteAll()
        messageSenderDao.deleteAll()
        chatMessageDao.deleteAll()
        messageReactionDao.deleteAll()
    }
}
