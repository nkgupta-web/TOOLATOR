package com.universal.calulator

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    orangeColor: Color = Color(0xFFFF7A00) // Deep Dark Orange Accent
) {
    val theme = LocalAppTheme.current.value

    Surface(
        modifier = modifier.size(size),
        shape = RoundedCornerShape(size * 0.26f),
        color = Color(0xFF16171D), // Dark Matte Surface
        border = BorderStroke(1.5.dp, theme.funcBtn.copy(alpha = 0.7f)),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // 4 Flanking Math Operator Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .padding(top = size * 0.18f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: [ + ] and [ × ]
                Column(
                    verticalArrangement = Arrangement.spacedBy(size * 0.05f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoKeyButton(symbol = "+", orangeColor = orangeColor, size = size * 0.26f)
                    LogoKeyButton(symbol = "×", orangeColor = orangeColor, size = size * 0.26f)
                }

                // Right Column: [ − ] and [ ÷ ]
                Column(
                    verticalArrangement = Arrangement.spacedBy(size * 0.05f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LogoKeyButton(symbol = "−", orangeColor = orangeColor, size = size * 0.26f)
                    LogoKeyButton(symbol = "÷", orangeColor = orangeColor, size = size * 0.26f)
                }
            }

            // Central Bold "T" Overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Horizontal Bar of "T"
                    Spacer(Modifier.height(size * 0.15f))
                    Surface(
                        modifier = Modifier
                            .width(size * 0.78f)
                            .height(size * 0.17f),
                        shape = RoundedCornerShape(
                            topStart = size * 0.09f,
                            bottomStart = size * 0.06f,
                            topEnd = size * 0.09f,
                            bottomEnd = size * 0.06f
                        ),
                        color = Color(0xFFF1F2F6)
                    ) {}

                    // Vertical Stem of "T"
                    Surface(
                        modifier = Modifier
                            .width(size * 0.23f)
                            .fillMaxHeight(0.82f),
                        shape = RoundedCornerShape(
                            bottomStart = size * 0.08f,
                            bottomEnd = size * 0.08f
                        ),
                        color = Color(0xFFF1F2F6)
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun LogoKeyButton(
    symbol: String,
    orangeColor: Color,
    size: Dp
) {
    Surface(
        modifier = Modifier.size(size),
        shape = RoundedCornerShape(size * 0.28f),
        color = orangeColor,
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = (size.value * 0.62f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun AppLogoMainPreview() {
    Box(
        modifier = Modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        AppLogo(size = 300.dp)
    }
}