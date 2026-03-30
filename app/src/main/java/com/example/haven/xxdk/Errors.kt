package com.example.haven.xxdk

sealed class XXDKError(message: String) : RuntimeException(message) {
    data object InvalidUtf8 : XXDKError("Invalid UTF-8")
    data object AppStateDirNotFound : XXDKError("App state dir not found")
}
