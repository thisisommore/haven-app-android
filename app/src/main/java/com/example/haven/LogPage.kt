package com.example.haven

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun LogPage(
    modifier: Modifier,
    logs: List<String>,
    filter: String,
    search: String,
    onFilter: (String) -> Unit,
    onSearch: (String) -> Unit,
) {
    val levels = listOf("ALL", "ERROR", "WARN", "INFO", "DEBUG")
    val filtered = logs.filter { (filter == "ALL" || it.startsWith(filter)) && it.contains(search, true) }
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = search,
            onValueChange = onSearch,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search logs") }
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(levels) { level ->
                FilterChip(selected = filter == level, onClick = { onFilter(level) }, label = { Text(level) })
            }
        }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { line ->
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small) {
                    Text(line, Modifier.fillMaxWidth().padding(12.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
