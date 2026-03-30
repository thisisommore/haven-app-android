package com.example.haven.xxdk.messagecallbacks

class ChannelsCallbacks(
    private val uiCallbacks: ChannelUICallbacks = ChannelUICallbacks(),
) {
    fun onChannelMessage(channelId: String, text: String) {
        uiCallbacks.onMessage(channelId, text)
    }
}
