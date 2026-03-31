package com.example.haven.ui.pages.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PasswordPage(
    modifier: Modifier,
    password: String,
    confirm: String,
    onPassword: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onContinue: () -> Unit,
    onImport: () -> Unit,
    onClearAll: () -> Unit = {},
    status: String,
    isLoading: Boolean,
    error: String?,
    showClearAll: Boolean = false,
) {
    val rules = listOf(
        "8+ chars" to (password.length >= 8),
        "Uppercase" to password.any(Char::isUpperCase),
        "Number" to password.any(Char::isDigit),
    )
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Enter a password to secure your Haven identity.", style = MaterialTheme.typography.bodyMedium)
        if (status.isNotBlank()) {
            Text(status, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = password,
            onValueChange = onPassword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("New password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )
        OutlinedTextField(
            value = confirm,
            onValueChange = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Confirm password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = !isLoading
        )
        rules.forEach { (label, ok) ->
            Text(
                text = "${if (ok) "✓" else "○"} $label",
                color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(if (confirm.isEmpty()) "Confirm password to continue" else if (password == confirm) "Passwords match" else "Passwords don't match")
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Button(
            onClick = onContinue,
            enabled = !isLoading && password.isNotBlank() && password == confirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading) {
                CircularWavyProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            }
            Text(if (isLoading) status else "Continue")
        }
        OutlinedButton(
            onClick = onImport,
            enabled = !isLoading && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text("Import existing account") }
        
        // Clear All option for partial/half setup reset
        if (showClearAll) {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Having trouble? You can reset and start over.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedButton(
                onClick = onClearAll,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) { Text("Clear All & Start Over") }
        }
    }
}
