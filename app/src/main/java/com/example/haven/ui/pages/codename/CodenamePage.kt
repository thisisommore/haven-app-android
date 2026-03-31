package com.example.haven.ui.pages.codename

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.haven.xxdk.GeneratedIdentity

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CodenamePage(
    modifier: Modifier,
    codenames: List<GeneratedIdentity>,
    selected: String,
    onSelect: (String) -> Unit,
    onGenerate: () -> Unit,
    onClaim: () -> Unit,
    status: String,
    isLoading: Boolean,
    error: String?,
) {
    Column(modifier = modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pick a codename from the iOS flow.", style = MaterialTheme.typography.bodyMedium)
        if (status.isNotBlank()) {
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedButton(onClick = onGenerate, enabled = !isLoading) { Text("Generate another 10") }
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(codenames, key = { it.pubkey }) { identity ->
                FilterChip(
                    selected = selected == identity.pubkey,
                    onClick = { onSelect(identity.pubkey) },
                    label = { Text(identity.codename) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = onClaim, enabled = !isLoading && selected.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            if (isLoading) {
                CircularWavyProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(if (isLoading) status else "Claim codename")
        }
    }
}
