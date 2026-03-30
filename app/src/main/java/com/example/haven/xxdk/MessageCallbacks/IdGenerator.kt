package com.example.haven.xxdk.messagecallbacks

import java.util.UUID

object IdGenerator {
    fun next(prefix: String = "id"): String = "$prefix-${UUID.randomUUID()}"
}
