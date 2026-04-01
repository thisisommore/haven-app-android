package com.example.haven.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.emojiDataStore: DataStore<Preferences> by preferencesDataStore(name = "recent_emojis")

class RecentEmojiStore(private val context: Context) {
    
    companion object {
        private val RECENT_EMOJIS_KEY = stringPreferencesKey("recent_emojis")
        private const val MAX_RECENT_EMOJIS = 48
    }
    
    val recentEmojis: Flow<List<String>> = context.emojiDataStore.data
        .map { preferences ->
            val emojiString = preferences[RECENT_EMOJIS_KEY] ?: ""
            if (emojiString.isEmpty()) {
                emptyList()
            } else {
                emojiString.split(",").filter { it.isNotEmpty() }
            }
        }
    
    suspend fun addRecentEmoji(emoji: String) {
        context.emojiDataStore.edit { preferences ->
            val currentString = preferences[RECENT_EMOJIS_KEY] ?: ""
            val currentList = if (currentString.isEmpty()) {
                emptyList()
            } else {
                currentString.split(",")
            }
            
            // Remove if already exists (to move to front)
            val filtered = currentList.filter { it != emoji }
            
            // Add new emoji at the beginning
            val newList = listOf(emoji) + filtered
            
            // Take only max allowed
            val limited = newList.take(MAX_RECENT_EMOJIS)
            
            preferences[RECENT_EMOJIS_KEY] = limited.joinToString(",")
        }
    }
    
    suspend fun getRecentEmojis(): List<String> {
        return recentEmojis.first()
    }
}
