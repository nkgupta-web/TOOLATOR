package com.universal.calulator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow

data class LoanCompareItem(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    var name: String,
    var principal: String,
    var annualRate: String,
    var tenure: String,
    var isTenureInYears: Boolean = true
)

data class LoanCalculationResult(
    val loan: LoanCompareItem,
    val emi: Double,
    val totalInterest: Double,
    val totalPayable: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanCompareScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    var selectedTab by remember { mutableIntStateOf(0) }

    // Single Loan States
    var singlePrincipal by remember { mutableStateOf("1000000") }
    var singleRate by remember { mutableStateOf("8.5") }
    var singleTenure by remember { mutableStateOf("5") }
    var singleTenureInYears by remember { mutableStateOf(true) }

    // Multi-Loan Compare States
    var compareLoans by remember {
        mutableStateOf(
            listOf(
                LoanCompareItem(name = "Loan A", principal = "1000000", annualRate = "8.5", tenure = "5", isTenureInYears = true),
                LoanCompareItem(name = "Loan B", principal = "1000000", annualRate = "8.0", tenure = "7", isTenureInYears = true)
            )
        )
    }

    var showWhyWinner by remember { mutableStateOf(false) }

    fun calculateEmi(p: Double, annualRate: Double, tenureValue: Double, isYears: Boolean): Triple<Double, Double, Double> {
        if (p <= 0.0 || tenureValue <= 0.0) return Triple(0.0, 0.0, 0.0)
        val n = if (isYears) tenureValue * 12.0 else tenureValue
        if (annualRate <= 0.0) {
            val emi = p / n
            return Triple(emi, 0.0, p)
        }
        val r = (annualRate / 12.0) / 100.0
        val factor = (1.0 + r).pow(n)
        val emi = (p * r * factor) / (factor - 1.0)
        val totalPayable = emi * n
        val totalInterest = totalPayable - p
        return Triple(emi, totalInterest, totalPayable)
    }

    fun formatCurrency(amount: Double): String {
        return try {
            val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
            val df = DecimalFormat("#,##,###.##")
            val formatted = df.format(bd)
            if (!formatted.contains(".")) "$formatted.00" else formatted
        } catch (_: Exception) {
            amount.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Loan & EMI Planner",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(8.dp))

        // Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(theme.surface)
                .padding(4.dp)
        ) {
            listOf("Single EMI", "Compare Multi-Loans").forEachIndexed { index, label ->
                val isSelected = selectedTab == index
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) theme.accent else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = index }
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                        modifier = Modifier.padding(vertical = 9.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedTab == 0) {
                // ================= SINGLE EMI =================
                val p = singlePrincipal.toDoubleOrNull() ?: 0.0
                val r = singleRate.toDoubleOrNull() ?: 0.0
                val t = singleTenure.toDoubleOrNull() ?: 0.0
                val (emi, interest, payable) = calculateEmi(p, r, t, singleTenureInYears)

                val heroScrollState = rememberScrollState()
                val interestScrollState = rememberScrollState()
                val payableScrollState = rememberScrollState()

                val formattedEmi = remember(emi) { formatCurrency(emi) }
                val formattedInterest = remember(interest) { formatCurrency(interest) }
                val formattedPayable = remember(payable) { formatCurrency(payable) }

                val totalMaturityBase = if (payable > 0.0) payable else p
                val rawPrincipalRatio = if (totalMaturityBase > 0.0) (p / totalMaturityBase).toFloat() else 1f
                val targetRatio = if (interest <= 0.0) 1f else rawPrincipalRatio.coerceIn(0.02f, 0.98f)

                val animatedPrincipalWeight by animateFloatAsState(
                    targetValue = targetRatio,
                    animationSpec = tween(durationMillis = 250),
                    label = "LoanRatioAnim"
                )

                val heroFontSize = when {
                    formattedEmi.length <= 8 -> 34.sp
                    formattedEmi.length <= 13 -> 26.sp
                    formattedEmi.length <= 18 -> 20.sp
                    else -> 18.sp
                }

                // Result Summary Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Monthly EMI",
                            fontSize = 13.sp,
                            color = theme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(heroScrollState),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "₹ $formattedEmi",
                                fontSize = heroFontSize,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Dynamic Share Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape)
                                .background(theme.textSecondary.copy(alpha = 0.35f))
                        ) {
                            if (interest <= 0.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(theme.accent)
                                )
                            } else {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(animatedPrincipalWeight)
                                            .fillMaxHeight()
                                            .background(theme.accent)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight((1f - animatedPrincipalWeight).coerceAtLeast(0.001f))
                                            .fillMaxHeight()
                                            .background(theme.textSecondary.copy(alpha = 0.35f))
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 2-Column Side by Side Breakdown Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = theme.numBtn.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Total Interest",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(interestScrollState)
                                    ) {
                                        Text(
                                            text = "₹ $formattedInterest",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = theme.numBtn.copy(alpha = 0.5f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Total Payable",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(payableScrollState)
                                    ) {
                                        Text(
                                            text = "₹ $formattedPayable",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Input Form Card
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(text = "Loan Amount (₹)", fontSize = 13.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = singlePrincipal,
                                onValueChange = { if (it.length <= 15) singlePrincipal = it },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 16.sp,
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier.height(28.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(text = "Interest Rate (% p.a.)", fontSize = 13.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                                }
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = singleRate,
                                    onValueChange = { if (it.length <= 5) singleRate = it },
                                    textStyle = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 16.sp,
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
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(28.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Tenure", fontSize = 13.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.numBtn)
                                            .padding(2.dp)
                                    ) {
                                        listOf(true to "Yr", false to "Mo").forEach { (isYr, label) ->
                                            val isSel = singleTenureInYears == isYr
                                            Text(
                                                text = label,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(if (isSel) theme.accent else Color.Transparent)
                                                    .clickable { singleTenureInYears = isYr }
                                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = singleTenure,
                                    onValueChange = { if (it.length <= 4) singleTenure = it },
                                    textStyle = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 16.sp,
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
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            } else {
                // ================= MULTI-LOAN COMPARE =================
                val results = remember(compareLoans) {
                    compareLoans.map { loan ->
                        val p = loan.principal.toDoubleOrNull() ?: 0.0
                        val r = loan.annualRate.toDoubleOrNull() ?: 0.0
                        val t = loan.tenure.toDoubleOrNull() ?: 0.0
                        val (emi, interest, payable) = calculateEmi(p, r, t, loan.isTenureInYears)
                        LoanCalculationResult(loan, emi, interest, payable)
                    }
                }

                // 1. Input Cards with Unified Baseline Headers
                compareLoans.forEachIndexed { index, loan ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Card Title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = theme.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    BasicTextField(
                                        value = loan.name,
                                        onValueChange = { newName ->
                                            compareLoans = compareLoans.toMutableList().also {
                                                it[index] = it[index].copy(name = newName)
                                            }
                                        },
                                        textStyle = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent
                                        ),
                                        cursorBrush = SolidColor(theme.accent),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (compareLoans.size > 2) {
                                    IconButton(
                                        onClick = {
                                            compareLoans = compareLoans.toMutableList().also { it.removeAt(index) }
                                        },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Loan",
                                            tint = theme.textSecondary
                                        )
                                    }
                                }
                            }

                            // 3 Perfectly Aligned Inputs with matched Header Heights
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                // 1. Amount
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "Amount (₹)",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = loan.principal,
                                        onValueChange = { valStr ->
                                            compareLoans = compareLoans.toMutableList().also {
                                                it[index] = it[index].copy(principal = valStr)
                                            }
                                        },
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 2. Rate
                                Column(modifier = Modifier.weight(0.9f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = "Rate %",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = loan.annualRate,
                                        onValueChange = { valStr ->
                                            compareLoans = compareLoans.toMutableList().also {
                                                it[index] = it[index].copy(annualRate = valStr)
                                            }
                                        },
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
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // 3. Tenure
                                Column(modifier = Modifier.weight(1.1f)) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Tenure",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(theme.numBtn)
                                                .padding(1.dp)
                                        ) {
                                            listOf(true to "Yr", false to "Mo").forEach { (isYr, lbl) ->
                                                val isSelected = loan.isTenureInYears == isYr
                                                Text(
                                                    text = lbl,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isSelected) theme.accent else Color.Transparent)
                                                        .clickable {
                                                            compareLoans = compareLoans.toMutableList().also {
                                                                it[index] = it[index].copy(isTenureInYears = isYr)
                                                            }
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = loan.tenure,
                                        onValueChange = { valStr ->
                                            compareLoans = compareLoans.toMutableList().also {
                                                it[index] = it[index].copy(tenure = valStr)
                                            }
                                        },
                                        placeholder = { Text("Duration", fontSize = 11.sp, color = theme.textSecondary.copy(alpha = 0.5f)) },
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Add Loan Button
                Button(
                    onClick = {
                        val nextLetter = ('A' + compareLoans.size).toString()
                        compareLoans = compareLoans + LoanCompareItem(
                            name = "Loan $nextLetter",
                            principal = "1000000",
                            annualRate = "8.0",
                            tenure = "5",
                            isTenureInYears = true
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.funcBtn),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.accent)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Another Loan", color = theme.accent, fontWeight = FontWeight.SemiBold)
                }

                // 3. Comparative Breakdown
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "COMPARATIVE BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary,
                            letterSpacing = 1.sp
                        )

                        results.forEachIndexed { idx, res ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = theme.numBtn,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = res.loan.name.ifEmpty { "Option ${idx + 1}" },
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent
                                        )
                                        Text(
                                            text = "₹ ${formatCurrency(res.emi)} / mo",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary
                                        )
                                    }

                                    HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Total Interest:", fontSize = 12.sp, color = theme.textSecondary)
                                        Text(text = "₹ ${formatCurrency(res.totalInterest)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = theme.textPrimary)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Total Payable:", fontSize = 12.sp, color = theme.textSecondary)
                                        Text(text = "₹ ${formatCurrency(res.totalPayable)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Quick Comparison Matrix
                if (results.size >= 2 && results.all { it.totalPayable > 0.0 }) {
                    val lowestInterestOption = results.minByOrNull { it.totalInterest }
                    val highestInterestOption = results.maxByOrNull { it.totalInterest }
                    val lowestEmiOption = results.minByOrNull { it.emi }
                    val highestEmiOption = results.maxByOrNull { it.emi }

                    val interestSavings = if (lowestInterestOption != null && highestInterestOption != null) {
                        highestInterestOption.totalInterest - lowestInterestOption.totalInterest
                    } else 0.0

                    val emiDiff = if (lowestEmiOption != null && highestEmiOption != null) {
                        highestEmiOption.emi - lowestEmiOption.emi
                    } else 0.0

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "QUICK COMPARISON MATRIX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lowest Monthly EMI", fontSize = 12.sp, color = theme.textSecondary)
                                Text(
                                    text = "${lowestEmiOption?.loan?.name} · ₹${formatCurrency(lowestEmiOption?.emi ?: 0.0)}/mo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accent
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Lowest Total Interest", fontSize = 12.sp, color = theme.textSecondary)
                                Text(
                                    text = "${lowestInterestOption?.loan?.name} · ₹${formatCurrency(lowestInterestOption?.totalInterest ?: 0.0)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                            }

                            if (interestSavings > 0.0 && lowestInterestOption != null) {
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Maximum Interest Saved", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = theme.accent)
                                    Text("₹ ${formatCurrency(interestSavings)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                                }
                            }
                        }
                    }

                    // 5. Winner & Recommendation
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.accent.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = theme.accent, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Recommendation & Winner",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accent
                                )
                            }

                            if (lowestInterestOption != null && lowestEmiOption != null) {
                                if (lowestInterestOption.loan.id == lowestEmiOption.loan.id) {
                                    Text(
                                        text = "🏆 Best Overall — ${lowestInterestOption.loan.name}\nLowest total borrowing cost. Saves ₹${formatCurrency(interestSavings)} in interest with the lowest monthly EMI.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = theme.textPrimary,
                                        lineHeight = 17.sp
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "🏆 Best Overall — ${lowestInterestOption.loan.name}\nLowest total borrowing cost. Save ₹${formatCurrency(interestSavings)} in interest compared to ${highestInterestOption?.loan?.name ?: "others"}.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.textPrimary,
                                            lineHeight = 17.sp
                                        )

                                        Text(
                                            text = "💡 Best for Lower EMI — ${lowestEmiOption.loan.name}\nPay ₹${formatCurrency(emiDiff)} less per month, but pay more interest overall.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.textSecondary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = theme.accent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showWhyWinner = !showWhyWinner }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Info, contentDescription = null, tint = theme.accent, modifier = Modifier.size(15.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = "Why is ${lowestInterestOption.loan.name} recommended?",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.accent
                                        )
                                    }
                                    Icon(
                                        imageVector = if (showWhyWinner) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = theme.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                AnimatedVisibility(visible = showWhyWinner) {
                                    val other = if (lowestInterestOption.loan.id == compareLoans[0].id) results[1] else results[0]
                                    val rateDiff = abs((lowestInterestOption.loan.annualRate.toDoubleOrNull() ?: 0.0) - (other.loan.annualRate.toDoubleOrNull() ?: 0.0))
                                    val payableDiff = abs(other.totalPayable - lowestInterestOption.totalPayable)

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .background(theme.surface, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ReasoningRow(label = "Interest Rate Gap", value = "${String.format("%.2f", rateDiff)}% difference")
                                        ReasoningRow(label = "Monthly EMI Gap", value = "₹ ${formatCurrency(abs(other.emi - lowestInterestOption.emi))} / mo")
                                        ReasoningRow(label = "Total Interest Saved", value = "₹ ${formatCurrency(interestSavings)}")
                                        ReasoningRow(label = "Total Net Savings", value = "₹ ${formatCurrency(payableDiff)}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReasoningRow(label: String, value: String) {
    val theme = LocalAppTheme.current.value
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = theme.textSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
    }
}