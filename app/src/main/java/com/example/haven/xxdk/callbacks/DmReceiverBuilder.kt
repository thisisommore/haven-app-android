package com.example.haven.xxdk.callbacks

import android.content.Context
import bindings.DMReceiver
import bindings.DMReceiverBuilder

/**
 * Builder for DMReceiver
 * Mirrors iOS DMReceiverBuilder implementation
 */
class DmReceiverBuilder(private val context: Context) : DMReceiverBuilder {

    private val receiver: DMReceiver by lazy { DmReceiver(context) }

    override fun build(path: String?): DMReceiver {
        return receiver
    }
}
