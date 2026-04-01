package com.example.haven.ui.pages.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.SpeakerNotesOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haven.data.model.ChatModel
import kotlinx.coroutines.delay

// ── Design tokens ──────────────────────────────────────────────────────────────
private val SheetBackground   = Color(0xFFF3EFFF)
private val CardBackground    = Color.White
private val CardCornerRadius  = 40.dp
private val ButtonCornerRadius = 28.dp

private val PrimaryPurple     = Color(0xFF6750A4)
private val DarkPurple        = Color(0xFF5D5387)
private val OnSurface         = Color(0xFF1D1B20)
private val LabelGray         = Color(0xFF79747E)
private val PlaceholderGray   = Color(0xFFCAC4D0)
private val DividerColor      = Color(0xFFEFEFEF)

private val TitleFontSize     = 22.sp
private val ValueFontSize     = 22.sp
private val LabelFontSize     = 12.sp
private val ButtonFontSize    = 22.sp

/**
 * Main Channel Options Sheet component – pixel-perfect to the design spec.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelOptionsSheet(
    chat: ChatModel,
    viewModel: ChannelOptionsViewModel,
    onDismiss: () -> Unit,
    onLeaveChannel: () -> Unit,
    onDeleteChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(chat) {
        viewModel.loadChannelOptions(chat)
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        modifier = modifier.heightIn(min = screenHeight * 0.92f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PrimaryPurple.copy(alpha = 0.4f))
            )
        }
    ) {
        ChannelOptionsContent(
            chat = chat,
            viewModel = viewModel,
            onDismiss = onDismiss,
            onLeaveChannel = onLeaveChannel,
            onDeleteChat = onDeleteChat,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun ChannelOptionsContent(
    chat: ChatModel,
    viewModel: ChannelOptionsViewModel,
    onDismiss: () -> Unit,
    onLeaveChannel: () -> Unit,
    onDeleteChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDM = chat.dmToken != null
    val channelId = chat.channelId
    val isAdmin by viewModel.isAdmin.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isDMEnabled by viewModel.isDMEnabled.collectAsState()
    val shareUrlData by viewModel.shareUrlData.collectAsState()
    val channelNickname by viewModel.channelNickname.collectAsState()
    val mutedUsers by viewModel.mutedUsers.collectAsState()

    // Dialog states
    val showLeaveConfirmation by viewModel.showLeaveConfirmation.collectAsState()
    val showDeleteConfirmation by viewModel.showDeleteConfirmation.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val showImportDialog by viewModel.showImportDialog.collectAsState()

    // Toast
    val toastMessage by viewModel.toastMessage.collectAsState()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            delay(2000)
            viewModel.clearToast()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Title ──────────────────────────────────────────────────────────────
        Text(
            text = if (isDM) "DM Options" else "Channel Options",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = PrimaryPurple,
                fontSize = TitleFontSize
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 60.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryPurple)
            }
        } else {
            // ── Main Options Card ──────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CardCornerRadius))
                    .background(CardBackground)
                    .padding(vertical = 22.dp)
            ) {
                // Channel Name
                OptionItem(
                    label = if (isDM) "Name" else "Channel Name",
                    value = chat.name
                )

                CardDivider()

                // Your Nickname
                NicknameEditorItem(
                    nickname = channelNickname,
                    onNicknameChange = viewModel::updateChannelNickname
                )

                CardDivider()

                // Direct Messages Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Direct Messages",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = ButtonFontSize),
                        color = OnSurface
                    )
                    Switch(
                        checked = isDMEnabled,
                        onCheckedChange = viewModel::toggleDirectMessages
                    )
                }

                CardDivider()

                // Share URL Section
                android.util.Log.d("ChannelOptionsUI", "shareUrlData: ${shareUrlData?.url ?: "NULL"}")
                shareUrlData?.let { data ->
                    android.util.Log.d("ChannelOptionsUI", "Displaying share URL: ${data.url}")
                    // URL Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { 
                                // Share the URL
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(android.content.Intent.EXTRA_TEXT, data.url)
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                                // Need context to start activity - would need to be handled differently
                            }
                            .padding(horizontal = 26.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            data.url,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = PrimaryPurple,
                                fontSize = ButtonFontSize
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                            contentDescription = "Share Link",
                            tint = LabelGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    // Password Row (if available)
                    data.password?.let { password ->
                        if (password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 26.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Password",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                                        color = LabelGray
                                    )
                                    Text(
                                        password,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = OnSurface
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                // Copy password button would go here
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Action Buttons ─────────────────────────────────────────────────
            if (!isDM && channelId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val isAdminAction = isAdmin
                val buttonText =
                    if (isAdminAction) "Export Channel Key" else "Import Channel Key"
                val buttonIcon =
                    if (isAdminAction) Icons.Outlined.FileUpload else Icons.Outlined.Key

                Button(
                    onClick = {
                        if (isAdminAction) viewModel.showExportDialog()
                        else viewModel.showImportDialog()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(ButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPurple),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            buttonText,
                            modifier = Modifier.align(Alignment.CenterStart),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = ButtonFontSize
                            )
                        )
                        Icon(
                            imageVector = buttonIcon,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(22.dp)
                                .then(if (!isAdminAction) Modifier.rotate(90f) else Modifier)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Leave / Delete Button
            Button(
                onClick = {
                    if (isDM) viewModel.showDeleteConfirmation()
                    else viewModel.showLeaveConfirmation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(ButtonCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    if (isDM) "Delete Chat" else "Leave Channel",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = ButtonFontSize
                    )
                )
            }

            // ── Muted Users Section ────────────────────────────────────────────
            if (!isDM && channelId != null && isAdmin && mutedUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Muted Users",
                    style = MaterialTheme.typography.labelLarge,
                    color = PrimaryPurple,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp)
                )
                mutedUsers.forEach { user ->
                    MutedUserRow(user = user, onUnmute = { viewModel.unmuteUser(user.pubKey) })
                }
            }
        }

        // Toast message overlay
        val currentToast = toastMessage
        if (currentToast != null) {
            ToastMessage(message = currentToast)
        }
    }

    // ── Dialogs ────────────────────────────────────────────────────────────────
    if (showLeaveConfirmation) {
        LeaveChannelDialog(
            channelName = chat.name,
            onConfirm = {
                viewModel.dismissLeaveConfirmation()
                viewModel.leaveChannel(onLeaveChannel)
            },
            onDismiss = viewModel::dismissLeaveConfirmation
        )
    }

    if (showDeleteConfirmation) {
        DeleteChatDialog(
            chatName = chat.name,
            onConfirm = {
                viewModel.dismissDeleteConfirmation()
                viewModel.deleteChat(onDeleteChat)
            },
            onDismiss = viewModel::dismissDeleteConfirmation
        )
    }

    if (showExportDialog) {
        ExportKeyDialog(viewModel = viewModel, onDismiss = viewModel::dismissExportDialog)
    }

    if (showImportDialog) {
        ImportKeyDialog(viewModel = viewModel, onDismiss = viewModel::dismissImportDialog)
    }
}

// ── Reusable card divider ──────────────────────────────────────────────────────
@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        thickness = 1.dp,
        color = DividerColor
    )
}

// ── Static label + value row ───────────────────────────────────────────────────
@Composable
private fun OptionItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
            color = LabelGray
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = ValueFontSize),
            color = OnSurface
        )
    }
}

// ── Nickname editor using BasicTextField for zero internal padding ──────────────
@Composable
private fun NicknameEditorItem(
    nickname: String,
    onNicknameChange: (String) -> Unit
) {
    val textStyle = TextStyle(
        fontSize = ValueFontSize,
        color = OnSurface
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 26.dp)
    ) {
        Text(
            text = "Your Nickname",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
            color = LabelGray
        )
        Spacer(modifier = Modifier.height(4.dp))

        BasicTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            textStyle = textStyle,
            singleLine = true,
            cursorBrush = SolidColor(PrimaryPurple),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (nickname.isEmpty()) {
                        Text(
                            "Enter nickname (max 24 chars)",
                            style = textStyle.copy(color = PlaceholderGray)
                        )
                    }
                    innerTextField()
                }
            }
        )

        if (nickname.length > 10) {
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Nickname will be truncated to 10 chars in display",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red
                )
            }
        }
    }
}

// ── Muted user row ─────────────────────────────────────────────────────────────
@Composable
private fun MutedUserRow(
    user: MutedUser,
    onUnmute: () -> Unit
) {
    val displayName =
        user.codename ?: user.pubKey.joinToString("") { "%02x".format(it) }.take(16)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.SpeakerNotesOff,
                contentDescription = null,
                tint = LabelGray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurface
            )
        }
        TextButton(onClick = onUnmute) {
            Text("Unmute", color = PrimaryPurple)
        }
    }
}

// ── Toast ──────────────────────────────────────────────────────────────────────
@Composable
private fun ToastMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .background(PrimaryPurple, RoundedCornerShape(25.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, color = Color.White, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ── Confirmation Dialogs ───────────────────────────────────────────────────────
@Composable
private fun LeaveChannelDialog(
    channelName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Leave Channel") },
        text = { Text("Are you sure you want to leave \"$channelName\"?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Leave")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteChatDialog(
    chatName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Chat") },
        text = { Text("Are you sure you want to delete this chat with \"$chatName\"?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}