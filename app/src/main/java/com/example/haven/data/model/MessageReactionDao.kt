package com.example.haven.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageReactionDao {
    @Query("SELECT * FROM messageReactions WHERE targetMessageId = :targetMessageId ORDER BY timestamp ASC")
    fun getByTargetMessageId(targetMessageId: String): Flow<List<MessageReactionModel>>

    @Query("""
        SELECT r.* FROM messageReactions r
        INNER JOIN chatMessages m ON r.targetMessageId = m.externalId
        WHERE m.chatId = :chatId
        ORDER BY r.timestamp ASC
    """)
    fun getByChatId(chatId: String): Flow<List<MessageReactionModel>>

    @Query("SELECT * FROM messageReactions WHERE id = :id")
    suspend fun getById(id: Long): MessageReactionModel?

    @Query("SELECT * FROM messageReactions WHERE externalId = :externalId")
    suspend fun getByExternalId(externalId: String): MessageReactionModel?

    @Query("SELECT * FROM messageReactions WHERE senderId = :senderId ORDER BY timestamp DESC")
    fun getBySenderId(senderId: String): Flow<List<MessageReactionModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reaction: MessageReactionModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reactions: List<MessageReactionModel>)

    @Update
    suspend fun update(reaction: MessageReactionModel)

    @Delete
    suspend fun delete(reaction: MessageReactionModel)

    @Query("DELETE FROM messageReactions WHERE targetMessageId = :targetMessageId")
    suspend fun deleteByTargetMessageId(targetMessageId: String)

    @Query("DELETE FROM messageReactions")
    suspend fun deleteAll()

    @Query("UPDATE messageReactions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: Int)

    @Query("UPDATE messageReactions SET status = :status WHERE externalId = :externalId")
    suspend fun updateStatusByExternalId(externalId: String, status: Int)
}
