package com.example.haven.xxdk.callbacks

import android.util.Log
import bindings.ChannelUICallbacks as ChannelUICallbacksInterface

/**
 * Channel UI callbacks implementation
 * Mirrors iOS ChannelUICallbacks implementation
 */
class ChannelUICallbacks : ChannelUICallbacksInterface {
    companion object {
        private const val TAG = "ChannelUICallbacks"
    }

    /**
     * Mirrors the JS ChannelEvents enum for readable event types.
     */
    enum class ChannelEvent(val value: Long) {
        NICKNAME_UPDATE(1000),
        NOTIFICATION_UPDATE(2000),
        MESSAGE_RECEIVED(3000),
        USER_MUTED(4000),
        MESSAGE_DELETED(5000),
        ADMIN_KEY_UPDATE(6000),
        DM_TOKEN_UPDATE(7000),
        CHANNEL_UPDATE(8000);

        companion object {
            fun fromValue(value: Long): ChannelEvent? = values().find { it.value == value }
        }
    }

    override fun eventUpdate(eventType: Long, jsonData: ByteArray?) {
        if (jsonData == null) {
            Log.e(TAG, "Channel event update payload is nil for eventType $eventType")
            return
        }

        Log.i(TAG, "Push: channel eventUpdate $eventType")

        when (ChannelEvent.fromValue(eventType)) {
            ChannelEvent.NOTIFICATION_UPDATE -> {
                Log.d(TAG, "Channel Notification Update received")
            }
            else -> {
                // Handle other events as needed
            }
        }
    }
}
