package com.universal.calulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.universal.calulator.data.ToolCategory
import com.universal.calulator.data.ToolItem
import com.universal.calulator.data.ToolRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    categoryId: String?,
    onBack: () -> Unit,
    onToolClick: (String) -> Unit
) {
    val context = LocalContext.current
    val theme = LocalAppTheme.current.value

    val category = ToolCategory.fromId(categoryId)
    val categoryTitle = category?.title ?: "All Tools"
    val tools: List<ToolItem> = if (category != null) {
        ToolRegistry.getToolsByCategory(category)
    } else {
        ToolRegistry.allTools
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // 1. Header Bar with Brand Logo & Category Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = theme.textPrimary
                    )
                }

                Text(
                    text = categoryTitle,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            }

            // TOOLATOR Brand Logo in Category Header
            AppBrandLogoText(
                fontSize = 15.sp,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        // 2. Tools List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 20.dp)
        ) {
            items(tools, key = { it.id }) { tool ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = theme.accent.copy(alpha = 0.15f)),
                            onClick = {
                                HubPreferencesManager.addRecent(context, tool.id)
                                onToolClick(tool.id)
                            }
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            PremiumToolIconBadge(icon = tool.icon)

                            Column {
                                Text(
                                    text = tool.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = tool.description,
                                    fontSize = 12.sp,
                                    color = theme.textSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = theme.textSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Elevated Glassmorphic Squircle Badge Component
@Composable
fun PremiumToolIconBadge(
    icon: ImageVector,
    size: androidx.compose.ui.unit.Dp = 44.dp,
    iconSize: androidx.compose.ui.unit.Dp = 22.dp
) {
    val theme = LocalAppTheme.current.value
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
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
                shape = RoundedCornerShape(14.dp)
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