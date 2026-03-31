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
    fun getAll(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE id = :id")
    suspend fun getById(id: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE channelId = :channelId")
    suspend fun getByChannelId(channelId: String): ChatEntity?

    @Query("SELECT * FROM chats WHERE pubKey = :pubKey")
    suspend fun getByPubKey(pubKey: ByteArray): ChatEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    @Update
    suspend fun update(chat: ChatEntity)

    @Delete
    suspend fun delete(chat: ChatEntity)

    @Query("DELETE FROM chats")
    suspend fun deleteAll()

    @Query("UPDATE chats SET unreadCount = unreadCount + 1 WHERE id = :chatId")
    suspend fun incrementUnreadCount(chatId: String)

    @Query("UPDATE chats SET unreadCount = 0 WHERE id = :chatId")
    suspend fun clearUnreadCount(chatId: String)

    @Query("SELECT * FROM chats WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchByName(query: String): Flow<List<ChatEntity>>
}
