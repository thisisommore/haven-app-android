package com.example.haven.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Equivalent to iOS ChatModel
 * Table: chats
 */
@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["channelId"], unique = true),
        Index(value = ["pubKey"], unique = true)
    ]
)
data class ChatModel(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val joinedAt: Date = Date(),
    val unreadCount: Int = 0,
    // Channel only
    val channelId: String? = null,
    val channelDescription: String? = null,
    val isSecret: Boolean = false,
    val isAdmin: Boolean = false,
    val color: Int = 0xE97451, // burnt sienna
    // DM only
    val pubKey: ByteArray? = null,
    val dmToken: Int? = null
) {
    val isChannel: Boolean
        get() = name != "<self>" && dmToken == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChatModel
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
