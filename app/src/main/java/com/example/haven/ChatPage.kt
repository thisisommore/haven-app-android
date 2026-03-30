package com.example.haven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.haven.ui.components.HtmlText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haven.data.db.ChatMessageEntity

@Composable
internal fun ChatPage(
    modifier: Modifier,
    messages: List<ChatMessageEntity>
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        reverseLayout = true
    ) {
        items(messages.asReversed(), key = { it.id }) { msg ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (msg.isIncoming) Arrangement.Start else Arrangement.End
            ) {
                Surface(
                    color = if (msg.isIncoming) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.medium
                ) {
                    HtmlText(
                        html = msg.message,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}
