package com.example.haven.ui.pages.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haven.ui.pages.home.ChatRowView
import com.example.haven.ui.views.EmptyChatsState
import com.example.haven.xxdk.XXDK
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
    val search by controller.searchQuery.collectAsState()
    val chats by controller.filteredChats.collectAsState(initial = emptyList())
    val isLoading = !isSetupComplete && statusPercentage != 100
    val context = LocalContext.current

    // Menu / logout state
    var showMenu by remember { mutableStateOf(false) }
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
            // ── Header: gradient background ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainer
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 20.dp)
                ) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Top bar: title + loading + menu ─────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 38.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (statusPercentage < 100) {
                                CircularWavyProgressIndicator(
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Overflow / menu icon
                        val primaryColor = MaterialTheme.colorScheme.primary
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                // Custom thin plus icon
                                Canvas(modifier = Modifier.size(50.dp)) {
                                    val strokeWidth = 1.5.dp.toPx()
                                    val centerX = size.width / 2
                                    val centerY = size.height / 2
                                    val lineLength = size.minDimension * 0.35f
                                    
                                    // Horizontal line
                                    drawLine(
                                        color = primaryColor,
                                        start = androidx.compose.ui.geometry.Offset(centerX - lineLength, centerY),
                                        end = androidx.compose.ui.geometry.Offset(centerX + lineLength, centerY),
                                        strokeWidth = strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                    // Vertical line
                                    drawLine(
                                        color = primaryColor,
                                        start = androidx.compose.ui.geometry.Offset(centerX, centerY - lineLength),
                                        end = androidx.compose.ui.geometry.Offset(centerX, centerY + lineLength),
                                        strokeWidth = strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Join Channel") },
                                    onClick = {
                                        showMenu = false
                                        showJoinChannelSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Create Space") },
                                    onClick = {
                                        showMenu = false
                                        showCreateSpaceSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("New Chat") },
                                    onClick = {
                                        showMenu = false
                                        onNewChat()
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                DropdownMenuItem(
                                    text = { Text("Nickname") },
                                    onClick = {
                                        showMenu = false
                                        showNicknameSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export") },
                                    onClick = {
                                        showMenu = false
                                        showExportIdentitySheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("My QR") },
                                    onClick = {
                                        showMenu = false
                                        showQRCodeSheet = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Scan QR") },
                                    onClick = {
                                        showMenu = false
                                        showQRScannerSheet = true
                                    }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Logout",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        showMenu = false
                                        showLogoutConfirm = true
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Search bar: pill-shaped with icon ───────────
                    TextField(
                        value = search,
                        onValueChange = { controller.onSearchChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        placeholder = {
                            Text(
                                "Search",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 22.sp
                                )
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

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
            if (dmClient != null) {
                QRCodeSheet(
                    dmToken = dmClient.token,
                    pubKey = dmClient.publicKey,
                    codeset = xxdk.codeset,
                    onDismiss = { showQRCodeSheet = false }
                )
            }
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
            if (dmClient != null) {
                // Get current nickname from DirectMessage
                val currentNickname = (dmClient as? com.example.haven.xxdk.DirectMessage)?.getNickname() ?: ""
                NicknameSheet(
                    codename = xxdk.codename ?: "",
                    currentNickname = currentNickname,
                    onDismiss = { showNicknameSheet = false },
                    onSave = { nickname ->
                        try {
                            (dmClient as? com.example.haven.xxdk.DirectMessage)?.setNickname(nickname)
                        } catch (e: Exception) {
                            // Error handling
                        }
                    }
                )
            }
        }

        // Export Identity Sheet
        if (showExportIdentitySheet && xxdk != null) {
            ExportIdentitySheet(
                codename = xxdk.codename ?: "identity",
                onExport = { password, onSuccess, onError ->
                    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
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
