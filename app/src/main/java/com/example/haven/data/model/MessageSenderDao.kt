package com.example.haven.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageSenderDao {
    @Query("SELECT * FROM messageSenders")
    fun getAll(): Flow<List<MessageSenderModel>>

    @Query("SELECT * FROM messageSenders WHERE id = :id")
    suspend fun getById(id: String): MessageSenderModel?

    @Query("SELECT * FROM messageSenders WHERE pubkey = :pubKey")
    suspend fun getByPubKey(pubKey: ByteArray): MessageSenderModel?

    @Query("SELECT * FROM messageSenders WHERE codename = :codename")
    suspend fun getByCodename(codename: String): MessageSenderModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sender: MessageSenderModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(senders: List<MessageSenderModel>)

    @Update
    suspend fun update(sender: MessageSenderModel)

    @Delete
    suspend fun delete(sender: MessageSenderModel)

    @Query("DELETE FROM messageSenders")
    suspend fun deleteAll()

    @Query("UPDATE messageSenders SET nickname = :nickname WHERE id = :id")
    suspend fun updateNickname(id: String, nickname: String?)
}
