package com.example.haven.xxdk

import android.util.Base64
import android.util.Log
import bindings.ChannelInfo
import bindings.ChannelsManager
import org.json.JSONObject

/**
 * Channel implementation using XXDK bindings
 */
class Channel(
    private val channelsManager: ChannelsManager? = null,
    private val cmixId: Long = 0
) : ChannelsP {
    
    companion object {
        private const val TAG = "Channel"
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
        if (mute) mutedChannelIds += channelId else mutedChannelIds -= channelId
    }

    fun leaveChannel(channelId: String) {
        mutedChannelIds -= channelId
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
     * Mirrors iOS joinChannelFromURL
     */
    fun joinChannelFromURL(url: String): ChannelInfo? {
        val cm = channelsManager ?: run {
            Log.e(TAG, "Channels manager not available")
            return null
        }
        
        return try {
            // Decode the URL to get pretty print format
            val prettyPrint = bindings.Bindings.decodePublicURL(url)
            
            // Join the channel using pretty print
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
