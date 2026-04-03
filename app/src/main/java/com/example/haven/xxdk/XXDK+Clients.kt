package com.example.haven.xxdk

import android.util.Log
import com.example.haven.data.DatabaseModule
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.xxdk.callbacks.DmReceiverBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

internal suspend fun XXDK.performLoadClients(privateIdentity: ByteArray) = withContext(Dispatchers.IO) {
    progress(XXDKProgress.LoadingIdentity)

    val liveCmix = cmix ?: error("cmix is not available")

    val publicIdentity = Parser.decodeIdentity(bindings.Bindings.getPublicChannelIdentityFromPrivate(privateIdentity))
    codename = publicIdentity.codename
    codeset = publicIdentity.codesetVersion
    savePrivateIdentity(privateIdentity)

    progress(XXDKProgress.CreatingIdentity)

    // Load notifications
    val notifs = try {
        bindings.Bindings.loadNotifications(liveCmix.id)
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Could not load notifications: ${e.message}")
        null
    }
    notifications = notifs

    progress(XXDKProgress.SyncingNotifications)
    progress(XXDKProgress.ConnectingToNodes)
    progress(XXDKProgress.SettingUpRemoteKV)

    // Set up remoteKV
    try {
        val kv = liveCmix.remoteKV
        remoteKV = kv
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to set up remote KV: ${e.message}")
    }

    progress(XXDKProgress.WaitingForNetwork)
    progress(XXDKProgress.PreparingChannelsManager)

    // Create Channels Manager for returning users
    var channelsManager: bindings.ChannelsManager? = null
    try {
        val extensionJSON = "[]".toByteArray()
        channelsManager = bindings.Bindings.newChannelsManager(
            liveCmix.id,
            privateIdentity,
            eventModelBuilder!!,
            extensionJSON,
            notifs?.id ?: 0,
            channelUICallbacks!!
        )
        Log.d(XXDK.TAG, "ChannelsManager created for returning user")
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to create channels manager: ${e.message}", e)
    }

    // Create DM client for returning users
    var dmClient: bindings.DMClient? = null
    try {
        val dmReceiverBuilder = DmReceiverBuilder(context)
        val dmCallbacks = object : bindings.DmCallbacks {
            override fun eventUpdate(p0: Long, p1: ByteArray?) {
                // Handle DM callbacks
            }
        }
        dmClient = bindings.DMClient(
            liveCmix.id,
            notifs?.id ?: 0,
            privateIdentity,
            dmReceiverBuilder,
            dmCallbacks
        )
        Log.d(XXDK.TAG, "DMClient created for returning user")
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to create DM client for returning user: ${e.message}", e)
    }

    // Initialize messaging classes with bindings if available
    channelsManager?.let {
        channel = Channel(it, liveCmix.id)
        Log.d(XXDK.TAG, "Channel messaging initialized for returning user")
    }
    dmClient?.let {
        dm = DirectMessage(it)
        Log.d(XXDK.TAG, "DM messaging initialized for returning user")
    }

    progress(XXDKProgress.ReadyExistingUser)
}

