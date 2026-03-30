package com.example.haven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.haven.ui.ChatWithPreview
import com.example.haven.ui.HomeViewModel

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
                ChatCard(
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
private fun ChatCard(
    chat: ChatWithPreview,
    onClick: () -> Unit
) {
    ElevatedCard(onClick = onClick) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp), 
            verticalAlignment = Alignment.CenterVertically
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
                Surface(
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
