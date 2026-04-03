package com.example.haven.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens ──────────────────────────────────────────────────────────────
private val CardCornerRadius = 40.dp
private val ButtonCornerRadius = 28.dp

private val TitleFontSize = 22.sp
private val ValueFontSize = 22.sp
private val LabelFontSize = 12.sp
private val ButtonFontSize = 22.sp

/**
 * Join Channel Confirmation Sheet
 * Mirrors iOS JoinChannelConfirmationSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinChannelConfirmationSheet(
    channelName: String,
    channelURL: String,
    isJoining: Boolean,
    onConfirm: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var enableDM by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { if (!isJoining) onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        modifier = modifier.heightIn(min = screenHeight * 0.6f),
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
        JoinChannelConfirmationContent(
            channelName = channelName,
            channelURL = channelURL,
            enableDM = enableDM,
            onEnableDMChange = { enableDM = it },
            isJoining = isJoining,
            onConfirm = { onConfirm(enableDM) },
            onDismiss = onDismiss,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun JoinChannelConfirmationContent(
    channelName: String,
    channelURL: String,
    enableDM: Boolean,
    onEnableDMChange: (Boolean) -> Unit,
    isJoining: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryAccentColor = MaterialTheme.colorScheme.primary
    val cardBackgroundColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Confirm Channel",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = primaryAccentColor,
                    fontSize = TitleFontSize
                )
            )
            IconButton(
                onClick = onDismiss,
                enabled = !isJoining
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel"
                )
            }
        }

        // ── Channel Details Card ─────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor)
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
                    color = onSurfaceColor
                )
                Text(
                    text = channelName,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = ValueFontSize),
                    color = labelColor,
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
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = channelURL,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = onSurfaceColor,
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
                    color = onSurfaceColor
                )
                Switch(
                    checked = enableDM,
                    onCheckedChange = onEnableDMChange,
                    enabled = !isJoining
                )
            }
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
                    color = primaryAccentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Joining channel...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = labelColor
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
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = labelColor)
                }

                Button(
                    onClick = onConfirm,
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
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    )
}
