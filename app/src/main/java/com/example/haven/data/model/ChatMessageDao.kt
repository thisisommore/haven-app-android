package com.example.haven.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chatMessages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getByChatId(chatId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chatMessages WHERE chatId = :chatId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentByChatId(chatId: String, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM chatMessages WHERE id = :id")
    suspend fun getById(id: Long): ChatMessageEntity?

    @Query("SELECT * FROM chatMessages WHERE externalId = :externalId")
    suspend fun getByExternalId(externalId: String): ChatMessageEntity?

    @Query("SELECT * FROM chatMessages WHERE senderId = :senderId ORDER BY timestamp DESC")
    fun getBySenderId(senderId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Update
    suspend fun update(message: ChatMessageEntity)

    @Delete
    suspend fun delete(message: ChatMessageEntity)

    @Query("DELETE FROM chatMessages WHERE chatId = :chatId")
    suspend fun deleteByChatId(chatId: String)

    @Query("DELETE FROM chatMessages")
    suspend fun deleteAll()

    @Query("UPDATE chatMessages SET isRead = 1 WHERE chatId = :chatId")
    suspend fun markAllAsRead(chatId: String)

    @Query("UPDATE chatMessages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int)

    @Query("UPDATE chatMessages SET status = :status WHERE externalId = :externalId")
    suspend fun updateStatusByExternalId(externalId: String, status: Int)

    @Query("SELECT COUNT(*) FROM chatMessages WHERE chatId = :chatId AND isRead = 0 AND isIncoming = 1")
    suspend fun getUnreadCount(chatId: String): Int

    @Query("SELECT * FROM chatMessages WHERE chatId = :chatId AND message LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchInChat(chatId: String, query: String): Flow<List<ChatMessageEntity>>
}
