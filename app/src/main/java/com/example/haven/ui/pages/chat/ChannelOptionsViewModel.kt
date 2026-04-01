package com.example.haven.ui.pages.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.DatabaseRepository
import com.example.haven.data.model.ChatModel
import com.example.haven.xxdk.XXDK
import com.example.haven.xxdk.ShareUrlData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class representing a muted user
 */
data class MutedUser(
    val pubKey: ByteArray,
    val codename: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MutedUser
        return pubKey.contentEquals(other.pubKey)
    }

    override fun hashCode(): Int = pubKey.contentHashCode()
}

/**
 * ViewModel for Channel Options sheet
 * Mirrors iOS ChannelOptionsController
 */
class ChannelOptionsViewModel(
    private val repository: DatabaseRepository,
    private val xxdk: XXDK
) : ViewModel() {

    // UI State
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isDMEnabled = MutableStateFlow(false)
    val isDMEnabled: StateFlow<Boolean> = _isDMEnabled.asStateFlow()

    private val _shareUrlData = MutableStateFlow<ShareUrlData?>(null)
    val shareUrlData: StateFlow<ShareUrlData?> = _shareUrlData.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    private val _mutedUsers = MutableStateFlow<List<MutedUser>>(emptyList())
    val mutedUsers: StateFlow<List<MutedUser>> = _mutedUsers.asStateFlow()

    private val _channelNickname = MutableStateFlow("")
    val channelNickname: StateFlow<String> = _channelNickname.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _showLeaveConfirmation = MutableStateFlow(false)
    val showLeaveConfirmation: StateFlow<Boolean> = _showLeaveConfirmation.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow(false)
    val showDeleteConfirmation: StateFlow<Boolean> = _showDeleteConfirmation.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _showImportDialog = MutableStateFlow(false)
    val showImportDialog: StateFlow<Boolean> = _showImportDialog.asStateFlow()

    private val _exportKeyContent = MutableStateFlow<String?>(null)
    val exportKeyContent: StateFlow<String?> = _exportKeyContent.asStateFlow()

    private val _exportError = MutableStateFlow<String?>(null)
    val exportError: StateFlow<String?> = _exportError.asStateFlow()

    private val _importError = MutableStateFlow<String?>(null)
    val importError: StateFlow<String?> = _importError.asStateFlow()

    // Current chat being viewed
    private var currentChat: ChatModel? = null

    /**
     * Load channel options data
     */
    fun loadChannelOptions(chat: ChatModel) {
        viewModelScope.launch {
            _isLoading.value = true
            currentChat = chat
            _isAdmin.value = chat.isAdmin

            val channelId = chat.channelId
            if (channelId != null) {
                // Load DM enabled status
                _isDMEnabled.value = xxdk.channel.areDMsEnabled(channelId)

                // Load share URL
                val shareData = xxdk.channel.getShareUrl(channelId)
                _shareUrlData.value = shareData

                // Load muted users
                loadMutedUsers(channelId)

                // Load channel nickname
                _channelNickname.value = xxdk.channel.getChannelNickname(channelId)
            }

            _isLoading.value = false
        }
    }

    /**
     * Load muted users for the channel
     */
    private suspend fun loadMutedUsers(channelId: String) {
        try {
            val mutedPubKeys = xxdk.channel.getMutedUsers(channelId)
            val users = mutedPubKeys.map { pubKey ->
                val sender = repository.getSenderByPubKey(pubKey)
                MutedUser(pubKey, sender?.codename)
            }
            _mutedUsers.value = users
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to load muted users: ${e.message}")
        }
    }

    /**
     * Toggle direct messages enabled/disabled
     */
    fun toggleDirectMessages(enabled: Boolean) {
        val channelId = currentChat?.channelId ?: return
        viewModelScope.launch {
            try {
                if (enabled) {
                    xxdk.channel.enableDirectMessages(channelId)
                } else {
                    xxdk.channel.disableDirectMessages(channelId)
                }
                _isDMEnabled.value = enabled
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to toggle DM: ${e.message}")
                // Revert on failure
                _isDMEnabled.value = !enabled
                showToast("Failed to update DM setting")
            }
        }
    }

    /**
     * Update channel nickname with character limit
     */
    fun updateChannelNickname(newValue: String) {
        if (newValue.length > MAX_NICKNAME_LENGTH) {
            _channelNickname.value = newValue.take(MAX_NICKNAME_LENGTH)
        } else {
            _channelNickname.value = newValue
        }
    }

    /**
     * Save the channel nickname
     */
    fun saveNickname() {
        val channelId = currentChat?.channelId ?: return
        viewModelScope.launch {
            try {
                xxdk.channel.setChannelNickname(channelId, _channelNickname.value)
                showToast("Nickname saved")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to save nickname: ${e.message}")
                showToast("Failed to save nickname")
            }
        }
    }

    /**
     * Unmute a user
     */
    fun unmuteUser(pubKey: ByteArray) {
        val channelId = currentChat?.channelId ?: return
        viewModelScope.launch {
            try {
                xxdk.channel.muteUser(channelId, pubKey, mute = false)
                loadMutedUsers(channelId)
                showToast("User unmuted")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to unmute user: ${e.message}")
                showToast("Failed to unmute user")
            }
        }
    }

    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Password") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        showToast("Password copied")
    }

    /**
     * Export channel admin key
     */
    fun exportChannelKey(password: String) {
        val channelId = currentChat?.channelId ?: return
        viewModelScope.launch {
            try {
                val key = xxdk.channel.exportChannelAdminKey(channelId, password)
                _exportKeyContent.value = key
                _exportError.value = null
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to export key: ${e.message}")
                _exportError.value = "Failed to export key: ${e.message}"
            }
        }
    }

    /**
     * Import channel admin key
     */
    fun importChannelKey(keyContent: String, password: String, onSuccess: () -> Unit) {
        val channelId = currentChat?.channelId ?: return
        val chatId = currentChat?.id
        viewModelScope.launch {
            try {
                xxdk.channel.importChannelAdminKey(channelId, password, keyContent)

                // Update admin status in database
                chatId?.let { id ->
                    val chat = repository.getChatById(id)
                    chat?.let {
                        val updatedChat = it.copy(isAdmin = true)
                        repository.updateChat(updatedChat)
                    }
                }

                _isAdmin.value = true
                _importError.value = null
                showToast("Key imported successfully")
                onSuccess()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to import key: ${e.message}")
                _importError.value = "Import failed: ${e.message}"
            }
        }
    }

    /**
     * Show leave channel confirmation
     */
    fun showLeaveConfirmation() {
        _showLeaveConfirmation.value = true
    }

    /**
     * Dismiss leave channel confirmation
     */
    fun dismissLeaveConfirmation() {
        _showLeaveConfirmation.value = false
    }

    /**
     * Show delete chat confirmation
     */
    fun showDeleteConfirmation() {
        _showDeleteConfirmation.value = true
    }

    /**
     * Dismiss delete chat confirmation
     */
    fun dismissDeleteConfirmation() {
        _showDeleteConfirmation.value = false
    }

    /**
     * Show export key dialog
     */
    fun showExportDialog() {
        _showExportDialog.value = true
        _exportKeyContent.value = null
        _exportError.value = null
    }

    /**
     * Dismiss export key dialog
     */
    fun dismissExportDialog() {
        _showExportDialog.value = false
        _exportKeyContent.value = null
        _exportError.value = null
    }

    /**
     * Show import key dialog
     */
    fun showImportDialog() {
        _showImportDialog.value = true
        _importError.value = null
    }

    /**
     * Dismiss import key dialog
     */
    fun dismissImportDialog() {
        _showImportDialog.value = false
        _importError.value = null
    }

    /**
     * Show toast message
     */
    fun showToast(message: String) {
        _toastMessage.value = message
    }

    /**
     * Clear toast message
     */
    fun clearToast() {
        _toastMessage.value = null
    }

    /**
     * Leave the channel and delete from database
     */
    fun leaveChannel(onComplete: () -> Unit) {
        val chat = currentChat ?: return
        viewModelScope.launch {
            try {
                // Leave channel in XXDK
                chat.channelId?.let { channelId ->
                    xxdk.channel.leaveChannel(channelId)
                }
                // Delete from database
                repository.deleteChat(chat)
                onComplete()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to leave channel: ${e.message}")
                showToast("Failed to leave channel")
            }
        }
    }

    /**
     * Delete the DM chat
     */
    fun deleteChat(onComplete: () -> Unit) {
        val chat = currentChat ?: return
        viewModelScope.launch {
            try {
                repository.deleteChat(chat)
                onComplete()
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Failed to delete chat: ${e.message}")
                showToast("Failed to delete chat")
            }
        }
    }

    companion object {
        private const val TAG = "ChannelOptionsViewModel"
        private const val MAX_NICKNAME_LENGTH = 24

        /**
         * Factory for creating ViewModel with dependencies
         */
        fun createFactory(
            context: Context,
            xxdk: XXDK
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ChannelOptionsViewModel::class.java)) {
                    return ChannelOptionsViewModel(
                        DatabaseRepository(context.applicationContext),
                        xxdk
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
