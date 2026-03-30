package com.example.haven.xxdk

import org.json.JSONObject

data class PublicIdentity(
    val pubKey: String,
    val codename: String,
    val color: String,
    val extension: String,
    val codesetVersion: Int,
)

data class IsReadyInfo(
    val isReady: Boolean,
    val howLong: Double
)

object Parser {
    fun encode(value: Any): ByteArray = value.toString().encodeToByteArray()
    fun decode(raw: ByteArray): String = raw.decodeToString()
    
    fun decodeIdentity(raw: ByteArray): PublicIdentity {
        val json = JSONObject(raw.decodeToString())
        return PublicIdentity(
            pubKey = json.optString("PubKey"),
            codename = json.optString("Codename"),
            color = json.optString("Color"),
            extension = json.optString("Extension"),
            codesetVersion = json.optInt("CodesetVersion"),
        )
    }
    
    fun decodeReadyInfo(raw: ByteArray): IsReadyInfo {
        val json = JSONObject(raw.decodeToString())
        return IsReadyInfo(
            isReady = json.optBoolean("IsReady"),
            howLong = json.optDouble("HowLong")
        )
    }
}
