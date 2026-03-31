package com.example.haven


import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding


import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.haven.ui.ChatWithPreview
import com.example.haven.ui.HomeViewModel
import com.example.haven.ui.components.ChatListItem
import com.example.haven.ui.components.EmptyChatsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenChat: (String) -> Unit,
    onNewChat: () -> Unit = {},
    statusPercentage: Int = 0,
    modifier: Modifier = Modifier
) {
    val search by viewModel.searchQuery.collectAsState()
    val chats by viewModel.filteredChats.collectAsState(initial = emptyList())
    var expanded by remember { mutableStateOf(false) }
    val isLoading = statusPercentage != 100
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chat",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewChat,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New chat"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Material 3 Contained Loading Indicator (LinearProgressIndicator)
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { statusPercentage / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            }
            
            // Material 3 DockedSearchBar
            DockedSearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = search,
                        onQueryChange = { viewModel.onSearchChange(it) },
                        onSearch = { expanded = false },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("Search chats...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    )
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search suggestions can be added here
            }
            
            // Chat List
            if (chats.isEmpty()) {
                EmptyChatsState(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
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
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
    }
}

// Legacy HomePage for backward compatibility
@Composable
internal fun HomePage(
    modifier: Modifier,
    viewModel: HomeViewModel,
    onOpenChat: (String) -> Unit,
) {
    val search by viewModel.searchQuery.collectAsState()
    val chats by viewModel.filteredChats.collectAsState(initial = emptyList())
    
    Column(
        modifier = modifier.fillMaxSize().padding(16.dp), 
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = search,
            onValueChange = { viewModel.onSearchChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search chats") }
        )
        LazyColumn(
            modifier = Modifier.weight(1f), 
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chats, key = { it.id }) { chat ->
                LegacyChatCard(
                    chat = chat,
                    onClick = { 
                        viewModel.clearUnreadCount(chat.id)
                        onOpenChat(chat.id) 
                    }
                )
            }
        }
    }
}

@Composable
private fun LegacyChatCard(
    chat: ChatWithPreview,
    onClick: () -> Unit
) {
    androidx.compose.material3.Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.foundation.layout.Row(
            Modifier.fillMaxWidth().padding(16.dp), 
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f), 
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(chat.title, fontWeight = FontWeight.SemiBold)
                Text(
                    chat.preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (chat.unreadCount > 0) {
                androidx.compose.material3.Surface(
                    shape = MaterialTheme.shapes.small, 
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        chat.unreadCount.toString(), 
                        Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
