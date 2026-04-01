package com.example.haven.xxdk

/**
 * Progress states for XXDK initialization.
 * Each step increments by 7% (14 steps × 7% = 98%, final step brings to 100%)
 * Matching iOS implementation.
 */
enum class XXDKProgress(val label: String, val increment: Int) {
    Idle("...", 0),
    DownloadingNdf("Downloading NDF", 7),
    SettingUpCmix("Setting up cMixx", 7),
    LoadingCmix("Loading cMixx", 7),
    StartingNetworkFollower("Starting network follower", 7),
    NetworkFollowerComplete("Network follower complete", 7),
    LoadingIdentity("Loading identity", 7),
    CreatingIdentity("Creating your identity", 7),
    SyncingNotifications("Syncing notifications", 7),
    ConnectingToNodes("Connecting to nodes", 7),
    SettingUpRemoteKV("Setting up remote KV", 7),
    WaitingForNetwork("Waiting for network to be ready", 7),
    PreparingChannelsManager("Preparing channels manager", 7),
    JoiningChannels("Joining xxGeneralChat", 7),
    ReadyExistingUser("Preparing", 30), // 9% + 3 skipped steps (7% each)
    Ready("Ready", -1); // -1 = final step, force to 100%

    companion object {
        const val INCREMENT_PER_STEP = 7
    }
}

/**
 * Updates the XXDK status and percentage with the given progress step.
 * Uses incremental calculation matching iOS behavior.
 */
fun XXDK.progress(step: XXDKProgress) {
    status = step.label
    
    statusPercentage = if (step.increment == -1) {
        100
    } else {
        minOf(statusPercentage + step.increment, 100)
    }
}

/**
 * Resets progress to idle state.
 */
fun XXDK.resetProgress() {
    status = XXDKProgress.Idle.label
    statusPercentage = 0
}
