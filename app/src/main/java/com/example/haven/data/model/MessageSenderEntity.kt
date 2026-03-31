package com.example.haven.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Equivalent to iOS MessageSenderModel
 * Table: messageSenders
 */
@Entity(tableName = "messageSenders")
data class MessageSenderEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val pubkey: ByteArray,
    /// codename
    val codename: String,
    /// User-set nickname (optional)
    val nickname: String? = null,
    /// DM token for direct messaging (optional - null means DM is disabled)
    val dmToken: Int? = null,
    val color: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MessageSenderEntity
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    companion object {
        private val SELF_ID = UUID(0L, 0L).toString()

        fun selfSender(pubkey: ByteArray): MessageSenderEntity {
            return MessageSenderEntity(
                id = SELF_ID,
                pubkey = pubkey,
                codename = "",
                color = 0
            )
        }
    }
}
