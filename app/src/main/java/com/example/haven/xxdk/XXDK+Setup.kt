package com.example.haven.xxdk

fun XXDK.setupStateDirectories(basePath: String = DEFAULT_STATE_DIR): String = basePath

fun XXDK.setAppStateDir(path: String) {
    stateDir = path
}
