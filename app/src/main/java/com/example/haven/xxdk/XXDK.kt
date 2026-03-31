package com.example.haven.xxdk

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bindings.Cmix
import com.example.haven.data.db.ChatEntity
import com.example.haven.data.db.DatabaseModule
import com.example.haven.data.db.MessageSenderEntity
import com.example.haven.xxdk.callbacks.ChannelEventModelBuilder
import com.example.haven.xxdk.callbacks.ChannelUICallbacks
import com.example.haven.xxdk.callbacks.DmEvents
import com.example.haven.xxdk.callbacks.DmReceiver
import com.example.haven.xxdk.callbacks.DmReceiverBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

open class XXDK(
    private val context: Context,
) : XXDKP {
    companion object {
        private const val TAG = "XXDK"
    }
    override var status by mutableStateOf("...")
    override var statusPercentage by mutableIntStateOf(0)
    override var codename by mutableStateOf<String?>(null)
    override var codeset by mutableIntStateOf(0)

    override var channel: Channel = Channel()
    override var dm: DirectMessage = DirectMessage()

    var stateDir: String = File(context.filesDir, DEFAULT_STATE_DIR).absolutePath
    var storage: XXDKStorage? = null
    var storageTagListener: Any? = null
    var remoteKV: bindings.RemoteKV? = null
    var notifications: bindings.Notifications? = null
    var cmix: Cmix? = null
    var pendingApnsTokenHex: String? = null

    // Callbacks
    private var eventModelBuilder: ChannelEventModelBuilder? = null
    private var channelUICallbacks: ChannelUICallbacks? = null
    private var dmReceiver: DmReceiver? = null
    private var dmReceiverBuilder: DmReceiverBuilder? = null
    private var dmEvents: DmEvents? = null

    private var savedPrivateIdentity: ByteArray = byteArrayOf()

    init {
        bindings.Bindings.setTimeSource(NetTime)
        File(stateDir).mkdirs()
        
        // Initialize callbacks
        eventModelBuilder = ChannelEventModelBuilder(context)
        channelUICallbacks = ChannelUICallbacks()
        dmReceiver = DmReceiver(context)
        dmReceiverBuilder = DmReceiverBuilder(context)
        dmEvents = DmEvents()
    }

    override fun setAppStorage(storage: XXDKStorage) {
        this.storage = storage
        pendingApnsTokenHex?.let {
            storage.deviceTokenHex = it
            pendingApnsTokenHex = null
        }
    }

    override suspend fun downloadNdf(): ByteArray = withContext(Dispatchers.IO) {
        progress(XXDKProgress.DownloadingNdf)
        val cert = context.resources.openRawResource(com.example.haven.R.raw.mainnet).use { it.readBytes().decodeToString() }
        bindings.Bindings.downloadAndVerifySignedNdfWithUrl(MAINNET_NDF_URL, cert)
    }

    override suspend fun newCmix(downloadedNdf: ByteArray) = withContext(Dispatchers.IO) {
        progress(XXDKProgress.SettingUpCmix)
        val secret = storage?.password?.encodeToByteArray() ?: error("Password missing")
        val stateFile = File(stateDir)
        if (stateFile.exists()) {
            stateFile.deleteRecursively()
        }
        stateFile.mkdirs()
        bindings.Bindings.newCmix(String(downloadedNdf), stateDir, secret, "")
    }

    override suspend fun loadCmix() = withContext(Dispatchers.IO) {
        progress(XXDKProgress.LoadingCmix)
        val secret = storage?.password?.encodeToByteArray() ?: error("Password missing")
        cmix = bindings.Bindings.loadCmix(stateDir, secret, byteArrayOf())
    }

    override suspend fun startNetworkFollower() {
        withContext(Dispatchers.IO) {
            progress(XXDKProgress.StartingNetworkFollower)
            cmix?.startNetworkFollower(5_000)
            cmix?.waitForNetwork(30_000)
        }
    }

    override suspend fun loadClients(privateIdentity: ByteArray) = withContext(Dispatchers.IO) {
        progress(XXDKProgress.LoadingIdentity)
        
        val liveCmix = cmix ?: error("cmix is not available")
        
        val publicIdentity = Parser.decodeIdentity(bindings.Bindings.getPublicChannelIdentityFromPrivate(privateIdentity))
        codename = publicIdentity.codename
        codeset = publicIdentity.codesetVersion
        savePrivateIdentity(privateIdentity)
        
        // Load notifications
        val notifs = try {
            bindings.Bindings.loadNotifications(liveCmix.id)
        } catch (e: Exception) {
            Log.e(TAG, "Could not load notifications: ${e.message}")
            null
        }
        this@XXDK.notifications = notifs
        
        progress(XXDKProgress.CreatingIdentity)
        
        // Create Channels Manager for returning users
        try {
            val extensionJSON = "[]".toByteArray()
            val channelsManager = bindings.Bindings.newChannelsManager(
                liveCmix.id,
                privateIdentity,
                eventModelBuilder!!,
                extensionJSON,
                notifs?.id ?: 0,
                channelUICallbacks!!
            )
            channel = Channel(channelsManager, liveCmix.id)
            Log.d(TAG, "ChannelsManager created for returning user")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create channels manager: ${e.message}", e)
        }
        
        // Create DM client for returning users
        try {
            val dmReceiverBuilder = DmReceiverBuilder(context)
            val dmCallbacks = object : bindings.DmCallbacks {
                override fun eventUpdate(p0: Long, p1: ByteArray?) {}
            }
            val dmClient = bindings.DMClient(
                liveCmix.id,
                notifs?.id ?: 0,
                privateIdentity,
                dmReceiverBuilder,
                dmCallbacks
            )
            dm = DirectMessage(dmClient)
            Log.d(TAG, "DMClient created for returning user")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup DM client: ${e.message}", e)
        }
        
        progress(XXDKProgress.ReadyExistingUser)
    }

    override suspend fun setupClients(privateIdentity: ByteArray, successCallback: () -> Unit) = withContext(Dispatchers.IO) {
        val liveCmix = cmix ?: error("cmix is not available")
        val repository = DatabaseModule.provideRepository(context)
        
        progress(XXDKProgress.LoadingIdentity)
        kotlinx.coroutines.delay(500) // Small delay for UX
        
        val publicIdentity = Parser.decodeIdentity(bindings.Bindings.getPublicChannelIdentityFromPrivate(privateIdentity))
        codename = publicIdentity.codename
        codeset = publicIdentity.codesetVersion
        savePrivateIdentity(privateIdentity)
        
        progress(XXDKProgress.CreatingIdentity)
        kotlinx.coroutines.delay(500)
        
        // Notifications
        progress(XXDKProgress.SyncingNotifications)
        try {
            val notifs = bindings.Bindings.loadNotifications(liveCmix.id)
            this@XXDK.notifications = notifs
        } catch (e: Exception) {
            Log.e(TAG, "Could not load notifications: ${e.message}")
        }
        kotlinx.coroutines.delay(500)
        
        progress(XXDKProgress.ConnectingToNodes)
        kotlinx.coroutines.delay(500)
        
        progress(XXDKProgress.SettingUpRemoteKV)
        try {
            val kv = liveCmix.remoteKV
            remoteKV = kv
            // TODO: Set up storage tag listener when RemoteKV is properly implemented
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up remote KV: ${e.message}")
        }
        kotlinx.coroutines.delay(500)
        
        progress(XXDKProgress.WaitingForNetwork)
        kotlinx.coroutines.delay(800)
        
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
            Log.d(TAG, "ChannelsManager created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create channels manager: ${e.message}", e)
        }
        kotlinx.coroutines.delay(600)
        
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
            Log.d(TAG, "DMClient created successfully")
            
            val selfPubKey = dmClient.publicKey
            val token = dmClient.token.toInt()
            
            // Create self chat
            val currentCodename = codename
            if (!currentCodename.isNullOrEmpty()) {
                val existingSelfChat = repository.getChatByPubKey(selfPubKey)
                if (existingSelfChat == null) {
                    val selfChat = ChatEntity(
                        name = "<self>",
                        pubKey = selfPubKey,
                        dmToken = token,
                        color = 0xE97451
                    )
                    val selfSender = MessageSenderEntity(
                        id = java.util.UUID.nameUUIDFromBytes(selfPubKey).toString(),
                        pubkey = selfPubKey,
                        codename = currentCodename,
                        color = 0xE97451
                    )
                    repository.insertChat(selfChat)
                    repository.insertSender(selfSender)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to setup DM client: ${e.message}", e)
        }
        
        // Initialize messaging classes with bindings if available
        channelsManager?.let { 
            channel = Channel(it, liveCmix.id)
            Log.d(TAG, "Channel messaging initialized")
        }
        dmClient?.let { 
            dm = DirectMessage(it)
            Log.d(TAG, "DM messaging initialized")
        }
        
        progress(XXDKProgress.JoiningChannels)
        
        // Join xxIOS channel (same as iOS)
        try {
            val channelInfo = channel.joinChannelFromURL(XX_IOS_CHAT)
            val channelId = channelInfo?.channelID ?: "xxIOS"
            
            // Set notifications to push for all messages
            channel.setNotifications(channelId, 
                level = Channel.CHANNEL_NOTIFY_ALL,
                status = Channel.CHANNEL_NOTIFY_PUSH
            )
            
            // Create local chat entry if not exists
            val existingChannel = repository.getChatByChannelId(channelId)
            if (existingChannel == null) {
                val channelChat = ChatEntity(
                    name = channelInfo?.name ?: "xxGeneralChat",
                    channelId = channelId
                )
                repository.insertChat(channelChat)
                Log.d(TAG, "Created xxIOS channel chat in database")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to join xxIOS channel: ${e.message}", e)
            // Still create a local entry as fallback
            try {
                val existingChannel = repository.getChatByChannelId("xxIOS")
                if (existingChannel == null) {
                    val channelChat = ChatEntity(
                        name = "xxGeneralChat",
                        channelId = "xxIOS"
                    )
                    repository.insertChat(channelChat)
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to create fallback channel: ${e2.message}")
            }
        }
        kotlinx.coroutines.delay(500)
        
        progress(XXDKProgress.Ready)
        kotlinx.coroutines.delay(500)
        
        // Mark setup complete
        storage?.isSetupComplete = true
        
        successCallback()
    }

    override fun savePrivateIdentity(privateIdentity: ByteArray) {
        val liveCmix = cmix ?: error("cmix not initialized")
        savedPrivateIdentity = privateIdentity
        liveCmix.ekvSet("MyPrivateIdentity", privateIdentity)
    }

    override fun loadSavedPrivateIdentity(): ByteArray {
        val liveCmix = cmix ?: error("cmix not initialized")
        return liveCmix.ekvGet("MyPrivateIdentity")
    }

    override suspend fun generateIdentities(amountOfIdentities: Int): List<GeneratedIdentity> = withContext(Dispatchers.IO) {
        val liveCmix = cmix ?: return@withContext emptyList()
        List(amountOfIdentities) { index ->
            val privateIdentity = bindings.Bindings.generateChannelIdentity(liveCmix.id)
            val publicIdentity = Parser.decodeIdentity(bindings.Bindings.getPublicChannelIdentityFromPrivate(privateIdentity))
            GeneratedIdentity(
                privateIdentity = privateIdentity,
                codename = publicIdentity.codename.ifBlank { "Identity$index" },
                codeset = publicIdentity.codesetVersion,
                pubkey = publicIdentity.pubKey,
            )
        }
    }

    override suspend fun exportIdentity(password: String): ByteArray = withContext(Dispatchers.IO) {
        loadSavedPrivateIdentity()
    }

    override suspend fun importIdentity(password: String, data: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        savedPrivateIdentity = bindings.Bindings.importPrivateIdentity(password, data)
        status = "Identity imported"
        savedPrivateIdentity
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        progress(XXDKProgress.Idle)
        
        // 1. Stop network follower
        runCatching {
            cmix?.stopNetworkFollower()
        }.onFailure {
            Log.w(TAG, "Failed to stop network follower: ${it.message}")
        }
        
        // 2. Wait for running processes (with timeout)
        var retryCount = 0
        while (retryCount < 30) { // 3 seconds timeout
            try {
                if (cmix?.hasRunningProcessies() != true) break
            } catch (e: Exception) {
                break
            }
            delay(100)
            retryCount++
        }
        if (retryCount >= 30) {
            Log.w(TAG, "Force stopping processes after timeout")
        }
        
        // 3. Remove cmix from Go-side tracker to release references
        cmix?.let {
            try {
                bindings.Bindings.deleteCmixInstance(it.getID())
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete cmix instance: ${e.message}")
            }
        }
        
        // 4. Reset binding objects
        channel = Channel()
        dm = DirectMessage()
        cmix = null
        remoteKV = null
        notifications = null
        storageTagListener = null
        savedPrivateIdentity = byteArrayOf()
        
        // 5. Clear caches
        ReceiverHelpers.clearInstance()
        
        // 6. Delete stateDir and recreate it
        val stateFile = File(stateDir)
        if (stateFile.exists()) {
            stateFile.deleteRecursively()
        }
        stateFile.mkdirs()
        
        // Reset status
        codename = null
        codeset = 0
        status = XXDKProgress.Idle.label
        statusPercentage = XXDKProgress.Idle.percent
    }

    override fun storeApnsToken(token: String) {
        pendingApnsTokenHex = token
        storage?.deviceTokenHex = token
    }
}
