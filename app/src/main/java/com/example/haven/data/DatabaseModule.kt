package com.example.haven.data

import android.content.Context

/**
 * Provides database dependencies without Hilt
 * Use this as a simple service locator pattern
 */
object DatabaseModule {
    
    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var repository: DatabaseRepository? = null
    
    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: AppDatabase.getInstance(context).also { database = it }
        }
    }
    
    fun provideRepository(context: Context): DatabaseRepository {
        return repository ?: synchronized(this) {
            repository ?: DatabaseRepository(context).also { repository = it }
        }
    }
    
    fun clearInstance() {
        database = null
        repository = null
        AppDatabase.destroyInstance()
    }
}
