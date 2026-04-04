package com.example.haven.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.haven.ui.views.EmptyChatsState
import com.example.haven.xxdk.XXDK
import kotlinx.coroutines.launch

@Composable
internal fun HomeView(
    controller: HomePageController,
    xxdk: XXDK? = null,
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit = {},
    onLogout: () -> Unit = {},
    statusPercentage: Int = 0,
    isSetupComplete: Boolean = false,
    modifier: Modifier = Modifier
) {
    val search by controller.searchQuery.collectAsStateWithLifecycle()
    val chats by controller.filteredChats.collectAsState(initial = emptyList())
    val showSyncProgress = !isSetupComplete && statusPercentage != 100
    val coroutineScope = rememberCoroutineScope()

    // Menu / logout state
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showJoinChannelSheet by remember { mutableStateOf(false) }
    var showCreateSpaceSheet by remember { mutableStateOf(false) }
    var showQRCodeSheet by remember { mutableStateOf(false) }
    var showQRScannerSheet by remember { mutableStateOf(false) }
    var showNicknameSheet by remember { mutableStateOf(false) }
    var showExportIdentitySheet by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    // ── Logout confirmation dialog ──────────────────────────────────
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.Normal
                )
            },
            text = {
                Text(
                    "If you haven't backed up your identity, you will lose access to it permanently. Are you sure you want to logout?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            },
            shape = RoundedCornerShape(20.dp),
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        isLoggingOut = true
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutConfirm = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // ── Logout loading overlay ──────────────────────────────────────
    if (isLoggingOut) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(enabled = false) { },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(30.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text = if (xxdk != null) xxdk.status else "Logging out...",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeHeader(
                search = search,
                onSearchChange = controller::onSearchChange,
                showSyncProgress = showSyncProgress,
                codename = xxdk?.codename.orEmpty(),
                onJoinChannel = { showJoinChannelSheet = true },
                onCreateSpace = { showCreateSpaceSheet = true },
                onNewChat = onNewChat,
                onScanQR = { showQRScannerSheet = true },
                onNickname = { showNicknameSheet = true },
                onExport = { showExportIdentitySheet = true },
                onShowQRCode = { showQRCodeSheet = true },
                onLogout = { showLogoutConfirm = true }
            )

            // ── Chat list: white surface, rounded top corners ───────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),

                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (chats.isEmpty()) {
                    EmptyChatsState(modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
                    ) {
                        items(
                            items = chats,
                            key = { it.id }
                        ) { chat ->
                            ChatRowView(
                                chat = chat,
                                onClick = {
                                    controller.clearUnreadCount(chat.id)
                                    onOpenChat(chat.id)
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                            val isLastItem = chat.id == chats.lastOrNull()?.id
                            if (!isLastItem) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 20.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
                                    thickness = 0.5.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Join Channel Sheet
        if (showJoinChannelSheet && xxdk != null) {
            val joinChannelController: JoinChannelController = viewModel(
                factory = JoinChannelController.Factory(
                    context = LocalContext.current,
                    xxdk = xxdk
                )
            )
            JoinChannelSheet(
                controller = joinChannelController,
                onDismiss = {
                    joinChannelController.reset()
                    showJoinChannelSheet = false
                },
                onChannelJoined = {
                    joinChannelController.reset()
                    showJoinChannelSheet = false
                }
            )
        }
        
        // QR Code Sheet - Show My QR
        if (showQRCodeSheet && xxdk != null) {
            val dmClient = xxdk.dm
            QRCodeSheet(
                dmToken = dmClient.token,
                pubKey = dmClient.publicKey,
                codeset = xxdk.codeset,
                onDismiss = { showQRCodeSheet = false }
            )
        }
        
        // QR Scanner Sheet - Scan QR
        if (showQRScannerSheet) {
            QRScannerSheet(
                onCodeScanned = { code ->
                    // Handle scanned QR code
                    val qrData = com.example.haven.xxdk.QRCodeUtils.parseQRCode(code)
                    if (qrData != null) {
                        controller.handleScannedQR(qrData)
                    }
                },
                onShowMyQR = {
                    showQRCodeSheet = true
                },
                onDismiss = { showQRScannerSheet = false }
            )
        }

        // Create Space Sheet
        if (showCreateSpaceSheet && xxdk != null) {
            CreateSpaceSheet(
                onDismiss = { showCreateSpaceSheet = false },
                onCreateSpace = { name, description, privacyLevel, enableDms, onSuccess, onError ->
                    val result = xxdk.channel.createChannel(
                        name = name,
                        description = description,
                        privacyLevel = privacyLevel,
                        enableDms = enableDms
                    )
                    if (result != null) {
                        // Save to database via ViewModel
                        controller.createChannel(
                            channelId = result.channelId,
                            name = result.name,
                            description = result.description,
                            isAdmin = true,
                            isSecret = privacyLevel == com.example.haven.xxdk.PrivacyLevel.SECRET
                        )
                        onSuccess(
                            CreatedChannelInfo(
                                channelId = result.channelId,
                                name = result.name,
                                description = result.description,
                                isSecret = privacyLevel == com.example.haven.xxdk.PrivacyLevel.SECRET
                            )
                        )
                    } else {
                        onError("Failed to create channel")
                    }
                }
            )
        }

        // Nickname Sheet
        if (showNicknameSheet && xxdk != null) {
            val dmClient = xxdk.dm
            val currentNickname = dmClient.getNickname()
            NicknameSheet(
                codename = xxdk.codename ?: "",
                currentNickname = currentNickname,
                onDismiss = { showNicknameSheet = false },
                onSave = { nickname ->
                    try {
                        dmClient.setNickname(nickname)
                    } catch (e: Exception) {
                        // Error handling
                    }
                }
            )
        }

        // Export Identity Sheet
        if (showExportIdentitySheet && xxdk != null) {
            ExportIdentitySheet(
                codename = xxdk.codename ?: "identity",
                onExport = { password, onSuccess, onError ->
                    coroutineScope.launch {
                        try {
                            val data = xxdk.exportIdentity(password)
                            onSuccess(data)
                        } catch (e: Exception) {
                            onError("Failed to export: ${e.message}")
                        }
                    }
                },
                onDismiss = { showExportIdentitySheet = false }
            )
        }
    }
}
