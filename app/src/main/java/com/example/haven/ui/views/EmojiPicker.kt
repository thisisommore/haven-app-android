package com.example.haven.ui.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    recentEmojis: List<String> = emptyList()
) {
    val pagerState = rememberPagerState(pageCount = { EMOJI_CATEGORIES.size + if (recentEmojis.isNotEmpty()) 1 else 0 })
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        // Emoji pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val category = when {
                recentEmojis.isNotEmpty() && page == 0 -> EmojiCategory(
                    name = "Recent",
                    icon = Icons.Outlined.AccessTime,
                    emojis = recentEmojis
                )
                recentEmojis.isNotEmpty() -> EMOJI_CATEGORIES[page - 1]
                else -> EMOJI_CATEGORIES[page]
            }
            
            EmojiCategoryPage(
                category = category,
                onEmojiSelected = onEmojiSelected
            )
        }
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        
        // Category tabs at bottom (like iOS Signal)
        EmojiCategoryTabs(
            categories = EMOJI_CATEGORIES,
            recentEmojis = recentEmojis,
            selectedIndex = pagerState.currentPage,
            onCategorySelected = { index ->
                coroutineScope.launch {
                    pagerState.animateScrollToPage(index)
                }
            }
        )
    }
}

@Composable
private fun EmojiCategoryPage(
    category: EmojiCategory,
    onEmojiSelected: (String) -> Unit
) {
    Column {
        // Category header
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Emoji grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(category.emojis) { emoji ->
                EmojiItem(
                    emoji = emoji,
                    onClick = { onEmojiSelected(emoji) }
                )
            }
        }
    }
}

@Composable
private fun EmojiItem(
    emoji: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateDpAsState(
        targetValue = if (isPressed) 28.dp else 32.dp,
        label = "emoji_scale"
    )
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = if (isPressed) 22.sp else 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmojiCategoryTabs(
    categories: List<EmojiCategory>,
    recentEmojis: List<String>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit
) {
    val totalTabs = if (recentEmojis.isNotEmpty()) categories.size + 1 else categories.size
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recent tab (if there are recent emojis)
        if (recentEmojis.isNotEmpty()) {
            CategoryTab(
                icon = Icons.Outlined.AccessTime,
                isSelected = selectedIndex == 0,
                onClick = { onCategorySelected(0) }
            )
        }
        
        // Category tabs
        categories.forEachIndexed { index, category ->
            val actualIndex = if (recentEmojis.isNotEmpty()) index + 1 else index
            CategoryTab(
                icon = category.icon,
                isSelected = selectedIndex == actualIndex,
                onClick = { onCategorySelected(actualIndex) }
            )
        }
    }
}

@Composable
private fun CategoryTab(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (isSelected) 
            MaterialTheme.colorScheme.secondaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.size(40.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
