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

private val Context.reactionsDataStore: DataStore<Preferences> by preferencesDataStore(name = "recent_reactions")

class RecentReactionsStore(private val context: Context) {

    companion object {
        private val RECENT_REACTIONS_KEY = stringPreferencesKey("recent_reactions")
        private const val MAX_RECENT_REACTIONS = 20
    }

    val recentReactions: Flow<List<String>> = context.reactionsDataStore.data
        .map { preferences ->
            val reactionsString = preferences[RECENT_REACTIONS_KEY] ?: ""
            if (reactionsString.isEmpty()) {
                emptyList()
            } else {
                reactionsString.split(",").filter { it.isNotEmpty() }
            }
        }

    suspend fun addRecentReaction(emoji: String) {
        context.reactionsDataStore.edit { preferences ->
            val currentString = preferences[RECENT_REACTIONS_KEY] ?: ""
            val currentList = if (currentString.isEmpty()) {
                emptyList()
            } else {
                currentString.split(",")
            }

            // Remove if already exists (to move to front)
            val filtered = currentList.filter { it != emoji }

            // Add new reaction at the beginning
            val newList = listOf(emoji) + filtered

            // Take only max allowed
            val limited = newList.take(MAX_RECENT_REACTIONS)

            preferences[RECENT_REACTIONS_KEY] = limited.joinToString(",")
        }
    }

    suspend fun getRecentReactions(): List<String> {
        return recentReactions.first()
    }
}
