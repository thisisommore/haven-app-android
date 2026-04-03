package com.example.haven.xxdk

import android.util.Log
import com.example.haven.data.DatabaseModule
import com.example.haven.xxdk.callbacks.CallbackScopeProvider
import com.example.haven.xxdk.callbacks.ReceiverHelpers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

internal suspend fun XXDK.performLogout() = withContext(dispatchers.io) {
    progress(XXDKProgress.Idle)

    // 1. Stop network follower
    runCatching {
        cmix?.stopNetworkFollower()
    }.onFailure {
        Log.w(XXDK.TAG, "Failed to stop network follower: ${it.message}")
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
        Log.w(XXDK.TAG, "Force stopping processes after timeout")
    }

    // 3. Remove cmix from Go-side tracker to release references
    cmix?.let {
        try {
            bindings.Bindings.deleteCmixInstance(it.getID())
        } catch (e: Exception) {
            Log.w(XXDK.TAG, "Failed to delete cmix instance: ${e.message}")
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

    // 5. Clear caches and shutdown callback scope
    ReceiverHelpers.clearInstance()
    CallbackScopeProvider.shutdown()

    // 6. Clear database (messages, reactions, senders, chats)
    try {
        val repository = DatabaseModule.provideRepository(context)
        repository.clearAllData()
        Log.d(XXDK.TAG, "Database cleared successfully")
    } catch (e: Exception) {
        Log.e(XXDK.TAG, "Failed to clear database: ${e.message}")
    }

    // 7. Delete stateDir and recreate it
    val stateFile = File(stateDir)
    if (stateFile.exists()) {
        stateFile.deleteRecursively()
    }
    stateFile.mkdirs()

    // Reset status
    codename = null
    codeset = 0
    status = XXDKProgress.Idle.label
    statusPercentage = 0
}
