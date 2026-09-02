package com.universal.calulator

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

data class AppTheme(
    val id: String,
    val name: String,
    val isLight: Boolean = false,
    val bg: Color,
    val surface: Color,
    val numBtn: Color,
    val funcBtn: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color
)

enum class KeypadLayout(val id: String, val title: String) {
    STANDARD("standard", "Standard (7-8-9 Top)"),
    ASCENDING("ascending", "Phone Dial (1-2-3 Top)"),
    REVERSE("reverse", "Inverted (9-8-7 Top)"),
    CLASSIC("classic", "Accounting / Classic"),
    COMPACT("compact", "Compact")
}

object AppThemePreset {
    // 1. Signature Matte Amber (Default Slate Dark)
    val CYBER_AMBER = AppTheme(
        id = "cyber_amber",
        name = "Matte Amber",
        isLight = false,
        bg = Color(0xFF0F1115),
        surface = Color(0xFF161920),
        numBtn = Color(0xFF1E222B),
        funcBtn = Color(0xFF282D37),
        accent = Color(0xFFF59E0B),
        textPrimary = Color(0xFFF3F4F6),
        textSecondary = Color(0xFF9CA3AF),
        textMuted = Color(0xFF6B7280)
    )

    // 2. Pure AMOLED Black (Zero Battery Drain, 100% Pitch Black)
    val AMOLED_BLACK = AppTheme(
        id = "amoled_black",
        name = "Pure AMOLED",
        isLight = false,
        bg = Color(0xFF000000),
        surface = Color(0xFF0C0C0C),
        numBtn = Color(0xFF141414),
        funcBtn = Color(0xFF222222),
        accent = Color(0xFFFF9F0A),
        textPrimary = Color(0xFFFFFFFF),
        textSecondary = Color(0xFF9E9E9E),
        textMuted = Color(0xFF616161)
    )

    // 3. AMOLED Ice Cyan (Pitch Black + Electric Cyan)
    val AMOLED_CYAN = AppTheme(
        id = "amoled_cyan",
        name = "AMOLED Cyan",
        isLight = false,
        bg = Color(0xFF000000),
        surface = Color(0xFF0B0D0F),
        numBtn = Color(0xFF12161A),
        funcBtn = Color(0xFF1A2128),
        accent = Color(0xFF00E5FF),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        textMuted = Color(0xFF64748B)
    )

    // 4. Clean White & Royal Blue (Light Theme)
    val WHITE_BLUE = AppTheme(
        id = "white_blue",
        name = "White & Royal Blue",
        isLight = true,
        bg = Color(0xFFF8FAFC),
        surface = Color(0xFFFFFFFF),
        numBtn = Color(0xFFF1F5F9),
        funcBtn = Color(0xFFE2E8F0),
        accent = Color(0xFF2563EB),
        textPrimary = Color(0xFF0F172A),
        textSecondary = Color(0xFF64748B),
        textMuted = Color(0xFF94A3B8)
    )

    // 5. Nordic Indigo / Purple (Dark Violet)
    val NORDIC_SLATE = AppTheme(
        id = "nordic_slate",
        name = "Nordic Purple",
        isLight = false,
        bg = Color(0xFF10121A),
        surface = Color(0xFF171A24),
        numBtn = Color(0xFF202433),
        funcBtn = Color(0xFF2C3247),
        accent = Color(0xFF818CF8),
        textPrimary = Color(0xFFEEF2FF),
        textSecondary = Color(0xFFA5B4FC).copy(alpha = 0.7f),
        textMuted = Color(0xFF6366F1).copy(alpha = 0.5f)
    )

    // 6. Obsidian Sky Blue (Dark Oceanic Blue)
    val OBSIDIAN = AppTheme(
        id = "obsidian",
        name = "Obsidian Sky",
        isLight = false,
        bg = Color(0xFF0A0C0E),
        surface = Color(0xFF14171A),
        numBtn = Color(0xFF1B1F24),
        funcBtn = Color(0xFF262C33),
        accent = Color(0xFF0EA5E9),
        textPrimary = Color(0xFFF1F5F9),
        textSecondary = Color(0xFF94A3B8),
        textMuted = Color(0xFF64748B)
    )

    // 7. Electric Indigo (Modern OLED Violet)
    val ELECTRIC_INDIGO = AppTheme(
        id = "electric_indigo",
        name = "Royal Indigo",
        isLight = false,
        bg = Color(0xFF0C0D14),
        surface = Color(0xFF141724),
        numBtn = Color(0xFF1C2032),
        funcBtn = Color(0xFF282E48),
        accent = Color(0xFF6366F1), // Clean Vibrant Indigo Accent
        textPrimary = Color(0xFFF1F3FB),
        textSecondary = Color(0xFF9CA3AF),
        textMuted = Color(0xFF6B7280)
    )

    // 8. Warm Cream Latte (Soft Warm Light Theme)
    val WARM_CREAM = AppTheme(
        id = "warm_cream",
        name = "Warm Cream",
        isLight = true,
        bg = Color(0xFFFAF7F2),
        surface = Color(0xFFFFFFFF),
        numBtn = Color(0xFFF2ECE4),
        funcBtn = Color(0xFFE4DAD0),
        accent = Color(0xFFD97706),
        textPrimary = Color(0xFF292524),
        textSecondary = Color(0xFF78716C),
        textMuted = Color(0xFFA8A29E)
    )

    // 9. Minimalist Slate Grey (Monochrome)
    val MONOCHROME = AppTheme(
        id = "monochrome",
        name = "Minimalist Grey",
        isLight = false,
        bg = Color(0xFF121214),
        surface = Color(0xFF1C1C20),
        numBtn = Color(0xFF27272D),
        funcBtn = Color(0xFF35353D),
        accent = Color(0xFFE4E4E7),
        textPrimary = Color(0xFFFAFAFA),
        textSecondary = Color(0xFFA1A1AA),
        textMuted = Color(0xFF71717A)
    )

    val allThemes = listOf(
        CYBER_AMBER,
        AMOLED_BLACK,
        AMOLED_CYAN,
        WHITE_BLUE,
        NORDIC_SLATE,
        OBSIDIAN,
        ELECTRIC_INDIGO,
        WARM_CREAM,
        MONOCHROME
    )

    fun getThemeById(id: String): AppTheme {
        return allThemes.find { it.id.equals(id, ignoreCase = true) } ?: CYBER_AMBER
    }
}

object ThemePreferenceManager {
    private const val PREFS_NAME = "toolator_theme_preferences"
    private const val KEY_THEME_ID = "saved_app_theme_id"

    fun saveThemeId(context: Context, themeId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_ID, themeId)
            .apply()
    }

    fun getSavedTheme(context: Context): AppTheme {
        val savedId = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_ID, AppThemePreset.CYBER_AMBER.id) ?: AppThemePreset.CYBER_AMBER.id
        return AppThemePreset.getThemeById(savedId)
    }
}

val LocalAppTheme = compositionLocalOf { mutableStateOf(AppThemePreset.CYBER_AMBER) }
val LocalHapticEnabled = compositionLocalOf { mutableStateOf(true) }