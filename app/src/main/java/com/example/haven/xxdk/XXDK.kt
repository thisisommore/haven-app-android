package com.example.haven.xxdk

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import bindings.Cmix
import com.example.haven.xxdk.callbacks.ChannelEventModelBuilder
import com.example.haven.xxdk.callbacks.ChannelUICallbacks
import com.example.haven.xxdk.callbacks.DmEvents
import com.example.haven.xxdk.callbacks.DmReceiver
import com.example.haven.xxdk.callbacks.DmReceiverBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

open class XXDK(
    internal val context: Context,
) : XXDKP {
    companion object {
        internal const val TAG = "XXDK"
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
    internal var eventModelBuilder: ChannelEventModelBuilder? = null
    internal var channelUICallbacks: ChannelUICallbacks? = null
    internal var dmReceiver: DmReceiver? = null
    internal var dmReceiverBuilder: DmReceiverBuilder? = null
    internal var dmEvents: DmEvents? = null

    internal var savedPrivateIdentity: ByteArray = byteArrayOf()

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
            progress(XXDKProgress.NetworkFollowerComplete)
        }
    }

    override suspend fun loadClients(privateIdentity: ByteArray) = performLoadClients(privateIdentity)

    override suspend fun setupClients(privateIdentity: ByteArray, successCallback: () -> Unit) =
        performSetupClients(privateIdentity, successCallback)

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

    override suspend fun logout() = performLogout()

    override fun storeApnsToken(token: String) {
        pendingApnsTokenHex = token
        storage?.deviceTokenHex = token
    }
}
