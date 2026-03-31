package com.example.haven.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats ORDER BY joinedAt DESC")
    fun getAll(): Flow<List<ChatModel>>

    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getById(id: String): ChatModel?

    @Query("SELECT * FROM chats WHERE channelId = :channelId")
    suspend fun getByChannelId(channelId: String): ChatModel?

    @Query("SELECT * FROM chats WHERE pubKey = :pubKey")
    suspend fun getByPubKey(pubKey: ByteArray): ChatModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatModel>)

    @Update
    suspend fun update(chat: ChatModel)

    @Delete
    suspend fun delete(chat: ChatModel)

    @Query("DELETE FROM chats")
    suspend fun deleteAll()

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE id = :chatId")
    suspend fun incrementUnreadCount(chatId: String)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun clearUnreadCount(chatId: String)

    @Query("SELECT * FROM chats WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<ChatModel>>
}
