package com.example.haven.xxdk.callbacks

import android.util.Log
import bindings.DmCallbacks

/**
 * DM events callback implementation
 * Mirrors iOS DMReceiver's DmCallbacks implementation
 */
class DmEvents : DmCallbacks {
    companion object {
        private const val TAG = "DmEvents"
    }

    override fun eventUpdate(eventType: Long, jsonData: ByteArray?) {
        if (jsonData == null) {
            Log.e(TAG, "DM event update payload is nil for eventType $eventType")
            return
        }

        Log.i(TAG, "Push: eventUpdate $eventType")

        when (eventType) {
            1000L -> {
                // DmNotificationUpdate
                Log.d(TAG, "DM Notification Update received")
            }
            2000L -> {
                // DmBlockedUser
                Log.d(TAG, "DM Blocked User received")
            }
            3000L -> {
                // DmMessageReceived
                Log.d(TAG, "DM Message Received received")
            }
            4000L -> {
                // DmMessageDeleted
                Log.d(TAG, "DM Message Deleted received")
            }
            else -> {
                Log.w(TAG, "Unknown DM event type: $eventType")
            }
        }
    }
}
