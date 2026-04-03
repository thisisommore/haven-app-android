package com.example.haven.ui.pages.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Data class representing an emoji reaction option
 */
data class ReactionOption(
    val emoji: String,
    val name: String
)

/**
 * ReactionPickerSheet - Sheet-based emoji picker for message reactions
 * Mirrors iOS reaction picker design pattern
 *
 * @param onDismiss Called when the sheet is dismissed
 * @param onReactionSelected Called when an emoji is selected
 * @param recentReactions List of recently used reactions
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReactionPickerSheet(
    onDismiss: () -> Unit,
    onReactionSelected: (String) -> Unit,
    recentReactions: List<String> = emptyList(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    // Common reactions organized by category (matching iOS)
    val quickReactions = remember {
        listOf(
            ReactionOption("❤️", "heart"),
            ReactionOption("👍", "thumbs up"),
            ReactionOption("👎", "thumbs down"),
            ReactionOption("😂", "joy"),
            ReactionOption("😮", "wow"),
            ReactionOption("😢", "sad"),
            ReactionOption("🎉", "party"),
            ReactionOption("🔥", "fire")
        )
    }

    val smileys = remember {
        listOf(
            ReactionOption("😀", "grinning"),
            ReactionOption("😃", "smiley"),
            ReactionOption("😄", "smile"),
            ReactionOption("😁", "beaming"),
            ReactionOption("😆", "laughing"),
            ReactionOption("😅", "sweat smile"),
            ReactionOption("🤣", "rofl"),
            ReactionOption("😂", "joy"),
            ReactionOption("🙂", "slight smile"),
            ReactionOption("😊", "blush"),
            ReactionOption("😇", "innocent"),
            ReactionOption("🥰", "love"),
            ReactionOption("😍", "heart eyes"),
            ReactionOption("🤩", "star struck"),
            ReactionOption("😘", "kissing"),
            ReactionOption("😗", "kissing face")
        )
    }

    val gestures = remember {
        listOf(
            ReactionOption("👍", "thumbs up"),
            ReactionOption("👎", "thumbs down"),
            ReactionOption("👌", "ok"),
            ReactionOption("🤌", "pinched"),
            ReactionOption("🤏", "pinching"),
            ReactionOption("✌️", "victory"),
            ReactionOption("🤞", "fingers crossed"),
            ReactionOption("🤟", "love you"),
            ReactionOption("🤘", "rock on"),
            ReactionOption("👏", "clap"),
            ReactionOption("🙌", "raised hands"),
            ReactionOption("👐", "open hands"),
            ReactionOption("🤲", "palms up"),
            ReactionOption("🤝", "handshake"),
            ReactionOption("💪", "muscle"),
            ReactionOption("🙏", "pray")
        )
    }

    val hearts = remember {
        listOf(
            ReactionOption("❤️", "red heart"),
            ReactionOption("🧡", "orange heart"),
            ReactionOption("💛", "yellow heart"),
            ReactionOption("💚", "green heart"),
            ReactionOption("💙", "blue heart"),
            ReactionOption("💜", "purple heart"),
            ReactionOption("🖤", "black heart"),
            ReactionOption("🤍", "white heart"),
            ReactionOption("🤎", "brown heart"),
            ReactionOption("💔", "broken heart"),
            ReactionOption("❤️‍🔥", "fire heart"),
            ReactionOption("❤️‍🩹", "healing heart"),
            ReactionOption("💕", "two hearts"),
            ReactionOption("💞", "revolving hearts"),
            ReactionOption("💓", "beating heart"),
            ReactionOption("💗", "growing heart")
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            // Header
            Text(
                text = "Add Reaction",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick reactions row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                quickReactions.forEach { reaction ->
                    ReactionEmojiButton(
                        emoji = reaction.emoji,
                        onClick = {
                            onReactionSelected(reaction.emoji)
                            onDismiss()
                        },
                        size = 48.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Recent reactions section
            if (recentReactions.isNotEmpty()) {
                Text(
                    text = "Recently Used",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentReactions.distinct().take(10)) { emoji ->
                        ReactionEmojiButton(
                            emoji = emoji,
                            onClick = {
                                onReactionSelected(emoji)
                                onDismiss()
                            },
                            size = 40.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            // Smileys & Emotion
            Text(
                text = "Smileys & Emotion",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                smileys.forEach { reaction ->
                    ReactionEmojiButton(
                        emoji = reaction.emoji,
                        onClick = {
                            onReactionSelected(reaction.emoji)
                            onDismiss()
                        },
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Gestures & Hands
            Text(
                text = "Gestures & Hands",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gestures.forEach { reaction ->
                    ReactionEmojiButton(
                        emoji = reaction.emoji,
                        onClick = {
                            onReactionSelected(reaction.emoji)
                            onDismiss()
                        },
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hearts
            Text(
                text = "Hearts",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hearts.forEach { reaction ->
                    ReactionEmojiButton(
                        emoji = reaction.emoji,
                        onClick = {
                            onReactionSelected(reaction.emoji)
                            onDismiss()
                        },
                        size = 40.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReactionEmojiButton(
    emoji: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.5f).sp,
            textAlign = TextAlign.Center
        )
    }
}
