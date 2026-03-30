package com.example.haven.xxdk

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * Persistent storage for XXDK app settings
 * Uses DataStore (modern replacement for SharedPreferences)
 */
class XXDKStorage private constructor(private val context: Context) {
    
    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "xxdk_storage")
        
        @Volatile
        private var instance: XXDKStorage? = null
        
        fun getInstance(context: Context): XXDKStorage {
            return instance ?: synchronized(this) {
                instance ?: XXDKStorage(context.applicationContext).also { instance = it }
            }
        }
        
        // Keys
        private val PASSWORD_KEY = stringPreferencesKey("password")
        private val IS_SETUP_COMPLETE_KEY = booleanPreferencesKey("is_setup_complete")
        private val DEVICE_TOKEN_KEY = stringPreferencesKey("device_token_hex")
    }
    
    // Password - blocking getter for synchronous access
    var password: String
        get() = runBlocking { context.dataStore.data.map { it[PASSWORD_KEY] ?: "" }.first() }
        set(value) = runBlocking {
            context.dataStore.edit { it[PASSWORD_KEY] = value }
        }
    
    // Setup complete status - blocking getter for synchronous access
    var isSetupComplete: Boolean
        get() = runBlocking { context.dataStore.data.map { it[IS_SETUP_COMPLETE_KEY] ?: false }.first() }
        set(value) = runBlocking {
            context.dataStore.edit { it[IS_SETUP_COMPLETE_KEY] = value }
        }
    
    // Device token - blocking getter for synchronous access  
    var deviceTokenHex: String?
        get() = runBlocking { context.dataStore.data.map { it[DEVICE_TOKEN_KEY] }.first() }
        set(value) = runBlocking {
            context.dataStore.edit { 
                if (value != null) {
                    it[DEVICE_TOKEN_KEY] = value
                } else {
                    it.remove(DEVICE_TOKEN_KEY)
                }
            }
        }
    
    // Flow versions for reactive UI
    val isSetupCompleteFlow: Flow<Boolean> = context.dataStore.data
        .map { it[IS_SETUP_COMPLETE_KEY] ?: false }
    
    /**
     * Clear all stored data (for logout)
     */
    fun clearAll() = runBlocking {
        context.dataStore.edit { it.clear() }
    }
}
