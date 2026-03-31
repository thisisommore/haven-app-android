package com.example.haven.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Equivalent to iOS MessageReactionModel
 * Table: messageReactions
 */
@Entity(
    tableName = "messageReactions",
    foreignKeys = [
        ForeignKey(
            entity = MessageSenderModel::class,
            parentColumns = ["id"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["externalId"], unique = true),
        Index(value = ["senderId"]),
        Index(value = ["targetMessageId"])
    ]
)
data class MessageReactionModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val externalId: String,
    val targetMessageId: String,
    val emoji: String,
    val timestamp: Date = Date(),
    val senderId: String,
    val status: Int = MessageStatus.UNSENT.value
) {
    fun getMessageStatus(): MessageStatus = MessageStatus.fromValue(status)

    val isMe: Boolean
        get() = senderId == SELF_ID

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MessageReactionModel
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        private val SELF_ID = java.util.UUID(0L, 0L).toString()
    }
}
