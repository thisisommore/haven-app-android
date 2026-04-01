package com.example.haven.xxdk

import android.util.Base64
import android.util.Log
import bindings.ChannelInfo
import bindings.ChannelsManager
import org.json.JSONObject

/**
 * Data class for share URL information
 */
data class ShareUrlData(
    val url: String,
    val password: String? = null
)

/**
 * Channel implementation using XXDK bindings
 */
class Channel(
    private val channelsManager: ChannelsManager? = null,
    private val cmixId: Long = 0
) : ChannelsP {
    
    companion object {
        private const val TAG = "Channel"
        const val SHARE_URL_HOST = "https://xxnetwork.com/join"
        // Notification levels (from iOS)
        const val CHANNEL_NOTIFY_NONE = 0L
        const val CHANNEL_NOTIFY_MENTIONS = 1L
        const val CHANNEL_NOTIFY_ALL = 2L
        // Notification statuses
        const val CHANNEL_NOTIFY_NO_NOTIFY = 0L
        const val CHANNEL_NOTIFY_NOTIFY = 1L
        const val CHANNEL_NOTIFY_PUSH = 2L
    }
    
    override val msg: ChannelMessaging = ChannelMessaging(channelsManager)
    
    private val mutedChannelIds = mutableSetOf<String>()

    override fun isMuted(channelId: String): Boolean = channelId in mutedChannelIds

    override fun muteUser(channelId: String, pubKey: ByteArray, mute: Boolean) {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return
        }
        
        try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            // Use reflection to call muteUser
            val method = cm.javaClass.getMethod("muteUser", ByteArray::class.java, ByteArray::class.java, Boolean::class.java)
            method.invoke(cm, channelIdBytes, pubKey, mute)
            if (mute) mutedChannelIds += channelId else mutedChannelIds -= channelId
            Log.d(TAG, "User ${if (mute) "muted" else "unmuted"} in channel: $channelId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mute/unmute user: ${e.message}", e)
            // Don't throw - just log error for now
        }
    }

    fun leaveChannel(channelId: String) {
        mutedChannelIds -= channelId
    }
    
    /**
     * Check if DMs are enabled for a channel
     */
    fun areDMsEnabled(channelId: String): Boolean {
        val cm = channelsManager ?: return false
        
        return try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            // Try to call via reflection
            val method = cm.javaClass.getMethod("areDMsEnabled", ByteArray::class.java)
            method.invoke(cm, channelIdBytes) as? Boolean ?: false
        } catch (e: Exception) {
            Log.d(TAG, "areDMsEnabled not available: ${e.message}")
            false
        }
    }
    
    /**
     * Enable direct messages for a channel
     */
    fun enableDirectMessages(channelId: String) {
        val cm = channelsManager ?: return
        
        try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            val method = cm.javaClass.getMethod("enableDirectMessages", ByteArray::class.java)
            method.invoke(cm, channelIdBytes)
            Log.d(TAG, "DMs enabled for channel: $channelId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enable DMs: ${e.message}", e)
            // Don't throw
        }
    }
    
    /**
     * Disable direct messages for a channel
     */
    fun disableDirectMessages(channelId: String) {
        val cm = channelsManager ?: return
        
        try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            val method = cm.javaClass.getMethod("disableDirectMessages", ByteArray::class.java)
            method.invoke(cm, channelIdBytes)
            Log.d(TAG, "DMs disabled for channel: $channelId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disable DMs: ${e.message}", e)
            // Don't throw
        }
    }
    
    /**
     * Get share URL for a channel
     */
    fun getShareUrl(channelId: String): ShareUrlData? {
        // Stub implementation - actual API may differ
        Log.d(TAG, "getShareUrl called for channel: $channelId")
        return null
    }
    
    /**
     * Get muted users for a channel
     */
    fun getMutedUsers(channelId: String): List<ByteArray> {
        // Stub implementation - actual API may differ
        Log.d(TAG, "getMutedUsers called for channel: $channelId")
        return emptyList()
    }
    
    /**
     * Get channel nickname
     */
    fun getChannelNickname(channelId: String): String {
        // Stub implementation
        return ""
    }
    
    /**
     * Set channel nickname
     */
    fun setChannelNickname(channelId: String, nickname: String) {
        // Stub implementation
        Log.d(TAG, "setChannelNickname called for channel: $channelId")
    }
    
    /**
     * Export channel admin key
     */
    fun exportChannelAdminKey(channelId: String, encryptionPassword: String): String {
        // Stub implementation
        Log.d(TAG, "exportChannelAdminKey called for channel: $channelId")
        return "stub-key-content"
    }
    
    /**
     * Import channel admin key
     */
    fun importChannelAdminKey(channelId: String, encryptionPassword: String, privateKey: String) {
        // Stub implementation
        Log.d(TAG, "importChannelAdminKey called for channel: $channelId")
    }
    
    /**
     * Parse ChannelInfo from JSON bytes
     */
    private fun parseChannelInfo(jsonBytes: ByteArray): ChannelInfo? {
        return try {
            val json = JSONObject(jsonBytes.decodeToString())
            val info = ChannelInfo()
            info.setChannelID(json.optString("ChannelID"))
            info.setName(json.optString("Name"))
            info.setDescription(json.optString("Description"))
            info
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse ChannelInfo: ${e.message}")
            null
        }
    }
    
    /**
     * Join a channel from a URL (public share link)
     */
    fun joinChannelFromURL(url: String): ChannelInfo? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            val prettyPrint = bindings.Bindings.decodePublicURL(url)
            val resultBytes = cm.joinChannel(prettyPrint)
            val channelInfo = parseChannelInfo(resultBytes)
            Log.d(TAG, "Joined channel: ${channelInfo?.name}")
            channelInfo
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join channel from URL: ${e.message}", e)
            null
        }
    }
    
    /**
     * Join a channel using pretty print format
     */
    fun joinChannel(prettyPrint: String): ChannelInfo? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            val resultBytes = cm.joinChannel(prettyPrint)
            val channelInfo = parseChannelInfo(resultBytes)
            Log.d(TAG, "Joined channel: ${channelInfo?.name}")
            channelInfo
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join channel: ${e.message}", e)
            null
        }
    }
    
    /**
     * Set notification level for a channel
     */
    fun setNotifications(channelId: String, level: Long, status: Long) {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return
        }
        
        try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            cm.setMobileNotificationsLevel(channelIdBytes, level, status)
            Log.d(TAG, "Set notifications for channel: $channelId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set notifications: ${e.message}", e)
        }
    }
    
    /**
     * Get the channels manager for advanced operations
     */
    fun getChannelsManager(): ChannelsManager? = channelsManager
    
    /**
     * Get cmix ID
     */
    fun getCmixId(): Long = cmixId
}
