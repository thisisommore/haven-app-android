package com.example.haven.xxdk

import android.util.Base64
import android.util.Log
import bindings.DMClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DirectMessage implementation using XXDK bindings
 * Mirrors iOS DirectMessage implementation
 */
class DirectMessage(private val dmClient: DMClient? = null) : DirectMessageP {
    
    companion object {
        private const val TAG = "DirectMessage"
        private const val LEASE_TIME_MS = 7L * 24 * 60 * 60 * 1000 // 7 days
    }
    
    /**
     * Get the DM token for QR code sharing
     */
    val token: Long
        get() = dmClient?.token ?: 0L
    
    /**
     * Get the public key for QR code sharing
     */
    val publicKey: ByteArray
        get() = dmClient?.publicKey ?: byteArrayOf()

    override suspend fun send(msg: String, toPubKey: ByteArray, partnerToken: Int) = withContext(Dispatchers.IO) {
        val client = dmClient ?: run {
            Log.e(TAG, "DM client not available")
            return@withContext
        }
        
        val encodedMsg = MessageEncoding.encodeMessage("<p>$msg</p>") ?: run {
            Log.e(TAG, "Failed to encode message")
            return@withContext
        }
        
        try {
            // Use empty cmix params to use defaults (matches iOS)
            val cmixParamsJSON = byteArrayOf()
            client.sendText(
                toPubKey,
                partnerToken,
                encodedMsg,
                LEASE_TIME_MS,
                cmixParamsJSON
            )
            Log.d(TAG, "Message sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send message: ${e.message}", e)
        }
    }

    override suspend fun reply(
        msg: String,
        toPubKey: ByteArray,
        partnerToken: Int,
        replyToMessageIdB64: String,
    ) = withContext(Dispatchers.IO) {
        val client = dmClient ?: run {
            Log.e(TAG, "DM client not available")
            return@withContext
        }
        
        val encodedMsg = MessageEncoding.encodeMessage("<p>$msg</p>") ?: run {
            Log.e(TAG, "Failed to encode message")
            return@withContext
        }
        
        val replyToBytes = Base64.decode(replyToMessageIdB64, Base64.NO_WRAP)
        
        try {
            val cmixParamsJSON = byteArrayOf()
            client.sendReply(
                toPubKey,
                partnerToken,
                encodedMsg,
                replyToBytes,
                LEASE_TIME_MS,
                cmixParamsJSON
            )
            Log.d(TAG, "Reply sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send reply: ${e.message}", e)
        }
    }

    override suspend fun react(
        emoji: String,
        toMessageIdB64: String,
        toPubKey: ByteArray,
        partnerToken: Int,
    ) = withContext(Dispatchers.IO) {
        val client = dmClient ?: run {
            Log.e(TAG, "DM client not available")
            return@withContext
        }
        
        val reactToBytes = Base64.decode(toMessageIdB64, Base64.NO_WRAP)
        
        try {
            val cmixParamsJSON = byteArrayOf()
            client.sendReaction(
                toPubKey,
                partnerToken,
                emoji,
                reactToBytes,
                cmixParamsJSON
            )
            Log.d(TAG, "Reaction sent successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send reaction: ${e.message}", e)
        }
    }
}
