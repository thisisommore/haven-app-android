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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
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
import androidx.compose.runtime.setValue
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
import com.example.haven.ui.pages.home.ChatWithPreview
import com.example.haven.ui.pages.home.HomeViewModel
import com.example.haven.ui.views.ChatListItem
import com.example.haven.ui.views.EmptyChatsState

// ── Haven brand palette ──────────────────────────────────────────────
private val HavenOrange = Color(0xFFE8860C)
private val HavenOrangeDark = Color(0xFFD07808)
private val HavenHeaderStart = Color(0xFFFFF8F0)   // top of gradient
private val HavenHeaderEnd = Color(0xFFFFF1E0)     // bottom of gradient
private val SearchBg = Color(0xFFFFF9F3)
private val SearchBorder = Color(0xFFE8D5BF)
private val SubtitleGray = Color(0xFF8E8E93)
private val DividerColor = Color(0xFFF2F2F7)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit = {},
    onLogout: () -> Unit = {},
    statusPercentage: Int = 0,
    isSetupComplete: Boolean = false,
    modifier: Modifier = Modifier
) {
    val search by viewModel.searchQuery.collectAsState()
    val chats by viewModel.filteredChats.collectAsState(initial = emptyList())
    val isLoading = !isSetupComplete && statusPercentage != 100

    // Menu / logout state
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    // ── Logout confirmation dialog ──────────────────────────────────
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = {
                Text(
                    "Logout",
                    fontWeight = FontWeight.SemiBold
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChat,
                containerColor = HavenOrange,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
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
                            colors = listOf(HavenHeaderStart, HavenHeaderEnd)
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
                                    fontSize = 30.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1C1E)
                            )
                            if (isLoading) {
                                LoadingIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = HavenOrange
                                )
                            }
                        }

                        // Overflow / menu icon
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Canvas(modifier = Modifier.size(20.dp)) {
                                    val dotRadius = 2.8.dp.toPx()
                                    val color = Color(0xFF1C1C1E).copy(alpha = 0.55f)
                                    val spacing = size.width / 3f
                                    for (i in 0..2) {
                                        drawCircle(
                                            color = color,
                                            radius = dotRadius,
                                            center = Offset(
                                                x = spacing * i + spacing / 2f,
                                                y = center.y
                                            )
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export") },
                                    onClick = { showMenu = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("QR Code") },
                                    onClick = { showMenu = false }
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = DividerColor
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
                        onValueChange = { viewModel.onSearchChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 2.dp,
                                shape = RoundedCornerShape(14.dp),
                                ambientColor = HavenOrange.copy(alpha = 0.08f),
                                spotColor = HavenOrange.copy(alpha = 0.12f)
                            )
                            .clip(RoundedCornerShape(14.dp)),
                        placeholder = {
                            Text(
                                "Search",
                                color = SubtitleGray,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = HavenOrange
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                }
            }

            // ── Chat list: white surface, rounded top corners ───────
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                shadowElevation = 8.dp,
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
                            ChatListItem(
                                chat = chat,
                                onClick = {
                                    viewModel.clearUnreadCount(chat.id)
                                    onOpenChat(chat.id)
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    start = 76.dp,  // inset past the avatar
                                    end = 20.dp
                                ),
                                color = DividerColor,
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}