package com.universal.calulator

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

val BrandFlameOrange = Color(0xFFFF6A00)
val BrandPureWhite = Color(0xFFFFFFFF)
val BrandDarkSlate = Color(0xFF0F172A)

@Composable
fun AppBrandLogoText(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 18.sp,
    letterSpacing: TextUnit = 1.5.sp
) {
    val theme = LocalAppTheme.current.value
    val baseColor = if (theme.isLight) BrandDarkSlate else BrandPureWhite

    Text(
        text = buildAnnotatedString {
            // "T O O L " -> Base Color
            withStyle(SpanStyle(color = baseColor, fontWeight = FontWeight.Black)) {
                append("TOOL ")
            }
            // "Λ T O R" -> Flame Orange with Lambda 'Λ'
            withStyle(SpanStyle(color = BrandFlameOrange, fontWeight = FontWeight.Black)) {
                append("ΛTOR")
            }
        },
        fontSize = fontSize,
        letterSpacing = letterSpacing,
        fontFamily = FontFamily.SansSerif,
        modifier = modifier
    )
}