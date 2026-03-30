package com.example.haven.data.db

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromByteArray(value: ByteArray?): String? {
        return value?.let { java.util.Base64.getEncoder().encodeToString(it) }
    }

    @TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        return value?.let { java.util.Base64.getDecoder().decode(it) }
    }
}