internal suspend fun XXDK.performSetupClients(
    privateIdentity: ByteArray,
    successCallback: () -> Unit
) = withContext(Dispatchers.IO) {
    val liveCmix = cmix ?: error("cmix is not available")
    val repository = DatabaseModule.provideRepository(context)

    progress(XXDKProgress.LoadingIdentity)
    delay(500) // Small delay for UX

    val publicIdentity = Parser.decodeIdentity(bindings.Bindings.getPublicChannelIdentityFromPrivate(privateIdentity))
    codename = publicIdentity.codename
    codeset = publicIdentity.codesetVersion
    savePrivateIdentity(privateIdentity)

    progress(XXDKProgress.CreatingIdentity)
    delay(500)

    // Notifications
    progress(XXDKProgress.SyncingNotifications)
    try {
        val notifs = bindings.Bindings.loadNotifications(liveCmix.id)
        notifications = notifs
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Could not load notifications: ${e.message}")
    }
    delay(500)

    progress(XXDKProgress.ConnectingToNodes)
    delay(500)

    progress(XXDKProgress.SettingUpRemoteKV)
    try {
        val kv = liveCmix.remoteKV
        remoteKV = kv
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to set up remote KV: ${e.message}")
    }
    delay(500)

    progress(XXDKProgress.WaitingForNetwork)
    delay(800)

    progress(XXDKProgress.PreparingChannelsManager)

    // Create Channels Manager
    var channelsManager: bindings.ChannelsManager? = null
    try {
        val extensionJSON = "[]".toByteArray()
        channelsManager = bindings.Bindings.newChannelsManager(
            liveCmix.id,
            privateIdentity,
            eventModelBuilder!!,
            extensionJSON,
            notifications?.id ?: 0,
            channelUICallbacks!!
        )
        Log.d(XXDK.TAG, "ChannelsManager created successfully")
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to create channels manager: ${e.message}", e)
    }
    delay(600)

    // Create DM client
    var dmClient: bindings.DMClient? = null
    try {
        val dmReceiverBuilder = DmReceiverBuilder(context)
        val dmCallbacks = object : bindings.DmCallbacks {
            override fun eventUpdate(p0: Long, p1: ByteArray?) {
                // Handle DM callbacks
            }
        }
        dmClient = bindings.DMClient(
            liveCmix.id,
            notifications?.id ?: 0,
            privateIdentity,
            dmReceiverBuilder,
            dmCallbacks
        )
        Log.d(XXDK.TAG, "DMClient created successfully")

        val selfPubKey = dmClient.publicKey
        val token = dmClient.token.toInt()

        // Create self chat
        val currentCodename = codename
        if (!currentCodename.isNullOrEmpty()) {
            val existingSelfChat = repository.getChatByPubKey(selfPubKey)
            if (existingSelfChat == null) {
                val selfChat = ChatModel(
                    name = "<self>",
                    pubKey = selfPubKey,
                    dmToken = token,
                    color = 0xE97451
                )
                val selfSender = MessageSenderModel(
                    id = UUID.nameUUIDFromBytes(selfPubKey).toString(),
                    pubkey = selfPubKey,
                    codename = currentCodename,
                    color = 0xE97451
                )
                repository.insertChat(selfChat)
                repository.insertSender(selfSender)
            }
        }
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to setup DM client: ${e.message}", e)
    }

    // Initialize messaging classes with bindings if available
    channelsManager?.let {
        channel = Channel(it, liveCmix.id)
        Log.d(XXDK.TAG, "Channel messaging initialized")
    }
    dmClient?.let {
        dm = DirectMessage(it)
        Log.d(XXDK.TAG, "DM messaging initialized")
    }

    progress(XXDKProgress.JoiningChannels)

    // Join xxIOS channel (same as iOS)
    try {
        val channelInfo = channel.joinChannelFromURL(XX_IOS_CHAT)
        val channelId = channelInfo?.channelId ?: "xxIOS"

        // Set notifications to push for all messages
        channel.setNotifications(
            channelId,
            level = Channel.CHANNEL_NOTIFY_ALL,
            status = Channel.CHANNEL_NOTIFY_PUSH
        )

        // Create local chat entry if not exists
        val existingChannel = repository.getChatByChannelId(channelId)
        if (existingChannel == null) {
            val channelChat = ChatModel(
                name = channelInfo?.name ?: "xxGeneralChat",
                channelId = channelId
            )
            repository.insertChat(channelChat)
            Log.d(XXDK.TAG, "Created xxIOS channel chat in database")
        }
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to join xxIOS channel: ${e.message}", e)
        // Still create a local entry as fallback
        try {
            val existingChannel = repository.getChatByChannelId("xxIOS")
            if (existingChannel == null) {
                val channelChat = ChatModel(
                    name = "xxGeneralChat",
                    channelId = "xxIOS"
                )
                repository.insertChat(channelChat)
            }
        } catch (e2: Exception) {
            Log.e(XXDK.TAG, "Failed to create fallback channel: ${e2.message}")
        }
    }
    delay(500)

    progress(XXDKProgress.Ready)
    delay(500)

    // Mark setup complete
    storage?.isSetupComplete = true

    successCallback()
}
