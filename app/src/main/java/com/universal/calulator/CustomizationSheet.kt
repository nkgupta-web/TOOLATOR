package com.universal.calulator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomizationSheet(
    currentLayout: KeypadLayout,
    currentTheme: AppTheme,
    onLayoutChange: (KeypadLayout) -> Unit,
    onThemeChange: (AppTheme) -> Unit,
    onResetDefault: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val hapticState = LocalHapticEnabled.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = currentTheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Customize Appearance",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.textPrimary
                        )
                        Text(
                            text = "Personalize colors & keypad",
                            fontSize = 12.sp,
                            color = currentTheme.textSecondary
                        )
                    }

                    IconButton(
                        onClick = {
                            ThemePreferenceManager.saveThemeId(context, AppThemePreset.CYBER_AMBER.id)
                            onResetDefault()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(currentTheme.numBtn)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Defaults",
                            tint = currentTheme.accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Color Palette",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(AppThemePreset.allThemes) { themeItem ->
                        val isSelected = themeItem.id == currentTheme.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable {
                                ThemePreferenceManager.saveThemeId(context, themeItem.id)
                                onThemeChange(themeItem)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(themeItem.bg)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) currentTheme.accent else currentTheme.funcBtn,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(themeItem.accent)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (themeItem.isLight) Color.White else Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = themeItem.name,
                                fontSize = 11.sp,
                                color = if (isSelected) currentTheme.accent else currentTheme.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Keypad Layout",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.textSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    KeypadLayout.entries.forEach { layout ->
                        val isSelected = layout == currentLayout
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) currentTheme.funcBtn else currentTheme.numBtn,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, currentTheme.accent) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLayoutChange(layout) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = layout.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) currentTheme.accent else currentTheme.textPrimary
                                )

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = currentTheme.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Tactile Response",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = currentTheme.textSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = currentTheme.numBtn,
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Keypad Vibration",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = currentTheme.textPrimary
                        )

                        Switch(
                            checked = hapticState.value,
                            onCheckedChange = { hapticState.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = currentTheme.accent,
                                checkedTrackColor = currentTheme.accent.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    }
}