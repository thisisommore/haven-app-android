package com.example.haven.xxdk

import android.util.Base64
import android.util.Log
import bindings.ChannelsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * ChannelMessaging implementation using XXDK bindings
 * Mirrors iOS ChannelMessaging implementation
 */
class ChannelMessaging(private val channelsManager: ChannelsManager? = null) {
    
    companion object {
        private const val TAG = "ChannelMessaging"
        private const val VALID_UNTIL_MS = 30000L // 30 seconds
    }

    suspend fun send(msg: String, channelId: String) = withContext(Dispatchers.IO) {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return@withContext
        }
        
        val channelIdData = Base64.decode(channelId, Base64.NO_WRAP)
        val encodedMsg = MessageEncoding.encodeMessage("<p>$msg</p>") ?: run {
            Log.e(TAG, "Failed to encode message")
            return@withContext
        }
        
        try {
            val cmixParamsJSON = byteArrayOf()
            val pingsJSON: ByteArray? = null
            cm.sendMessage(
                channelIdData,
                encodedMsg,
                VALID_UNTIL_MS,
                cmixParamsJSON,
                pingsJSON
            )
            Log.d(TAG, "Channel message sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send channel message: ${e.message}", e)
        }
    }

    suspend fun reply(msg: String, channelId: String, replyToMessageIdB64: String) = withContext(Dispatchers.IO) {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return@withContext
        }
        
        val channelIdData = Base64.decode(channelId, Base64.NO_WRAP)
        val encodedMsg = MessageEncoding.encodeMessage("<p>$msg</p>") ?: run {
            Log.e(TAG, "Failed to encode message")
            return@withContext
        }
        
        val messageToReactTo = Base64.decode(replyToMessageIdB64, Base64.NO_WRAP)
        
        try {
            val cmixParamsJSON = byteArrayOf()
            val pingsJSON: ByteArray? = null
            cm.sendReply(
                channelIdData,
                encodedMsg,
                messageToReactTo,
                VALID_UNTIL_MS,
                cmixParamsJSON,
                pingsJSON
            )
            Log.d(TAG, "Channel reply sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send channel reply: ${e.message}", e)
        }
    }

    suspend fun react(emoji: String, toMessageIdB64: String, inChannelId: String) = withContext(Dispatchers.IO) {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return@withContext
        }
        
        val channelIdData = Base64.decode(inChannelId, Base64.NO_WRAP)
        val messageToReactTo = Base64.decode(toMessageIdB64, Base64.NO_WRAP)
        
        try {
            val cmixParamsJSON = byteArrayOf()
            cm.sendReaction(
                channelIdData,
                emoji,
                messageToReactTo,
                VALID_UNTIL_MS,
                cmixParamsJSON
            )
            Log.d(TAG, "Channel reaction sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send channel reaction: ${e.message}", e)
        }
    }

    suspend fun delete(channelId: String, messageId: String) = withContext(Dispatchers.IO) {
        // TODO: Implement delete when bindings support it
        Log.d(TAG, "Delete not yet implemented")
    }
}
