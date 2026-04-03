package com.example.haven.xxdk

import android.util.Base64
import android.util.Log
import bindings.ChannelInfo
import bindings.ChannelsManager
import org.json.JSONObject

/**
 * Privacy level for channel URLs
 */
enum class PrivacyLevel {
    PUBLIC,
    SECRET
}

/**
 * Data class for channel information from JSON
 */
data class ChannelInfoJson(
    val channelId: String,
    val name: String,
    val description: String
)

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
     * Mirrors iOS getShareURL implementation
     * Signature: getShareURL(cmixId, host, maxUses, channelIdBytes)
     * Returns JSON with "url" and "password" fields
     */
    fun getShareUrl(channelId: String): ShareUrlData? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            
            // Direct API call matching iOS signature: (cmixId, host, maxUses, channelIdBytes)
            val shareUrlJsonBytes = cm.getShareURL(cmixId, SHARE_URL_HOST, 0, channelIdBytes)
            
            // Parse JSON like iOS does
            Parser.decodeShareURL(shareUrlJsonBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get share URL: ${e.message}", e)
            null
        }
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
     * Check if current user is admin of a channel
     */
    fun isAdmin(channelId: String): Boolean {
        val cm = channelsManager ?: return false
        
        return try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            cm.isChannelAdmin(channelIdBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check admin status: ${e.message}", e)
            false
        }
    }
    
    /**
     * Export channel admin key
     */
    fun exportAdminKey(channelId: String, encryptionPassword: String): ByteArray? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            cm.exportChannelAdminKey(channelIdBytes, encryptionPassword)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export admin key: ${e.message}", e)
            null
        }
    }
    
    /**
     * Import channel admin key
     */
    fun importAdminKey(channelId: String, encryptionPassword: String, privateKey: String): Boolean {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return false
        }
        
        return try {
            val channelIdBytes = Base64.decode(channelId, Base64.NO_WRAP)
            cm.importChannelAdminKey(channelIdBytes, encryptionPassword, privateKey.toByteArray())
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import admin key: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get the privacy level for a given channel URL
     * Returns PUBLIC (0,1) or SECRET (2)
     */
    fun getPrivacyLevel(url: String): PrivacyLevel {
        return try {
            val typeValue = bindings.Bindings.getShareUrlType(url)
            if (typeValue == 2L) PrivacyLevel.SECRET else PrivacyLevel.PUBLIC
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get privacy level: ${e.message}", e)
            PrivacyLevel.PUBLIC // Default to public on error
        }
    }
    
    /**
     * Decode a public URL to pretty print format
     */
    fun decodePublicURL(url: String): String {
        return try {
            bindings.Bindings.decodePublicURL(url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode public URL: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Decode a private URL with password
     */
    fun decodePrivateURL(url: String, password: String): String {
        return try {
            bindings.Bindings.decodePrivateURL(url, password)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode private URL: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Get channel JSON from pretty print format
     */
    fun getChannelJSON(prettyPrint: String): ChannelInfoJson? {
        return try {
            val jsonBytes = bindings.Bindings.getChannelJSON(prettyPrint)
            val json = JSONObject(jsonBytes.decodeToString())
            ChannelInfoJson(
                channelId = json.optString("ChannelID"),
                name = json.optString("Name"),
                description = json.optString("Description")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get channel JSON: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get channel data from a private URL with password
     */
    fun getPrivateChannelFrom(url: String, password: String): ChannelInfoJson? {
        return try {
            val prettyPrint = decodePrivateURL(url, password)
            getChannelJSON(prettyPrint)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get private channel: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get channel data from a public URL
     */
    fun getChannelFrom(url: String): ChannelInfoJson? {
        return try {
            val prettyPrint = decodePublicURL(url)
            getChannelJSON(prettyPrint)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get channel from URL: ${e.message}", e)
            null
        }
    }
    
    /**
     * Join a channel from a URL (public share link)
     */
    fun joinChannelFromURL(url: String): ChannelInfoJson? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            val prettyPrint = bindings.Bindings.decodePublicURL(url)
            val resultBytes = cm.joinChannel(prettyPrint)
            val channelInfo = parseChannelInfo(resultBytes)
            Log.d(TAG, "Joined channel: ${channelInfo?.name}")
            channelInfo?.let {
                ChannelInfoJson(
                    channelId = it.getChannelID(),
                    name = it.getName(),
                    description = it.getDescription()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join channel from URL: ${e.message}", e)
            null
        }
    }
    
    /**
     * Join a channel using pretty print format
     */
    fun joinChannel(prettyPrint: String): ChannelInfoJson? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            val resultBytes = cm.joinChannel(prettyPrint)
            val channelInfo = parseChannelInfo(resultBytes)
            Log.d(TAG, "Joined channel: ${channelInfo?.getName()}")
            channelInfo?.let {
                ChannelInfoJson(
                    channelId = it.getChannelID(),
                    name = it.getName(),
                    description = it.getDescription()
                )
            }
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
