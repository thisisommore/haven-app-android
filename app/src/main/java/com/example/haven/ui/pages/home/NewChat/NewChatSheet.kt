package com.example.haven.ui.pages.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ── Design tokens ──────────────────────────────────────────────────────────────
private val CardCornerRadius = 40.dp
private val ButtonCornerRadius = 28.dp

private val TitleFontSize = 22.sp
private val ValueFontSize = 22.sp
private val LabelFontSize = 12.sp
private val ButtonFontSize = 22.sp

// Theme-aware colors
@Composable
private fun sheetBackgroundColor(): Color = MaterialTheme.colorScheme.surfaceContainer

@Composable
private fun cardBackgroundColor(): Color = MaterialTheme.colorScheme.surface

@Composable
private fun primaryAccentColor(): Color = MaterialTheme.colorScheme.primary

@Composable
private fun buttonAccentColor(): Color = MaterialTheme.colorScheme.primary

@Composable
private fun buttonContentColor(): Color = MaterialTheme.colorScheme.onPrimary

@Composable
private fun onSurfaceColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
private fun labelColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
private fun placeholderColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

@Composable
private fun dividerColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

/**
 * Main Join Channel Sheet component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinChannelSheet(
    controller: JoinChannelController,
    onDismiss: () -> Unit,
    onChannelJoined: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val activeSheet by controller.activeSheet.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBackgroundColor(),
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        modifier = modifier.heightIn(min = screenHeight * 0.93f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primaryAccentColor().copy(alpha = 0.4f))
            )
        }
    ) {
        when (activeSheet) {
            JoinSheetState.INVITE_INPUT -> {
                InviteInputContent(
                    controller = controller,
                    onDismiss = onDismiss,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
            JoinSheetState.PASSWORD_INPUT -> {
                PasswordInputContent(
                    controller = controller,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
            JoinSheetState.CONFIRMATION -> {
                ConfirmationContent(
                    controller = controller,
                    onChannelJoined = onChannelJoined,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun InviteInputContent(
    controller: JoinChannelController,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inviteLink by controller.inviteLink.collectAsStateWithLifecycle()
    val errorMessage by controller.errorMessage.collectAsStateWithLifecycle()
    val isLoading by controller.isLoading.collectAsStateWithLifecycle()
    val toastMessage by controller.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            delay(2000)
            controller.clearToast()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        Text(
            text = "Join Channel",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = primaryAccentColor(),
                fontSize = TitleFontSize
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp)
        )

        // ── Invite Link Input Card ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor())
                .padding(vertical = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
            ) {
                Text(
                    text = "Enter Invite Link",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor()
                )
                Spacer(modifier = Modifier.height(8.dp))

                val textStyle = TextStyle(
                    fontSize = ValueFontSize,
                    color = onSurfaceColor()
                )

                BasicTextField(
                    value = inviteLink,
                    onValueChange = controller::onInviteLinkChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(primaryAccentColor()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (inviteLink.isEmpty()) {
                                Text(
                                    "Paste channel invite link here...",
                                    style = textStyle.copy(color = placeholderColor())
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        // ── Error Message ────────────────────────────────────────────────────────
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Join Button ──────────────────────────────────────────────────────────
        Button(
            onClick = { controller.validateAndProceed() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(ButtonCornerRadius),
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonAccentColor(),
                contentColor = buttonContentColor()
            ),
            contentPadding = PaddingValues(horizontal = 24.dp),
            enabled = !isLoading && inviteLink.isNotBlank()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        color = buttonContentColor(),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Next",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = ButtonFontSize
                        )
                    )
                }
            }
        }

        // Toast message overlay
        toastMessage?.let { message ->
            ToastMessage(message = message)
        }
    }
}

@Composable
private fun PasswordInputContent(
    controller: JoinChannelController,
    modifier: Modifier = Modifier
) {
    val password by controller.password.collectAsStateWithLifecycle()
    val errorMessage by controller.errorMessage.collectAsStateWithLifecycle()
    val isLoading by controller.isLoading.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        Text(
            text = "Private Channel",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = primaryAccentColor(),
                fontSize = TitleFontSize
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp)
        )

        // ── Description ──────────────────────────────────────────────────────────
        Text(
            text = "This channel is password protected. Enter the password to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor(),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        )

        // ── Password Input Card ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor())
                .padding(vertical = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor()
                )
                Spacer(modifier = Modifier.height(8.dp))

                val textStyle = TextStyle(
                    fontSize = ValueFontSize,
                    color = onSurfaceColor()
                )

                BasicTextField(
                    value = password,
                    onValueChange = controller::onPasswordChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(primaryAccentColor()),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (password.isEmpty()) {
                                Text(
                                    "Enter password",
                                    style = textStyle.copy(color = placeholderColor())
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        // ── Error Message ────────────────────────────────────────────────────────
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Buttons ───────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = { controller.goBack() },
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = labelColor())
            }

            Button(
                onClick = { controller.validatePassword() },
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                shape = RoundedCornerShape(ButtonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonAccentColor(),
                    contentColor = buttonContentColor()
                ),
                enabled = !isLoading && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = buttonContentColor(),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Confirm",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = ButtonFontSize
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmationContent(
    controller: JoinChannelController,
    onChannelJoined: () -> Unit,
    modifier: Modifier = Modifier
) {
    val channelData by controller.channelData.collectAsStateWithLifecycle()
    val inviteLink by controller.inviteLink.collectAsStateWithLifecycle()
    val enableDM by controller.enableDM.collectAsStateWithLifecycle()
    val isLoading by controller.isLoading.collectAsStateWithLifecycle()
    val isJoining by controller.isJoining.collectAsStateWithLifecycle()
    val errorMessage by controller.errorMessage.collectAsStateWithLifecycle()
    val joinSuccess by controller.joinSuccess.collectAsStateWithLifecycle()

    LaunchedEffect(joinSuccess) {
        if (joinSuccess) {
            onChannelJoined()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        Text(
            text = "Confirm Channel",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = primaryAccentColor(),
                fontSize = TitleFontSize
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp)
        )

        // ── Channel Details Card ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor())
                .padding(vertical = 22.dp)
        ) {
            // Channel Name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = ValueFontSize),
                    color = onSurfaceColor()
                )
                Text(
                    text = channelData?.name ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = ValueFontSize),
                    color = labelColor(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            CardDivider()

            // URL
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
            ) {
                Text(
                    text = "URL",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor()
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = inviteLink,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    ),
                    color = onSurfaceColor(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            CardDivider()

            // Enable DM Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable DM",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = ButtonFontSize),
                    color = onSurfaceColor()
                )
                Switch(
                    checked = enableDM,
                    onCheckedChange = { if (!isJoining) controller.onEnableDMChange(it) },
                    enabled = !isJoining
                )
            }
        }

        // ── Error Message ────────────────────────────────────────────────────────
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        // ── Joining Progress ─────────────────────────────────────────────────────
        if (isJoining) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = primaryAccentColor(),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Joining channel...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Buttons ───────────────────────────────────────────────────────────────
        if (!isJoining) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = { controller.goBack() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = labelColor())
                }

                Button(
                    onClick = { controller.joinChannel() },
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp),
                    shape = RoundedCornerShape(ButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonAccentColor(),
                        contentColor = buttonContentColor()
                    ),
                    enabled = !isLoading
                ) {
                    Text(
                        "Join",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = ButtonFontSize
                        )
                    )
                }
            }
        }
    }
}

// ── Reusable card divider ──────────────────────────────────────────────────────
@Composable
private fun CardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        thickness = 1.dp,
        color = dividerColor()
    )
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
                .background(primaryAccentColor(), RoundedCornerShape(25.dp))
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
