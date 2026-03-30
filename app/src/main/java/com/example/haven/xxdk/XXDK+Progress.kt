package com.example.haven.xxdk

enum class XXDKProgress(val label: String, val percent: Int) {
    Idle("...", 0),
    DownloadingNdf("Downloading NDF", 5),
    SettingUpCmix("Setting up cMixx", 10),
    LoadingCmix("Loading cMixx", 20),
    StartingNetworkFollower("Starting network follower", 40),
    LoadingIdentity("Loading identity", 45),
    CreatingIdentity("Creating your identity", 52),
    SyncingNotifications("Syncing notifications", 59),
    ConnectingToNodes("Connecting to nodes", 66),
    SettingUpRemoteKV("Setting up remote KV", 73),
    WaitingForNetwork("Waiting for network to be ready", 80),
    PreparingChannelsManager("Preparing channels manager", 87),
    JoiningChannels("Joining xxGeneralChat", 94),
    ReadyExistingUser("Preparing", 100),
    Ready("Ready", 100),
}

fun XXDK.progress(step: XXDKProgress) {
    status = step.label
    statusPercentage = step.percent
}
