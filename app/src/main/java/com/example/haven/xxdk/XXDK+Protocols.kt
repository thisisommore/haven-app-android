package com.example.haven.xxdk

data class GeneratedIdentity(
    val privateIdentity: ByteArray,
    val codename: String,
    val codeset: Int,
    val pubkey: String,
)

interface ChannelsP {
    val msg: ChannelMessaging
    fun isMuted(channelId: String): Boolean
    fun muteUser(channelId: String, pubKey: ByteArray, mute: Boolean)
}

interface DirectMessageP {
    suspend fun send(msg: String, toPubKey: ByteArray, partnerToken: Int)
    suspend fun reply(msg: String, toPubKey: ByteArray, partnerToken: Int, replyToMessageIdB64: String)
    suspend fun react(emoji: String, toMessageIdB64: String, toPubKey: ByteArray, partnerToken: Int)
}

interface XXDKP {
    val status: String
    val statusPercentage: Int
    val codename: String?
    val codeset: Int
    val channel: ChannelsP
    val dm: DirectMessageP?

    suspend fun newCmix(downloadedNdf: ByteArray)
    suspend fun loadCmix()
    suspend fun startNetworkFollower()
    suspend fun downloadNdf(): ByteArray
    suspend fun loadClients(privateIdentity: ByteArray)
    suspend fun setupClients(privateIdentity: ByteArray, successCallback: () -> Unit)
    fun savePrivateIdentity(privateIdentity: ByteArray)
    fun loadSavedPrivateIdentity(): ByteArray
    suspend fun generateIdentities(amountOfIdentities: Int): List<GeneratedIdentity>
    fun setAppStorage(storage: XXDKStorage)
    suspend fun exportIdentity(password: String): ByteArray
    suspend fun importIdentity(password: String, data: ByteArray): ByteArray
    suspend fun logout()
    fun storeApnsToken(token: String)
}

fun String.data(): ByteArray = encodeToByteArray()

fun ByteArray.utf8(): String = decodeToString()
