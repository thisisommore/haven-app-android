package com.example.haven.data.db

import android.content.Context
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
        ChatEntity::class,
        MessageSenderEntity::class,
        ChatMessageEntity::class,
        MessageReactionEntity::class
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
