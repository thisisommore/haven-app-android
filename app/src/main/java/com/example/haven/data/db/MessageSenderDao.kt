package com.example.haven.data.db

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
    fun getAll(): Flow<List<MessageSenderEntity>>

    @Query("SELECT * FROM messageSenders WHERE id = :id")
    suspend fun getById(id: String): MessageSenderEntity?

    @Query("SELECT * FROM messageSenders WHERE pubkey = :pubKey")
    suspend fun getByPubKey(pubKey: ByteArray): MessageSenderEntity?

    @Query("SELECT * FROM messageSenders WHERE codename = :codename")
    suspend fun getByCodename(codename: String): MessageSenderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sender: MessageSenderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(senders: List<MessageSenderEntity>)

    @Update
    suspend fun update(sender: MessageSenderEntity)

    @Delete
    suspend fun delete(sender: MessageSenderEntity)

    @Query("DELETE FROM messageSenders")
    suspend fun deleteAll()

    @Query("UPDATE messageSenders SET nickname = :nickname WHERE id = :id")
    suspend fun updateNickname(id: String, nickname: String?)
}
