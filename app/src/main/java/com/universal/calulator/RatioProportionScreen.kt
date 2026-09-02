package com.universal.calulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatioProportionScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    var selectedTab by remember { mutableStateOf("Simplify") } // Simplify, Proportion, Divide Total

    // Tab 1: Simplify
    var aSimp by remember { mutableStateOf("1920") }
    var bSimp by remember { mutableStateOf("1080") }

    // Tab 2: Proportion (A : B = C : D)
    var aProp by remember { mutableStateOf("4") }
    var bProp by remember { mutableStateOf("3") }
    var cProp by remember { mutableStateOf("16") }
    var dProp by remember { mutableStateOf("") }

    // Tab 3: Divide Total
    var totalAmountInput by remember { mutableStateOf("500") }
    var ratioParts by remember { mutableStateOf(listOf("2", "3", "5")) }

    fun fmt(n: Double): String = DecimalFormat("#,##0.##").format(n)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Ratio & Proportion",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3-Tab Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.surface)
                    .padding(3.dp)
            ) {
                listOf("Simplify", "Proportion", "Divide Total").forEach { tab ->
                    val isSelected = selectedTab == tab
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) theme.accent else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = tab }
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // TAB 1: SIMPLIFY
            if (selectedTab == "Simplify") {
                val valA = aSimp.toDoubleOrNull() ?: 1.0
                val valB = bSimp.toDoubleOrNull() ?: 1.0

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ENTER RATIO (A : B)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = aSimp,
                                onValueChange = { aSimp = it },
                                label = { Text("A", color = theme.textSecondary) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Text(":", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            OutlinedTextField(
                                value = bSimp,
                                onValueChange = { bSimp = it },
                                label = { Text("B", color = theme.textSecondary) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                if (valB != 0.0 && valA > 0 && valB > 0) {
                    val gcd = findGcdRatio((valA * 100).toLong(), (valB * 100).toLong())
                    val simA = ((valA * 100).toLong() / gcd).toDouble()
                    val simB = ((valB * 100).toLong() / gcd).toDouble()
                    val total = valA + valB
                    val pctA = (valA / total) * 100.0
                    val pctB = (valB / total) * 100.0

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("SIMPLIFIED RATIO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Text(
                                text = "${DecimalFormat("#.##").format(simA)} : ${DecimalFormat("#.##").format(simB)}",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.accent
                            )

                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Decimal Value", fontSize = 12.sp, color = theme.textSecondary)
                                Text(fmt(valA / valB), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Percentage Share", fontSize = 12.sp, color = theme.textSecondary)
                                Text("A: ${fmt(pctA)}% | B: ${fmt(pctB)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            }
                        }
                    }
                }
            }

            // TAB 2: PROPORTION (A : B = C : D)
            else if (selectedTab == "Proportion") {
                val vA = aProp.toDoubleOrNull()
                val vB = bProp.toDoubleOrNull()
                val vC = cProp.toDoubleOrNull()
                val vD = dProp.toDoubleOrNull()

                val missingCount = listOf(vA, vB, vC, vD).count { it == null }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ENTER 3 VALUES (LEAVE 1 EMPTY TO SOLVE)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = aProp,
                                onValueChange = { aProp = it },
                                label = { Text("A", fontSize = 10.sp, color = theme.textSecondary) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Text(":", fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            OutlinedTextField(
                                value = bProp,
                                onValueChange = { bProp = it },
                                label = { Text("B", fontSize = 10.sp, color = theme.textSecondary) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Text("=", fontWeight = FontWeight.Bold, color = theme.accent)
                            OutlinedTextField(
                                value = cProp,
                                onValueChange = { cProp = it },
                                label = { Text("C", fontSize = 10.sp, color = theme.textSecondary) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                            Text(":", fontWeight = FontWeight.Bold, color = theme.textPrimary)
                            OutlinedTextField(
                                value = dProp,
                                onValueChange = { dProp = it },
                                label = { Text("D", fontSize = 10.sp, color = theme.textSecondary) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (missingCount == 1) {
                            val (varName, solvedVal) = when {
                                vA == null && vB != null && vC != null && vD != null && vD != 0.0 -> Pair("A", (vB * vC) / vD)
                                vB == null && vA != null && vC != null && vD != null && vC != 0.0 -> Pair("B", (vA * vD) / vC)
                                vC == null && vA != null && vB != null && vD != null && vB != 0.0 -> Pair("C", (vA * vD) / vB)
                                vD == null && vA != null && vB != null && vC != null && vA != 0.0 -> Pair("D", (vB * vC) / vA)
                                else -> Pair("--", 0.0)
                            }
                            Text("SOLVED PROPORTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Text(text = "$varName = ${fmt(solvedVal)}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = theme.accent)
                        } else {
                            Text("Leave exactly 1 field empty to solve.", fontSize = 12.sp, color = theme.textSecondary)
                        }
                    }
                }
            }

            // TAB 3: DIVIDE TOTAL (SPLIT RATIO)
            else {
                val totalAmount = totalAmountInput.toDoubleOrNull() ?: 0.0
                val parsedParts = ratioParts.mapNotNull { it.toDoubleOrNull() }
                val partsSum = parsedParts.sum()
                val isValid = totalAmount > 0.0 && parsedParts.size >= 2 && partsSum > 0.0

                // Total Quantity Input Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TOTAL QUANTITY / AMOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                        OutlinedTextField(
                            value = totalAmountInput,
                            onValueChange = { totalAmountInput = it },
                            placeholder = { Text("e.g. 500", color = theme.textSecondary.copy(alpha = 0.5f)) },
                            textStyle = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedContainerColor = theme.numBtn,
                                unfocusedContainerColor = theme.numBtn,
                                focusedBorderColor = theme.accent,
                                unfocusedBorderColor = theme.funcBtn,
                                cursorColor = theme.accent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Dynamic Ratio Parts Input Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("RATIO PARTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)

                            // Add Part Button
                            if (ratioParts.size < 6) {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(theme.accent.copy(alpha = 0.15f))
                                        .clickable { ratioParts = ratioParts + "1" }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.accent, modifier = Modifier.size(14.dp))
                                    Text("Add Part", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                                }
                            }
                        }

                        ratioParts.forEachIndexed { index, partVal ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = partVal,
                                    onValueChange = { newVal ->
                                        ratioParts = ratioParts.toMutableList().also { it[index] = newVal }
                                    },
                                    label = { Text("Part ${index + 1}", color = theme.textSecondary) },
                                    textStyle = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = theme.textPrimary,
                                        unfocusedTextColor = theme.textPrimary,
                                        focusedContainerColor = theme.numBtn,
                                        unfocusedContainerColor = theme.numBtn,
                                        focusedBorderColor = theme.accent,
                                        unfocusedBorderColor = theme.funcBtn,
                                        cursorColor = theme.accent
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                if (ratioParts.size > 2) {
                                    IconButton(
                                        onClick = { ratioParts = ratioParts.filterIndexed { i, _ -> i != index } },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF5350))
                                    }
                                }
                            }
                        }
                    }
                }

                // Split Result Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("DIVIDED SHARES", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)

                        if (isValid) {
                            var verificationSum = 0.0
                            val shareStrings = mutableListOf<String>()

                            parsedParts.forEachIndexed { index, part ->
                                val share = (part / partsSum) * totalAmount
                                val pct = (part / partsSum) * 100.0
                                verificationSum += share
                                shareStrings.add(fmt(share))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Part ${index + 1} (${part.toInt()})",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.textPrimary
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = fmt(share),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent
                                        )
                                        Text(
                                            text = "${fmt(pct)}%",
                                            fontSize = 10.sp,
                                            color = theme.textSecondary
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 2.dp))

                            // Verification Sum Line
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${shareStrings.joinToString(" + ")} = ${fmt(verificationSum)} ✓",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        } else {
                            Text(
                                text = "Enter a valid positive total amount and ratio parts.",
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

private fun findGcdRatio(a: Long, b: Long): Long = if (b == 0L) abs(a) else findGcdRatio(b, a % b)