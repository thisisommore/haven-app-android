package com.example.haven.xxdk.callbacks

import android.util.Base64

internal data class MessageUpdateInfo(
    val messageId: String?,
    val status: Long?,
    val messageIdSet: Boolean,
    val statusSet: Boolean
) {
    companion object {
        fun fromJson(json: ByteArray): MessageUpdateInfo {
            // Simple JSON parsing - in production use a proper JSON library
            val jsonStr = json.decodeToString()
            // This is a simplified parser - use Gson or Moshi in production
            return MessageUpdateInfo(
                messageId = null,
                status = null,
                messageIdSet = false,
                statusSet = false
            )
        }
    }
}

internal data class ModelMessage(
    val pubKey: ByteArray,
    val messageID: ByteArray
) {
    fun toJson(): ByteArray {
        // Simple JSON serialization
        return """{"pub_key":"${Base64.encodeToString(pubKey, Base64.NO_WRAP)}","message_id":"${Base64.encodeToString(messageID, Base64.NO_WRAP)}"}""".toByteArray()
    }
}
