package com.universal.calulator

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val bgColor = Color(0xFF16171D)
    val cardColor = Color(0xFF1E1F28)
    val borderColor = Color(0xFF2E313D)
    val darkOrange = Color(0xFFFF7A00)
    val textPrimary = Color.White
    val textSecondary = Color(0xFF9E9EA7)

    var showUpdateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About TOOLATOR",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor
                )
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 6.dp, bottom = 24.dp)
        ) {
            // 1. Brand Identity Hero Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "TOOLATOR App Logo",
                            modifier = Modifier
                                .size(84.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Black)) {
                                    append("TOOL")
                                }
                                append(" ")
                                withStyle(style = SpanStyle(color = darkOrange, fontWeight = FontWeight.Black)) {
                                    append("ΛTOR")
                                }
                            },
                            fontSize = 22.sp,
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Universal Calculation & Productivity Suite",
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            shape = CircleShape,
                            color = darkOrange.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, darkOrange.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Version 3.0.0 • Stable",
                                color = darkOrange,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 2. Check for Updates Action Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUpdateDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, darkOrange.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = darkOrange.copy(alpha = 0.15f),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Update",
                                        tint = darkOrange,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Check for Updates",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Text(
                                    text = "v3.0.0 is currently the latest build",
                                    fontSize = 11.5.sp,
                                    color = textSecondary
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 3. Combined Card: What, Why & Our Purpose
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WHAT & WHY TOOLATOR?",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Created from “Tool” + “Calculator”, Toolator eliminates the hassle of switching between multiple single-purpose apps.",
                            fontSize = 13.sp,
                            color = textPrimary,
                            lineHeight = 18.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "It combines essential calculations, financial utilities, unit converters, and date tools into a single, distraction-free suite.",
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.5.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = borderColor)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "OUR PURPOSE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Make everyday calculations simpler, faster, and reliable with zero clutter. Designed to solve real day-to-day problems instantly.",
                            fontSize = 13.sp,
                            color = textSecondary,
                            lineHeight = 18.5.sp
                        )
                    }
                }
            }

            // 4. What's Inside
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "WHAT'S INSIDE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FeatureRow("⚡", "Powerful Calculator", "Basic, scientific, and precision calculations.", textPrimary, textSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        FeatureRow("🧰", "Everyday Tools", "Finance, unit converters, dates, health & utility tools.", textPrimary, textSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        FeatureRow("🎨", "Customizable Experience", "Carefully crafted dark themes to match your preference.", textPrimary, textSecondary)
                        Spacer(modifier = Modifier.height(10.dp))
                        FeatureRow("📋", "Smart History", "Track past calculations cleanly on device.", textPrimary, textSecondary)
                    }
                }
            }

            // 5. Built With Purpose
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "BUILT WITH PURPOSE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        PurposeRow("SIMPLICITY", "Less clutter, fewer distractions.", darkOrange, textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        PurposeRow("UTILITY", "Tools designed for real everyday problems.", darkOrange, textPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        PurposeRow("SPEED", "Quick access and fast results.", darkOrange, textPrimary)
                    }
                }
            }

            // 6. Privacy-First (Streamlined & Short)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PRIVACY-FIRST",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your calculations, tags, and preferences stay completely offline on your device. No login or account is required.",
                            fontSize = 13.sp,
                            color = textPrimary,
                            lineHeight = 18.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Live tools (like currency rates) connect strictly on-demand to fetch required data.",
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            lineHeight = 17.5.sp
                        )
                    }
                }
            }

            // 7. Support & Feedback Actions
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SUPPORT & FEEDBACK",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        ActionRowItem(
                            icon = Icons.Default.Share,
                            title = "Share TOOLATOR",
                            subtitle = "Recommend TOOLATOR to friends",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Check out TOOLATOR - Universal Calculation & Productivity Suite!")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share TOOLATOR"))
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)
                        ActionRowItem(
                            icon = Icons.Default.Star,
                            title = "Rate & Review",
                            subtitle = "Help improve the app",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = {
                                Toast.makeText(context, "Redirecting to store...", Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)
                        ActionRowItem(
                            icon = Icons.Default.Email,
                            title = "Feedback & Bug Report",
                            subtitle = "Report an issue or share an idea",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_SUBJECT, "TOOLATOR Feedback / Bug Report")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = borderColor)
                        ActionRowItem(
                            icon = Icons.Default.Lock,
                            title = "Privacy Policy & Terms",
                            subtitle = "Read applicable terms and privacy",
                            textPrimary = textPrimary,
                            textSecondary = textSecondary,
                            onClick = {
                                Toast.makeText(context, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // 8. Open Source & Footer
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "OPEN SOURCE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = darkOrange,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Toolator uses trusted open-source libraries under their respective licenses.",
                            fontSize = 12.5.sp,
                            color = textSecondary,
                            lineHeight = 17.5.sp
                        )
                    }
                }
            }

            // 9. Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                                append("TOOL")
                            }
                            append(" ")
                            withStyle(style = SpanStyle(color = darkOrange, fontWeight = FontWeight.Bold)) {
                                append("ΛTOR")
                            }
                        },
                        fontSize = 13.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Crafted for precision & speed",
                        fontSize = 11.5.sp,
                        color = textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "© 2026 TOOLATOR. All rights reserved.",
                        fontSize = 10.5.sp,
                        color = textSecondary.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            confirmButton = {
                TextButton(onClick = { showUpdateDialog = false }) {
                    Text("OK", color = darkOrange, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Text(
                    text = "App Update",
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "You are on the latest version (v3.0.0 • Stable).\nNo new updates available right now.",
                    fontSize = 14.sp,
                    color = textSecondary
                )
            },
            containerColor = cardColor,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun FeatureRow(
    icon: String,
    title: String,
    desc: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = icon, fontSize = 18.sp)
        Column {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimary
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = textSecondary,
                lineHeight = 16.5.sp
            )
        }
    }
}

@Composable
private fun PurposeRow(
    tag: String,
    desc: String,
    accent: Color,
    textPrimary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(5.dp),
            color = accent.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.35f))
        ) {
            Text(
                text = tag,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
            )
        }
        Text(
            text = desc,
            fontSize = 12.5.sp,
            color = textPrimary,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textPrimary: Color,
    textSecondary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textSecondary,
                modifier = Modifier.size(19.dp)
            )
            Column {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = textSecondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = textSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}