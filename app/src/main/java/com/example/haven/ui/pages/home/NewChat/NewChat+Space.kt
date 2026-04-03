package com.example.haven.ui.pages.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haven.xxdk.PrivacyLevel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Design tokens ──────────────────────────────────────────────────────────────
private val CardCornerRadius = 40.dp
private val ButtonCornerRadius = 28.dp

private val TitleFontSize = 22.sp
private val ValueFontSize = 22.sp
private val LabelFontSize = 12.sp
private val ButtonFontSize = 22.sp

/**
 * Sheet state for Create Space flow
 */
enum class CreateSpaceSheetState {
    FORM,
    CREATING,
    SUCCESS
}

/**
 * Data class for channel creation result
 */
data class CreatedChannelInfo(
    val channelId: String,
    val name: String,
    val description: String,
    val isSecret: Boolean
)

/**
 * Create Space Sheet - Allows users to create new channels/spaces
 * Mirrors iOS CreateSpaceSheet functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSpaceSheet(
    onDismiss: () -> Unit,
    onCreateSpace: (
        name: String,
        description: String,
        privacyLevel: PrivacyLevel,
        enableDms: Boolean,
        onSuccess: (CreatedChannelInfo) -> Unit,
        onError: (String) -> Unit
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val coroutineScope = rememberCoroutineScope()

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isSecret by remember { mutableStateOf(true) }
    var enableDms by remember { mutableStateOf(false) }
    var createState by remember { mutableStateOf(CreateSpaceSheetState.FORM) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = {
            if (createState != CreateSpaceSheetState.CREATING) {
                onDismiss()
            }
        },
        sheetState = modalSheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f),
        shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
        modifier = modifier.heightIn(min = screenHeight * 0.93f),
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
        when (createState) {
            CreateSpaceSheetState.FORM -> {
                CreateSpaceFormContent(
                    name = name,
                    onNameChange = { name = it; errorMessage = null },
                    description = description,
                    onDescriptionChange = { description = it },
                    isSecret = isSecret,
                    onIsSecretChange = { isSecret = it },
                    enableDms = enableDms,
                    onEnableDmsChange = { enableDms = it },
                    errorMessage = errorMessage,
                    onCreateClick = {
                        createState = CreateSpaceSheetState.CREATING
                        onCreateSpace(
                            name.trim(),
                            description.trim(),
                            if (isSecret) PrivacyLevel.SECRET else PrivacyLevel.PUBLIC,
                            enableDms,
                            { info ->
                                createState = CreateSpaceSheetState.SUCCESS
                                coroutineScope.launch {
                                    delay(1500)
                                    onDismiss()
                                }
                            },
                            { error ->
                                createState = CreateSpaceSheetState.FORM
                                errorMessage = error
                            }
                        )
                    },
                    onCancelClick = onDismiss,
                    modifier = Modifier.navigationBarsPadding()
                )
            }
            CreateSpaceSheetState.CREATING -> {
                CreatingContent(
                    modifier = Modifier.navigationBarsPadding()
                )
            }
            CreateSpaceSheetState.SUCCESS -> {
                SuccessContent(
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun CreateSpaceFormContent(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    isSecret: Boolean,
    onIsSecretChange: (Boolean) -> Unit,
    enableDms: Boolean,
    onEnableDmsChange: (Boolean) -> Unit,
    errorMessage: String?,
    onCreateClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryAccentColor = MaterialTheme.colorScheme.primary
    val cardBackgroundColor = MaterialTheme.colorScheme.surface
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // ── Header ───────────────────────────────────────────────────────────────
        Text(
            text = "Create Space",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Medium,
                color = primaryAccentColor,
                fontSize = TitleFontSize
            ),
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 60.dp, bottom = 20.dp)
        )

        // ── Name Input Card ──────────────────────────────────────────────────────
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
                    text = "Name",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                val textStyle = TextStyle(
                    fontSize = ValueFontSize,
                    color = onSurfaceColor
                )

                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(primaryAccentColor),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        Box {
                            if (name.isEmpty()) {
                                Text(
                                    text = "Space name",
                                    style = textStyle.copy(color = placeholderColor)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Description Input Card ───────────────────────────────────────────────
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
                    text = "Description",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = LabelFontSize),
                    color = labelColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                val textStyle = TextStyle(
                    fontSize = ValueFontSize,
                    color = onSurfaceColor
                )

                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    textStyle = textStyle,
                    cursorBrush = SolidColor(primaryAccentColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (description.isEmpty()) {
                                Text(
                                    text = "Space description (optional)",
                                    style = textStyle.copy(color = placeholderColor)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Privacy Toggle Card ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor)
                .padding(vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Secret Space",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = ValueFontSize),
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isSecret) {
                            "Secret Spaces hide everything: name, description, members, and messages."
                        } else {
                            "Public Spaces are accessible by anyone with the link."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = labelColor
                    )
                }
                Switch(
                    checked = isSecret,
                    onCheckedChange = onIsSecretChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Enable DM Toggle Card ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardCornerRadius))
                .background(cardBackgroundColor)
                .padding(vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Enable Direct Messages",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = ButtonFontSize),
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Allow others to send you direct messages from this space",
                        style = MaterialTheme.typography.bodySmall,
                        color = labelColor
                    )
                }
                Switch(
                    checked = enableDms,
                    onCheckedChange = onEnableDmsChange
                )
            }
        }

        // ── Error Message ────────────────────────────────────────────────────────
        errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
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
                onClick = onCancelClick,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", color = labelColor)
            }

            Button(
                onClick = onCreateClick,
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                shape = RoundedCornerShape(ButtonCornerRadius),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryAccentColor,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = name.isNotBlank()
            ) {
                Text(
                    "Create",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = ButtonFontSize
                    )
                )
            }
        }
    }
}

@Composable
private fun CreatingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Creating space...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SuccessContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Space created!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
