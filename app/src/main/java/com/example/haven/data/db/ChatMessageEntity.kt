package com.example.haven.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Equivalent to iOS ChatMessageModel
 * Table: chatMessages
 */
@Entity(
    tableName = "chatMessages",
    foreignKeys = [
        ForeignKey(
            entity = MessageSenderEntity::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["externalId"], unique = true),
        Index(value = ["chatId"]),
        Index(value = ["senderId"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val externalId: String,
    val message: String,
    val timestamp: Date,
    val isIncoming: Boolean,
    val isRead: Boolean = false,
    val status: Int = MessageStatus.UNSENT.value,
    val senderId: String? = null,
    val chatId: String,
    val replyTo: String? = null,
    val isPlain: Boolean = false
) {
    fun getMessageStatus(): MessageStatus = MessageStatus.fromValue(status)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChatMessageEntity
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
