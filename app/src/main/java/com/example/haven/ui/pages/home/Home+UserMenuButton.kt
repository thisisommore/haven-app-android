package com.example.haven.ui.pages.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeHeader(
    search: String,
    onSearchChange: (String) -> Unit,
    showSyncProgress: Boolean,
    codename: String,
    onJoinChannel: () -> Unit,
    onCreateSpace: () -> Unit,
    onScanQR: () -> Unit,
    onNickname: () -> Unit,
    onExport: () -> Unit,
    onShowQRCode: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
                    if (showSyncProgress) {
                        CircularWavyProgressIndicator(
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserCodenameMenuChip(
                        codename = codename,
                        onNickname = onNickname,
                        onExport = onExport,
                        onShowQRCode = onShowQRCode,
                        onLogout = onLogout
                    )
                    PlusMenuButton(
                        onJoinChannel = onJoinChannel,
                        onCreateSpace = onCreateSpace,
                        onScanQR = onScanQR
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextField(
                value = search,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                placeholder = {
                    Text(
                        "Search",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.primary
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
}

@Composable
internal fun UserCodenameMenuChip(
    codename: String,
    onNickname: () -> Unit,
    onExport: () -> Unit,
    onShowQRCode: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val menuTitle = codename.trim().ifEmpty { "Your profile" }
    val chipLabel = remember(codename) { codename.toCodenameBadgeLabel() }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primary)
                .clickable { showMenu = true }
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chipLabel,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp
                ),
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            Text(
                text = menuTitle,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            DropdownMenuItem(
                text = { Text("Nickname") },
                onClick = {
                    showMenu = false
                    onNickname()
                }
            )
            DropdownMenuItem(
                text = { Text("Export") },
                onClick = {
                    showMenu = false
                    onExport()
                }
            )
            DropdownMenuItem(
                text = { Text("QR Code") },
                onClick = {
                    showMenu = false
                    onShowQRCode()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DropdownMenuItem(
                text = { Text("Logout") },
                onClick = {
                    showMenu = false
                    onLogout()
                },
                colors = MenuDefaults.itemColors(
                    textColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

@Composable
internal fun PlusMenuButton(
    onJoinChannel: () -> Unit,
    onCreateSpace: () -> Unit,
    onScanQR: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier) {
        IconButton(onClick = { showMenu = true }) {
            Canvas(modifier = Modifier.size(32.dp)) {
                val strokeWidth = 1.5.dp.toPx()
                val centerX = size.width / 2
                val centerY = size.height / 2
                val lineLength = size.minDimension * 0.35f

                drawLine(
                    color = primaryColor,
                    start = Offset(centerX - lineLength, centerY),
                    end = Offset(centerX + lineLength, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = primaryColor,
                    start = Offset(centerX, centerY - lineLength),
                    end = Offset(centerX, centerY + lineLength),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            shape = RoundedCornerShape(12.dp),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp
        ) {
            DropdownMenuItem(
                text = { Text("Join Channel") },
                onClick = {
                    showMenu = false
                    onJoinChannel()
                }
            )
            DropdownMenuItem(
                text = { Text("Create Space") },
                onClick = {
                    showMenu = false
                    onCreateSpace()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DropdownMenuItem(
                text = { Text("Scan QR") },
                onClick = {
                    showMenu = false
                    onScanQR()
                }
            )
        }
    }
}

private fun String.toCodenameBadgeLabel(): String {
    val parts = trim()
        .split(Regex("[^A-Za-z0-9]+"))
        .filter { it.isNotBlank() }

    val label = when {
        parts.size >= 2 -> parts.take(2).joinToString("") { it.take(1) }
        parts.size == 1 -> parts.first().take(2)
        else -> "me"
    }

    return label.lowercase(Locale.ROOT)
}
