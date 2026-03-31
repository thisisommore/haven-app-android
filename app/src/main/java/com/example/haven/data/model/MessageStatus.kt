package com.example.haven.data.model

enum class MessageStatus(val value: Int) {
    UNSENT(0),
    SENT(1),
    DELIVERED(2),
    FAILED(3),
    DELETING(9);

    companion object {
        fun fromValue(value: Int): MessageStatus {
            return entries.find { it.value == value } ?: UNSENT
        }
    }
}
