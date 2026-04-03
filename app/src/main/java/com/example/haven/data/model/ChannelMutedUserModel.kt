package com.example.haven.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Equivalent to iOS ChannelMutedUserModel
 * Table: channelMutedUsers
 * Stores muted users per channel
 */
@Entity(
    tableName = "channelMutedUsers",
    indices = [
        Index(value = ["channelId", "pubkey"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = ChatModel::class,
            parentColumns = ["channelId"],
            childColumns = ["channelId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChannelMutedUserModel(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val channelId: String,
    val pubkey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChannelMutedUserModel
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
