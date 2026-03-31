package com.example.haven.ui.pages.password

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun PasswordPage(
    modifier: Modifier = Modifier,
    password: String,
    confirm: String,
    onPassword: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onContinue: () -> Unit,
    onImport: () -> Unit,
    status: String,
    isLoading: Boolean,
    error: String?,
) {
    val rules = listOf(
        "At least 8 characters" to (password.length >= 8),
        "Contains an uppercase letter" to password.any { it.isUpperCase() },
        "Contains a lowercase letter" to password.any { it.isLowerCase() },
        "Contains a number" to password.any { it.isDigit() },
        "Contains a symbol (!@#$...)" to password.any { !it.isLetterOrDigit() },
    )

    // Requirement: passwords must match AND be non-empty
    // (Relaxed from digit requirement based on user feedback)
    val matches = password.isNotBlank() && password == confirm
    val canContinue = matches && !isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDF7F2))
            .statusBarsPadding()
    ) {
        // Custom Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 24.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onImport,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF9DED3),
                    contentColor = Color(0xFF4E2A00)
                ),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                modifier = Modifier.height(40.dp)
            ) {
                Text("Import", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.width(12.dp))
            
            // Continue Button (Check Icon)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (canContinue) Color(0xFF4E2A00) else Color(0xFF4E2A00).copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(
                        enabled = canContinue,
                        onClick = onContinue
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Continue",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Text(
            text = "Join the alpha",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 34.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF4E2A00)
            ),
            modifier = Modifier.padding(start = 24.dp, bottom = 24.dp)
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = "Enter a password to secure your Haven identity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF5D4037).copy(alpha = 0.8f)
                )

                PasswordInputField(
                    label = "New Password",
                    value = password,
                    onValueChange = onPassword,
                    enabled = !isLoading
                )

                PasswordInputField(
                    label = "Confirm password",
                    value = confirm,
                    onValueChange = onConfirm,
                    enabled = !isLoading
                )

                Text(
                    text = "Password recommendation",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(top = 8.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rules.forEach { (label, met) ->
                        RuleItem(label = label, isMet = met)
                    }
                }

                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularWavyProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(status, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun PasswordInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF5D4037)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF2F2F7),
                unfocusedContainerColor = Color(0xFFF2F2F7),
                disabledContainerColor = Color(0xFFF2F2F7),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("-", color = Color.Gray) }
        )
    }
}

@Composable
private fun RuleItem(label: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.HighlightOff,
            contentDescription = null,
            tint = if (isMet) Color(0xFF4E2A00) else Color(0xFF5D4037).copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isMet) Color(0xFF4E2A00) else Color(0xFF5D4037).copy(alpha = 0.8f)
        )
    }
}
