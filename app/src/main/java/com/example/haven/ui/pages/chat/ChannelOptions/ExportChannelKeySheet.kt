package com.example.haven.ui.pages.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File

/**
 * Export Key Dialog for exporting channel admin key
 * Mirrors iOS ExportChannelKeySheet
 */
@Composable
fun ExportKeyDialog(
    viewModel: ChannelOptionsController,
    onDismiss: () -> Unit
) {
    val exportKeyContent by viewModel.exportKeyContent.collectAsState()
    val exportError by viewModel.exportError.collectAsState()

    var password by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    val isPasswordValid = password.isNotEmpty()

    AlertDialog(
        onDismissRequest = {
            if (exportKeyContent == null) {
                onDismiss()
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Export Key",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (exportKeyContent == null) {
                    // Export form
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 16.dp)
                            .rotate(90f),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "Export Admin Key",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Export the admin key to share admin privileges with another user.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Encryption Password") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Key,
                                contentDescription = null,
                                modifier = Modifier.rotate(90f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (isPasswordValid) {
                                    viewModel.exportChannelKey(password)
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    exportError?.let { error ->
                        Text(
                            error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action buttons
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.exportChannelKey(password)
                        },
                        enabled = isPasswordValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null
                            )
                            Text("Export to File")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.exportChannelKey(password)
                        },
                        enabled = isPasswordValid,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null
                            )
                            Text("Copy to Clipboard")
                        }
                    }
                } else {
                    // Success state - show exported key
                    Icon(
                        imageVector = Icons.Outlined.Key,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 16.dp)
                            .rotate(90f),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        "Key Exported",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        "Keep this key secure. Anyone with this key can manage the channel.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Show the key content
                    androidx.compose.foundation.text.selection.SelectionContainer {
                        Text(
                            exportKeyContent!!,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp),
                            maxLines = 4,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save to file and copy buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                saveKeyToFile(context, exportKeyContent!!)
                                viewModel.showToast("Saved to file")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.FileDownload,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Save")
                        }

                        Button(
                            onClick = {
                                copyKeyToClipboard(context, exportKeyContent!!)
                                viewModel.showToast("Copied to clipboard")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ContentCopy,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                            Text("Copy")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Keep this key secure. Anyone with this key can manage the channel.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}

/**
 * Save key to a file in app's private storage
 */
private fun saveKeyToFile(context: android.content.Context, keyContent: String) {
    try {
        val file = File(context.filesDir, "channel_admin_key.txt")
        file.writeText(keyContent)
    } catch (e: Exception) {
        android.util.Log.e("ExportKey", "Failed to save file: ${e.message}")
    }
}

/**
 * Copy key to clipboard
 */
private fun copyKeyToClipboard(context: android.content.Context, keyContent: String) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Channel Admin Key", keyContent)
    clipboard.setPrimaryClip(clip)
}
