package com.universal.calulator

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.universal.calulator.data.ToolCategory
import com.universal.calulator.data.ToolItem
import com.universal.calulator.data.ToolRegistry

object HubPreferencesManager {
    private const val PREFS_NAME = "toolator_hub_prefs"
    private const val KEY_RECENTS = "recent_tool_ids"
    private const val KEY_FAVS = "favorite_tool_ids"

    fun getRecentIds(context: Context): List<String> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_RECENTS, "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    fun addRecent(context: Context, toolId: String) {
        if (toolId.isBlank()) return
        val list = getRecentIds(context).toMutableList()
        list.remove(toolId)
        list.add(0, toolId)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENTS, list.take(10).joinToString(","))
            .apply()
    }

    fun removeSingleRecent(context: Context, toolId: String): List<String> {
        val list = getRecentIds(context).toMutableList()
        list.remove(toolId)
        val result = list.filter { it.isNotEmpty() }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_RECENTS, result.joinToString(","))
            .apply()
        return result
    }

    fun clearAllRecents(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_RECENTS)
            .apply()
    }

    fun getFavoriteIds(context: Context): Set<String> {
        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_FAVS, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }

    fun toggleFavorite(context: Context, toolId: String): Set<String> {
        val set = getFavoriteIds(context).toMutableSet()
        if (set.contains(toolId)) {
            set.remove(toolId)
        } else {
            set.add(toolId)
        }
        val cleanSet = set.filter { it.isNotEmpty() }.toSet()
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FAVS, cleanSet.joinToString(","))
            .apply()
        return cleanSet
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsHubScreen(
    onBack: () -> Unit = {},
    onCategoryClick: (String) -> Unit = {},
    onToolClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current.value
    var searchQuery by remember { mutableStateOf("") }
    var showManageFavsSheet by remember { mutableStateOf(false) }

    var recentIds by remember { mutableStateOf(HubPreferencesManager.getRecentIds(context)) }
    var favoriteIds by remember { mutableStateOf(HubPreferencesManager.getFavoriteIds(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                recentIds = HubPreferencesManager.getRecentIds(context)
                favoriteIds = HubPreferencesManager.getFavoriteIds(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val recentTools = remember(recentIds) {
        recentIds.mapNotNull { id -> ToolRegistry.getToolById(id) }
    }

    val favoriteTools = remember(favoriteIds) {
        favoriteIds.mapNotNull { id -> ToolRegistry.getToolById(id) }
    }

    val searchResults = remember(searchQuery) {
        ToolRegistry.searchTools(searchQuery)
    }

    val launchTool: (String) -> Unit = { toolId ->
        HubPreferencesManager.addRecent(context, toolId)
        recentIds = HubPreferencesManager.getRecentIds(context)
        onToolClick(toolId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        // 1. Header Bar with Brand Logo
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }

            // Standardized Brand Logo Header
            AppBrandLogoText(
                fontSize = 18.sp,
                letterSpacing = 1.8.sp
            )

            Spacer(modifier = Modifier.size(36.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search ${ToolRegistry.allTools.size} tools & converters...", fontSize = 14.sp, color = theme.textSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            textStyle = TextStyle(
                color = theme.textPrimary,
                fontSize = 14.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = theme.textPrimary,
                unfocusedTextColor = theme.textPrimary,
                focusedContainerColor = theme.surface,
                unfocusedContainerColor = theme.surface,
                focusedBorderColor = theme.accent,
                unfocusedBorderColor = theme.funcBtn.copy(alpha = 0.6f),
                cursorColor = theme.accent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Main Screen Body
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (searchQuery.isNotEmpty()) {
                item {
                    Text(
                        text = "Search Results (${searchResults.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                }

                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tools matching \"$searchQuery\"", color = theme.textSecondary, fontSize = 14.sp)
                        }
                    }
                } else {
                    items(searchResults) { tool ->
                        val isFav = favoriteIds.contains(tool.id)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = theme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { launchTool(tool.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PremiumGlowBadge(icon = tool.icon, size = 38.dp, iconSize = 20.dp)
                                    Column {
                                        Text(text = tool.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                        Text(text = tool.category.title, fontSize = 11.sp, color = theme.textSecondary)
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        favoriteIds = HubPreferencesManager.toggleFavorite(context, tool.id)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (isFav) theme.accent else theme.textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Section 1: Categories
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Categories",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = theme.numBtn,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "${ToolRegistry.allTools.size} tools",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        ToolCategory.entries.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { cat ->
                                    CategoryCard(
                                        category = cat,
                                        toolCount = ToolRegistry.getToolCount(cat),
                                        modifier = Modifier.weight(1f),
                                        onClick = { onCategoryClick(cat.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Section 2: Recently Used
                if (recentTools.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recently Used",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Clear All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.accent,
                                modifier = Modifier.clickable {
                                    HubPreferencesManager.clearAllRecents(context)
                                    recentIds = emptyList()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            recentTools.forEach { tool ->
                                RemovableRecentChip(
                                    tool = tool,
                                    onLaunch = { launchTool(tool.id) },
                                    onRemove = {
                                        recentIds = HubPreferencesManager.removeSingleRecent(context, tool.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Section 3: Favorites
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Favorites",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Manage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.accent,
                            modifier = Modifier.clickable { showManageFavsSheet = true }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (favoriteTools.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = theme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showManageFavsSheet = true }
                        ) {
                            Text(
                                text = "+ Tap 'Manage' to bookmark your favorite tools",
                                fontSize = 12.sp,
                                color = theme.textSecondary,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            favoriteTools.forEach { tool ->
                                FavoriteToolChip(
                                    tool = tool,
                                    onClick = { launchTool(tool.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 4. Manage Favorites Modal Sheet
    if (showManageFavsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showManageFavsSheet = false },
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Manage Favorites (${favoriteIds.size}/${ToolRegistry.allTools.size})",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "Bookmark tools for one-tap direct access",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }

                    if (favoriteIds.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                context.getSharedPreferences("toolator_hub_prefs", Context.MODE_PRIVATE)
                                    .edit()
                                    .remove("favorite_tool_ids")
                                    .apply()
                                favoriteIds = emptySet()
                            }
                        ) {
                            Text("Clear", color = theme.accent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(ToolRegistry.allTools, key = { it.id }) { tool ->
                        val isFav = favoriteIds.contains(tool.id)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = theme.numBtn,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    favoriteIds = HubPreferencesManager.toggleFavorite(context, tool.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PremiumGlowBadge(icon = tool.icon, size = 32.dp, iconSize = 18.dp)
                                    Column {
                                        Text(
                                            text = tool.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary
                                        )
                                        Text(
                                            text = tool.category.title,
                                            fontSize = 10.sp,
                                            color = theme.textSecondary
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        favoriteIds = HubPreferencesManager.toggleFavorite(context, tool.id)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (isFav) theme.accent else theme.textSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: ToolCategory,
    toolCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = theme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.55f)),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(color = theme.accent.copy(alpha = 0.15f)),
            onClick = onClick
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PremiumGlowBadge(icon = category.icon, size = 44.dp, iconSize = 24.dp)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = theme.numBtn,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "$toolCount tools",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accent,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = category.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = category.description,
                    fontSize = 11.sp,
                    color = theme.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RemovableRecentChip(
    tool: ToolItem,
    onLaunch: () -> Unit,
    onRemove: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = theme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
        modifier = Modifier.clickable(onClick = onLaunch)
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PremiumGlowBadge(icon = tool.icon, size = 30.dp, iconSize = 17.dp, cornerRadius = 9.dp)

            Text(
                text = tool.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(20.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = theme.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun FavoriteToolChip(
    tool: ToolItem,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = theme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PremiumGlowBadge(icon = tool.icon, size = 30.dp, iconSize = 17.dp, cornerRadius = 9.dp)

            Text(
                text = tool.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PremiumGlowBadge(
    icon: ImageVector,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 13.dp
) {
    val theme = LocalAppTheme.current.value
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        theme.accent.copy(alpha = if (theme.isLight) 0.15f else 0.22f),
                        theme.numBtn
                    )
                )
            )
            .border(
                width = 1.dp,
                color = theme.accent.copy(alpha = if (theme.isLight) 0.25f else 0.35f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = theme.accent,
            modifier = Modifier.size(iconSize)
        )
    }
}