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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens ──────────────────────────────────────────────────────────────
private val CardCornerRadius = 40.dp
private val ButtonCornerRadius = 28.dp

private val TitleFontSize = 22.sp
private val ValueFontSize = 22.sp
private val LabelFontSize = 12.sp
private val ButtonFontSize = 22.sp
private const val MAX_NICKNAME_LENGTH = 24

/**
 * Nickname Sheet - Allows users to set their DM nickname
 * Mirrors iOS NicknamePickerSheet functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameSheet(
    codename: String,
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var nickname by remember { mutableStateOf(currentNickname) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load current nickname on appear
    LaunchedEffect(Unit) {
        nickname = currentNickname
    }

    val displayName = remember(nickname, codename) {
        if (nickname.isEmpty()) {
            codename
        } else {
            val truncatedNick = if (nickname.length > 10) {
                nickname.take(10) + "…"
            } else {
                nickname
            }
            "$truncatedNick aka $codename"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
        }
    ) {
        val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
        val onSurfaceColor = MaterialTheme.colorScheme.onSurface
        val primaryAccentColor = MaterialTheme.colorScheme.primary
        val cardBackgroundColor = MaterialTheme.colorScheme.surface
        val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        val havenColor = MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
        ) {
            // ── Header ───────────────────────────────────────────────────────────────
            Text(
                text = "DM Nickname",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = primaryAccentColor,
                    fontSize = TitleFontSize
                ),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp)
            )

            // ── Display Name Preview ────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Display Name",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = ValueFontSize),
                    color = onSurfaceColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Nickname Input Card ──────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CardCornerRadius))
                    .background(cardBackgroundColor)
                    .padding(vertical = 22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 26.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nickname",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                            color = labelColor
                        )
                        Text(
                            text = "${nickname.length}/$MAX_NICKNAME_LENGTH",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                            color = if (nickname.length > MAX_NICKNAME_LENGTH) {
                                MaterialTheme.colorScheme.error
                            } else {
                                labelColor
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val textStyle = TextStyle(
                        fontSize = ValueFontSize,
                        color = onSurfaceColor
                    )

                    BasicTextField(
                        value = nickname,
                        onValueChange = {
                            if (it.length <= MAX_NICKNAME_LENGTH) {
                                nickname = it
                                errorMessage = null
                            }
                        },
                        textStyle = textStyle,
                        cursorBrush = SolidColor(primaryAccentColor),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box {
                                if (nickname.isEmpty()) {
                                    Text(
                                        text = "Enter nickname",
                                        style = textStyle.copy(color = placeholderColor)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }

            // ── Truncation Warning ───────────────────────────────────────────────────
            if (nickname.length > 10) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = havenColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Nickname will be truncated to 10 chars in display",
                        style = MaterialTheme.typography.bodySmall,
                        color = havenColor
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

            // ── Info Section ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoRow(
                    icon = Icons.Default.Info,
                    text = "This nickname applies to Direct Messages only.",
                    isBold = true
                )
                InfoRow(
                    icon = Icons.Default.Info,
                    text = "Your nickname is sent with every DM message. Recipients will see this instead of your codename."
                )
                InfoRow(
                    icon = Icons.Default.Info,
                    text = "For channels, set your nickname in each channel's settings."
                )
                InfoRow(
                    icon = Icons.Default.Info,
                    text = "Nicknames longer than 10 chars are truncated in display to prevent scams."
                )
                InfoRow(
                    icon = Icons.Default.Info,
                    text = "Leave empty to use your codename."
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Buttons ───────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = labelColor)
                }

                Button(
                    onClick = {
                        if (nickname.length <= MAX_NICKNAME_LENGTH) {
                            onSave(nickname)
                            onDismiss()
                        } else {
                            errorMessage = "Nickname too long"
                        }
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp),
                    shape = RoundedCornerShape(ButtonCornerRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAccentColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        "Save",
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
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isBold: Boolean = false
) {
    val havenColor = MaterialTheme.colorScheme.primary
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = havenColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = if (isBold) {
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
