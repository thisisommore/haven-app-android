package com.example.haven.data

import android.content.Context
import com.example.haven.data.model.ChatModel
import com.example.haven.data.model.MessageSenderModel
import com.example.haven.data.model.ChatMessageModel
import com.example.haven.data.model.MessageReactionModel
import com.example.haven.data.model.Converters
import com.example.haven.data.model.ChatDao
import com.example.haven.data.model.MessageSenderDao
import com.example.haven.data.model.ChatMessageDao
import com.example.haven.data.model.MessageReactionDao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database equivalent to iOS SQLiteData database
 * Version 1: Initial schema matching iOS v1 migration
 */
@Database(
    entities = [
        ChatModel::class,
        MessageSenderModel::class,
        ChatMessageModel::class,
        MessageReactionModel::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun messageSenderDao(): MessageSenderDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun messageReactionDao(): MessageReactionDao

    companion object {
        const val DATABASE_NAME = "haven.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration(false)
                .build()
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
