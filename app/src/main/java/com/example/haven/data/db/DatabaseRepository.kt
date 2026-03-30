package com.example.haven.data.db

import android.content.Context
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
    fun getAllChats(): Flow<List<ChatEntity>> = chatDao.getAll()
    suspend fun getChatById(id: String): ChatEntity? = chatDao.getById(id)
    suspend fun getChatByChannelId(channelId: String): ChatEntity? = chatDao.getByChannelId(channelId)
    suspend fun getChatByPubKey(pubKey: ByteArray): ChatEntity? = chatDao.getByPubKey(pubKey)
    suspend fun insertChat(chat: ChatEntity) = chatDao.insert(chat)
    suspend fun updateChat(chat: ChatEntity) = chatDao.update(chat)
    suspend fun deleteChat(chat: ChatEntity) = chatDao.delete(chat)
    suspend fun clearAllChats() = chatDao.deleteAll()
    suspend fun incrementUnreadCount(chatId: String) = chatDao.incrementUnreadCount(chatId)
    suspend fun clearUnreadCount(chatId: String) = chatDao.clearUnreadCount(chatId)
    fun searchChatsByName(query: String): Flow<List<ChatEntity>> = chatDao.searchByName(query)

    // Message Senders
    fun getAllSenders(): Flow<List<MessageSenderEntity>> = messageSenderDao.getAll()
    suspend fun getSenderById(id: String): MessageSenderEntity? = messageSenderDao.getById(id)
    suspend fun getSenderByPubKey(pubKey: ByteArray): MessageSenderEntity? = messageSenderDao.getByPubKey(pubKey)
    suspend fun getSenderByCodename(codename: String): MessageSenderEntity? = messageSenderDao.getByCodename(codename)
    suspend fun insertSender(sender: MessageSenderEntity) = messageSenderDao.insert(sender)
    suspend fun updateSender(sender: MessageSenderEntity) = messageSenderDao.update(sender)
    suspend fun updateSenderNickname(id: String, nickname: String?) = messageSenderDao.updateNickname(id, nickname)
    suspend fun deleteSender(sender: MessageSenderEntity) = messageSenderDao.delete(sender)
    suspend fun clearAllSenders() = messageSenderDao.deleteAll()

    // Chat Messages
    fun getMessagesByChatId(chatId: String): Flow<List<ChatMessageEntity>> = chatMessageDao.getByChatId(chatId)
    suspend fun getRecentMessages(chatId: String, limit: Int): List<ChatMessageEntity> = 
        chatMessageDao.getRecentByChatId(chatId, limit)
    suspend fun getMessageById(id: Long): ChatMessageEntity? = chatMessageDao.getById(id)
    suspend fun getMessageByExternalId(externalId: String): ChatMessageEntity? = 
        chatMessageDao.getByExternalId(externalId)
    suspend fun insertMessage(message: ChatMessageEntity) = chatMessageDao.insert(message)
    suspend fun updateMessage(message: ChatMessageEntity) = chatMessageDao.update(message)
    suspend fun deleteMessage(message: ChatMessageEntity) = chatMessageDao.delete(message)
    suspend fun deleteMessagesByChatId(chatId: String) = chatMessageDao.deleteByChatId(chatId)
    suspend fun markAllMessagesAsRead(chatId: String) = chatMessageDao.markAllAsRead(chatId)
    suspend fun updateMessageStatus(id: Long, status: MessageStatus) = 
        chatMessageDao.updateStatus(id, status.value)
    suspend fun updateMessageStatusByExternalId(externalId: String, status: MessageStatus) = 
        chatMessageDao.updateStatusByExternalId(externalId, status.value)
    suspend fun getUnreadMessageCount(chatId: String): Int = chatMessageDao.getUnreadCount(chatId)

    // Message Reactions
    fun getReactionsByTargetMessageId(targetMessageId: String): Flow<List<MessageReactionEntity>> = 
        messageReactionDao.getByTargetMessageId(targetMessageId)
    suspend fun getReactionById(id: Long): MessageReactionEntity? = messageReactionDao.getById(id)
    suspend fun getReactionByExternalId(externalId: String): MessageReactionEntity? = 
        messageReactionDao.getByExternalId(externalId)
    suspend fun insertReaction(reaction: MessageReactionEntity) = messageReactionDao.insert(reaction)
    suspend fun updateReaction(reaction: MessageReactionEntity) = messageReactionDao.update(reaction)
    suspend fun deleteReaction(reaction: MessageReactionEntity) = messageReactionDao.delete(reaction)
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
