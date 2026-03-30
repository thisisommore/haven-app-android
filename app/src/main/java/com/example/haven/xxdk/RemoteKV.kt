package com.example.haven.xxdk

fun interface RemoteKVKeyChangeListener {
    fun onValueChanged(key: String, value: String?)
}

class RemoteKV {
    private val values = linkedMapOf<String, String>()
    var listener: RemoteKVKeyChangeListener? = null

    fun get(key: String): String? = values[key]

    fun put(key: String, value: String) {
        values[key] = value
        listener?.onValueChanged(key, value)
    }

    fun remove(key: String) {
        values.remove(key)
        listener?.onValueChanged(key, null)
    }
}
