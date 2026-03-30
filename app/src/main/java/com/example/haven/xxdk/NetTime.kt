package com.example.haven.xxdk

object NetTime : bindings.TimeSource {
    override fun nowMs(): Long = System.currentTimeMillis()
}
