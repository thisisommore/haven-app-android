package com.example.haven.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Design tokens ──────────────────────────────────────────────────────────────
private val CardCornerRadius = 40.dp
private val ButtonCornerRadius = 28.dp

private val TitleFontSize = 22.sp
private val ValueFontSize = 22.sp
private val LabelFontSize = 12.sp
private val ButtonFontSize = 22.sp

/**
 * Password Input Sheet for private channels
 * Mirrors iOS PasswordInputSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputSheet(
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    var password by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        modifier = modifier.heightIn(min = screenHeight * 0.5f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
        }
    ) {
        PasswordInputContent(
            password = password,
            onPasswordChange = { password = it },
            onConfirm = { onConfirm(password) },
            onCancel = onCancel,
            modifier = Modifier.navigationBarsPadding()
        )
    }
}

@Composable
private fun PasswordInputContent(
    password: String,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryAccentColor = MaterialTheme.colorScheme.primary
    val cardBackgroundColor = MaterialTheme.colorScheme.surface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Private Channel",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = primaryAccentColor,
                    fontSize = TitleFontSize
                )
            )
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel"
                )
            }
        }

        // ── Description ──────────────────────────────────────────────────────────
        Text(
            text = "This channel is password protected. Enter the password to continue.",
            style = MaterialTheme.typography.bodyMedium,
            color = labelColor,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)
        )

        // ── Password Input Card ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor)
                .padding(vertical = 22.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp)
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                val textStyle = TextStyle(
                    fontSize = ValueFontSize,
                    color = onSurfaceColor
                )

                BasicTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(primaryAccentColor),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (password.isEmpty()) {
                                Text(
                                    text = "Enter password",
                                    style = textStyle.copy(color = placeholderColor)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Buttons ───────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = labelColor)
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                shape = RoundedCornerShape(ButtonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryAccentColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = password.isNotEmpty()
            ) {
                Text(
                    "Confirm",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = ButtonFontSize
                    )
                )
            }
        }
    }
}
