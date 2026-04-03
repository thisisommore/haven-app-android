package com.example.haven.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * DAO for ChannelMutedUserModel
 * Equivalent to iOS ChannelMutedUserModel queries
 */
@Dao
interface ChannelMutedUserDao {
    @Query("SELECT * FROM channelMutedUsers WHERE channelId = :channelId")
    fun getByChannelId(channelId: String): Flow<List<ChannelMutedUserModel>>

    @Query("SELECT * FROM channelMutedUsers WHERE channelId = :channelId")
    suspend fun getByChannelIdSync(channelId: String): List<ChannelMutedUserModel>

    @Query("SELECT * FROM channelMutedUsers WHERE channelId = :channelId AND pubkey = :pubkey LIMIT 1")
    suspend fun getByChannelIdAndPubkey(channelId: String, pubkey: ByteArray): ChannelMutedUserModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mutedUser: ChannelMutedUserModel)

    @Delete
    suspend fun delete(mutedUser: ChannelMutedUserModel)

    @Query("DELETE FROM channelMutedUsers WHERE channelId = :channelId")
    suspend fun deleteByChannelId(channelId: String)

    @Query("DELETE FROM channelMutedUsers WHERE channelId = :channelId AND pubkey = :pubkey")
    suspend fun deleteByChannelIdAndPubkey(channelId: String, pubkey: ByteArray)
}
