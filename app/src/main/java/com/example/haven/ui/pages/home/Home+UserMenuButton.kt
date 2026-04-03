package com.example.haven.ui.pages.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/**
 * Plus Menu Button - Shows menu for Join Channel, Create Space, Scan QR, etc.
 * Mirrors iOS PlusMenuButton
 */
@Composable
fun PlusMenuButton(
    onJoinChannel: () -> Unit,
    onCreateSpace: () -> Unit,
    onNewChat: () -> Unit,
    onNickname: () -> Unit,
    onExport: () -> Unit,
    onShareQR: () -> Unit,
    onScanQR: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = modifier) {
        IconButton(onClick = { showMenu = true }) {
            // Custom thin plus icon matching iOS design
            Canvas(modifier = Modifier.size(50.dp)) {
                val strokeWidth = 1.5.dp.toPx()
                val centerX = size.width / 2
                val centerY = size.height / 2
                val lineLength = size.minDimension * 0.35f

                // Horizontal line
                drawLine(
                    color = primaryColor,
                    start = Offset(centerX - lineLength, centerY),
                    end = Offset(centerX + lineLength, centerY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                // Vertical line
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
            onDismissRequest = { showMenu = false }
        ) {
            // Main Actions
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
            DropdownMenuItem(
                text = { Text("New Chat") },
                onClick = {
                    showMenu = false
                    onNewChat()
                }
            )

            HorizontalDivider()

            // User Actions
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
                text = { Text("My QR") },
                onClick = {
                    showMenu = false
                    onShareQR()
                }
            )
            DropdownMenuItem(
                text = { Text("Scan QR") },
                onClick = {
                    showMenu = false
                    onScanQR()
                }
            )

            HorizontalDivider()

            // Logout
            DropdownMenuItem(
                text = {
                    Text(
                        "Logout",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    showMenu = false
                    onLogout()
                }
            )
        }
    }
}
