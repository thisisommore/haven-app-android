package com.example.haven.ui.pages.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.haven.data.DatabaseRepository
import com.example.haven.data.model.ChatModel
import com.example.haven.xxdk.ChannelInfoJson
import com.example.haven.xxdk.PrivacyLevel
import com.example.haven.xxdk.XXDK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Enum representing the active sheet in the join channel flow
 */
enum class JoinSheetState {
    INVITE_INPUT,
    PASSWORD_INPUT,
    CONFIRMATION
}

/**
 * Controller for Join Channel sheet
 * Matches iOS NewChatSheet logic
 */
class JoinChannelController(
    private val xxdk: XXDK,
    private val repository: DatabaseRepository
) : ViewModel() {

    // Sheet navigation state
    private val _activeSheet = MutableStateFlow(JoinSheetState.INVITE_INPUT)
    val activeSheet: StateFlow<JoinSheetState> = _activeSheet.asStateFlow()

    // Invite link input
    private val _inviteLink = MutableStateFlow("")
    val inviteLink: StateFlow<String> = _inviteLink.asStateFlow()

    // Password input (for private channels)
    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Channel data fetched from URL
    private val _channelData = MutableStateFlow<ChannelInfoJson?>(null)
    val channelData: StateFlow<ChannelInfoJson?> = _channelData.asStateFlow()

    // Enable DM toggle
    private val _enableDM = MutableStateFlow(false)
    val enableDM: StateFlow<Boolean> = _enableDM.asStateFlow()

    // Loading states
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining: StateFlow<Boolean> = _isJoining.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Join success
    private val _joinSuccess = MutableStateFlow(false)
    val joinSuccess: StateFlow<Boolean> = _joinSuccess.asStateFlow()

    // Store pretty print for private channels
    private var prettyPrint: String? = null
    private var isPrivateChannel = false

    /**
     * Update invite link
     */
    fun onInviteLinkChange(link: String) {
        _inviteLink.value = link
        _errorMessage.value = null
    }

    /**
     * Update password
     */
    fun onPasswordChange(pw: String) {
        _password.value = pw
        _errorMessage.value = null
    }

    /**
     * Update enable DM toggle
     */
    fun onEnableDMChange(enabled: Boolean) {
        _enableDM.value = enabled
    }

    /**
     * Clear toast message
     */
    fun clearToast() {
        _toastMessage.value = null
    }

    /**
     * Go back to previous sheet
     */
    fun goBack() {
        when (_activeSheet.value) {
            JoinSheetState.PASSWORD_INPUT -> {
                _activeSheet.value = JoinSheetState.INVITE_INPUT
                _password.value = ""
                _errorMessage.value = null
            }
            JoinSheetState.CONFIRMATION -> {
                if (isPrivateChannel) {
                    _activeSheet.value = JoinSheetState.PASSWORD_INPUT
                } else {
                    _activeSheet.value = JoinSheetState.INVITE_INPUT
                }
                _errorMessage.value = null
            }
            else -> { /* Already at first sheet */ }
        }
    }

    /**
     * Validate invite link and proceed to next step
     */
    fun validateAndProceed() {
        val trimmedLink = _inviteLink.value.trim()
        if (trimmedLink.isEmpty()) {
            _errorMessage.value = "Please enter an invite link"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val channel = xxdk.channel
                val privacyLevel = channel.getPrivacyLevel(trimmedLink)

                if (privacyLevel == PrivacyLevel.SECRET) {
                    // Private channel - need password
                    isPrivateChannel = true
                    _activeSheet.value = JoinSheetState.PASSWORD_INPUT
                } else {
                    // Public channel - fetch channel data directly
                    val channelInfo = channel.getChannelFrom(trimmedLink)
                    if (channelInfo != null) {
                        _channelData.value = channelInfo
                        _activeSheet.value = JoinSheetState.CONFIRMATION
                    } else {
                        _errorMessage.value = "Failed to fetch channel information"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get channel: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Validate password for private channel
     */
    fun validatePassword() {
        val pw = _password.value
        if (pw.isEmpty()) {
            _errorMessage.value = "Please enter a password"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val channel = xxdk.channel
                val trimmedLink = _inviteLink.value.trim()
                
                // Decode private URL with password
                val pp = channel.decodePrivateURL(trimmedLink, pw)
                prettyPrint = pp
                
                // Get channel info
                val channelInfo = channel.getPrivateChannelFrom(trimmedLink, pw)
                if (channelInfo != null) {
                    _channelData.value = channelInfo
                    _activeSheet.value = JoinSheetState.CONFIRMATION
                } else {
                    _errorMessage.value = "Failed to decrypt channel information"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to decrypt channel: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Join the channel
     */
    fun joinChannel() {
        val channelInfo = _channelData.value ?: return
        _isJoining.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val channel = xxdk.channel
                val trimmedLink = _inviteLink.value.trim()
                
                // Join the channel
                val joinedChannel = if (prettyPrint != null) {
                    // Private channel
                    channel.joinChannel(prettyPrint!!)
                } else {
                    // Public channel
                    channel.joinChannelFromURL(trimmedLink)
                }

                if (joinedChannel != null) {
                    // Enable/disable DM based on toggle
                    if (_enableDM.value) {
                        channel.enableDirectMessages(joinedChannel.channelId)
                    } else {
                        channel.disableDirectMessages(joinedChannel.channelId)
                    }

                    // Save to database
                    val newChat = ChatModel(
                        name = joinedChannel.name,
                        channelId = joinedChannel.channelId,
                        isSecret = isPrivateChannel
                    )
                    repository.insertChat(newChat)

                    _joinSuccess.value = true
                } else {
                    _errorMessage.value = "Failed to join channel"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to join channel: ${e.message}"
            } finally {
                _isJoining.value = false
            }
        }
    }

    /**
     * Reset the view model state
     */
    fun reset() {
        _activeSheet.value = JoinSheetState.INVITE_INPUT
        _inviteLink.value = ""
        _password.value = ""
        _channelData.value = null
        _enableDM.value = false
        _isLoading.value = false
        _isJoining.value = false
        _errorMessage.value = null
        _toastMessage.value = null
        _joinSuccess.value = false
        prettyPrint = null
        isPrivateChannel = false
    }

    /**
     * Factory for creating Controller with dependencies
     * Matches iOS pattern
     */
    class Factory(
        private val context: Context,
        private val xxdk: XXDK
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(JoinChannelController::class.java)) {
                return JoinChannelController(
                    xxdk,
                    DatabaseRepository(context.applicationContext)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
