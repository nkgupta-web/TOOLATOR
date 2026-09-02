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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow

enum class InvestmentType {
    SIP, LUMP_SUM
}

data class InvestmentCompareItem(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    var name: String,
    var type: InvestmentType = InvestmentType.SIP,
    var amount: String,
    var expectedReturnRate: String,
    var duration: String,
    var isDurationInYears: Boolean = true
)

data class InvestmentCalculationResult(
    val item: InvestmentCompareItem,
    val totalInvested: Double,
    val estimatedReturns: Double,
    val totalWealth: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentCompareScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Single Investment, 1: Compare Investments

    // Single Investment States
    var singleType by remember { mutableStateOf(InvestmentType.SIP) }
    var singleAmount by remember { mutableStateOf("5000") }
    var singleRate by remember { mutableStateOf("12.0") }
    var singleDuration by remember { mutableStateOf("3") }
    var singleDurationInYears by remember { mutableStateOf(true) }

    // Multi-Investment Compare States
    var compareItems by remember {
        mutableStateOf(
            listOf(
                InvestmentCompareItem(
                    name = "Investment A",
                    type = InvestmentType.SIP,
                    amount = "5000",
                    expectedReturnRate = "12.0",
                    duration = "3",
                    isDurationInYears = true
                ),
                InvestmentCompareItem(
                    name = "Investment B",
                    type = InvestmentType.LUMP_SUM,
                    amount = "180000",
                    expectedReturnRate = "12.0",
                    duration = "3",
                    isDurationInYears = true
                )
            )
        )
    }

    var showWhyWinner by remember { mutableStateOf(false) }

    fun calculateInvestment(
        type: InvestmentType,
        amount: Double,
        annualRate: Double,
        duration: Double,
        isYears: Boolean
    ): Triple<Double, Double, Double> {
        if (amount <= 0.0 || duration <= 0.0) return Triple(0.0, 0.0, 0.0)

        val totalMonths = if (isYears) duration * 12.0 else duration
        val years = totalMonths / 12.0

        return if (type == InvestmentType.SIP) {
            val monthlyRate = (annualRate / 12.0) / 100.0
            val totalInvested = amount * totalMonths
            val totalWealth = if (monthlyRate > 0.0) {
                amount * (((1.0 + monthlyRate).pow(totalMonths) - 1.0) / monthlyRate) * (1.0 + monthlyRate)
            } else {
                totalInvested
            }
            val estimatedReturns = totalWealth - totalInvested
            Triple(totalInvested, estimatedReturns, totalWealth)
        } else {
            val totalInvested = amount
            val r = annualRate / 100.0
            val totalWealth = amount * (1.0 + r).pow(years)
            val estimatedReturns = totalWealth - totalInvested
            Triple(totalInvested, estimatedReturns, totalWealth)
        }
    }

    fun formatCurrency(amount: Double): String {
        return try {
            val bd = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP)
            val df = DecimalFormat("#,##,##0.00")
            df.format(bd)
        } catch (_: Exception) {
            String.format("%.2f", amount)
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
                text = "Investment / SIP Planner",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(8.dp))

        // Top Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(theme.surface)
                .padding(4.dp)
        ) {
            listOf("Single Investment", "Compare Investments").forEachIndexed { index, label ->
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
                // ================= SINGLE INVESTMENT =================
                val amt = singleAmount.toDoubleOrNull() ?: 0.0
                val r = singleRate.toDoubleOrNull() ?: 0.0
                val d = singleDuration.toDoubleOrNull() ?: 0.0
                val (invested, returns, wealth) = calculateInvestment(singleType, amt, r, d, singleDurationInYears)

                val heroScrollState = rememberScrollState()
                val returnsScrollState = rememberScrollState()
                val wealthScrollState = rememberScrollState()

                val formattedInvested = remember(invested) { formatCurrency(invested) }
                val formattedReturns = remember(returns) { formatCurrency(returns) }
                val formattedWealth = remember(wealth) { formatCurrency(wealth) }

                val totalMaturityBase = if (wealth > 0.0) wealth else invested
                val rawPrincipalRatio = if (totalMaturityBase > 0.0) (invested / totalMaturityBase).toFloat() else 1f
                val targetRatio = if (returns <= 0.0) 1f else rawPrincipalRatio.coerceIn(0.02f, 0.98f)

                val animatedInvestedWeight by animateFloatAsState(
                    targetValue = targetRatio,
                    animationSpec = tween(durationMillis = 250),
                    label = "InvestRatioAnim"
                )

                val heroFontSize = when {
                    formattedInvested.length <= 8 -> 34.sp
                    formattedInvested.length <= 13 -> 26.sp
                    formattedInvested.length <= 18 -> 20.sp
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
                            text = "Total Invested",
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
                                text = "₹ $formattedInvested",
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
                            if (returns <= 0.0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(theme.accent)
                                )
                            } else {
                                Row(modifier = Modifier.fillMaxSize()) {
                                    Box(
                                        modifier = Modifier
                                            .weight(animatedInvestedWeight)
                                            .fillMaxHeight()
                                            .background(theme.accent)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight((1f - animatedInvestedWeight).coerceAtLeast(0.001f))
                                            .fillMaxHeight()
                                            .background(theme.textSecondary.copy(alpha = 0.35f))
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 2-Column Breakdown Cards
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
                                        text = "Estimated Returns",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(returnsScrollState)
                                    ) {
                                        Text(
                                            text = "₹ $formattedReturns",
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
                                        text = "Total Wealth / Maturity",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(wealthScrollState)
                                    ) {
                                        Text(
                                            text = "₹ $formattedWealth",
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
                        // SIP vs Lump Sum Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.numBtn)
                                .padding(2.dp)
                        ) {
                            listOf(InvestmentType.SIP to "SIP", InvestmentType.LUMP_SUM to "Lump Sum").forEach { (type, label) ->
                                val isSel = singleType == type
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) theme.accent else Color.Transparent)
                                        .clickable { singleType = type }
                                        .padding(vertical = 6.dp)
                                )
                            }
                        }

                        // Amount Input
                        Column {
                            Text(
                                text = if (singleType == InvestmentType.SIP) "Monthly Investment (₹)" else "Investment Amount (₹)",
                                fontSize = 12.sp,
                                color = theme.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = singleAmount,
                                onValueChange = { singleAmount = it },
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Rate & Duration Inputs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Expected Return (% p.a.)", fontSize = 12.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = singleRate,
                                    onValueChange = { singleRate = it },
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
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Duration", fontSize = 12.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(theme.numBtn)
                                            .padding(2.dp)
                                    ) {
                                        listOf(true to "Yr", false to "Mo").forEach { (isYr, label) ->
                                            val isSel = singleDurationInYears == isYr
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (isSel) theme.accent else Color.Transparent)
                                                    .clickable { singleDurationInYears = isYr }
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = singleDuration,
                                    onValueChange = { singleDuration = it },
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
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (singleType == InvestmentType.SIP) {
                            Text(
                                text = "ⓘ SIP assumes investment at the beginning of each month.",
                                fontSize = 10.sp,
                                color = theme.textSecondary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                // ================= MULTI-INVESTMENT COMPARE =================
                val results = remember(compareItems) {
                    compareItems.map { item ->
                        val amt = item.amount.toDoubleOrNull() ?: 0.0
                        val r = item.expectedReturnRate.toDoubleOrNull() ?: 0.0
                        val d = item.duration.toDoubleOrNull() ?: 0.0
                        val (invested, returns, wealth) = calculateInvestment(item.type, amt, r, d, item.isDurationInYears)
                        InvestmentCalculationResult(item, invested, returns, wealth)
                    }
                }

                // 1. Dynamic Input Cards (Original Clean Labels & Alignment)
                compareItems.forEachIndexed { index, item ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
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
                            // Header Row: Title & Remove
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
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    BasicTextField(
                                        value = item.name,
                                        onValueChange = { newName ->
                                            compareItems = compareItems.toMutableList().also {
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

                                if (compareItems.size > 2) {
                                    IconButton(
                                        onClick = {
                                            compareItems = compareItems.toMutableList().also { it.removeAt(index) }
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Option",
                                            tint = theme.textSecondary
                                        )
                                    }
                                }
                            }

                            // Card-Level SIP vs Lump Sum Switch
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(theme.numBtn)
                                    .padding(2.dp)
                            ) {
                                listOf(InvestmentType.SIP to "SIP", InvestmentType.LUMP_SUM to "Lump Sum").forEach { (type, lbl) ->
                                    val isSel = item.type == type
                                    Text(
                                        text = lbl,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) theme.accent else Color.Transparent)
                                            .clickable {
                                                compareItems = compareItems.toMutableList().also {
                                                    it[index] = it[index].copy(type = type)
                                                }
                                            }
                                            .padding(vertical = 4.dp)
                                    )
                                }
                            }

                            // Dynamic Inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1.3f)) {
                                    Text(
                                        text = if (item.type == InvestmentType.SIP) "Monthly (₹)" else "Amount (₹)",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = item.amount,
                                        onValueChange = { valStr ->
                                            compareItems = compareItems.toMutableList().also {
                                                it[index] = it[index].copy(amount = valStr)
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

                                Column(modifier = Modifier.weight(0.9f)) {
                                    Text(
                                        text = "Return %",
                                        fontSize = 11.sp,
                                        color = theme.textSecondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = item.expectedReturnRate,
                                        onValueChange = { valStr ->
                                            compareItems = compareItems.toMutableList().also {
                                                it[index] = it[index].copy(expectedReturnRate = valStr)
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

                                Column(modifier = Modifier.weight(1.1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Duration",
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
                                                val isSelected = item.isDurationInYears == isYr
                                                Text(
                                                    text = lbl,
                                                    fontSize = 9.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isSelected) theme.accent else Color.Transparent)
                                                        .clickable {
                                                            compareItems = compareItems.toMutableList().also {
                                                                it[index] = it[index].copy(isDurationInYears = isYr)
                                                            }
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    OutlinedTextField(
                                        value = item.duration,
                                        onValueChange = { valStr ->
                                            compareItems = compareItems.toMutableList().also {
                                                it[index] = it[index].copy(duration = valStr)
                                            }
                                        },
                                        placeholder = { Text("Duration", fontSize = 10.sp, color = theme.textSecondary.copy(alpha = 0.5f)) },
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

                // 2. Add Another Investment Button
                Button(
                    onClick = {
                        val nextLetter = ('A' + compareItems.size).toString()
                        compareItems = compareItems + InvestmentCompareItem(
                            name = "Investment $nextLetter",
                            type = InvestmentType.SIP,
                            amount = "5000",
                            expectedReturnRate = "12.0",
                            duration = "3",
                            isDurationInYears = true
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.funcBtn),
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = theme.accent)
                    Spacer(Modifier.width(6.dp))
                    Text("Add Another Investment", color = theme.accent, fontWeight = FontWeight.SemiBold)
                }

                // 3. Comparative Breakdown Cards
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
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = res.item.name.ifEmpty { "Option ${idx + 1}" },
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.accent,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.funcBtn
                                            ) {
                                                Text(
                                                    text = if (res.item.type == InvestmentType.SIP) "SIP" else "LUMP SUM",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = theme.textSecondary,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = "₹ ${formatCurrency(res.totalWealth)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }

                                    HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Total Invested:", fontSize = 12.sp, color = theme.textSecondary)
                                        Text(text = "₹ ${formatCurrency(res.totalInvested)}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = theme.textPrimary)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "Estimated Returns:", fontSize = 12.sp, color = theme.textSecondary)
                                        Text(text = "₹ ${formatCurrency(res.estimatedReturns)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Quick Comparison Matrix
                if (results.size >= 2 && results.all { it.totalWealth > 0.0 }) {
                    val highestWealthOption = results.maxByOrNull { it.totalWealth }
                    val lowestWealthOption = results.minByOrNull { it.totalWealth }
                    val highestReturnsOption = results.maxByOrNull { it.estimatedReturns }

                    val maxWealth = highestWealthOption?.totalWealth ?: 0.0
                    val minWealth = lowestWealthOption?.totalWealth ?: 0.0
                    val extraWealth = maxWealth - minWealth

                    Surface(
                        shape = RoundedCornerShape(18.dp),
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
                            Text(
                                text = "QUICK COMPARISON MATRIX",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )

                            // Higher Final Wealth
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Higher Final Wealth",
                                    fontSize = 12.sp,
                                    color = theme.textSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = highestWealthOption?.item?.name ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.accent
                                    )
                                    Text(
                                        text = "₹ ${formatCurrency(highestWealthOption?.totalWealth ?: 0.0)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.textPrimary
                                    )
                                }
                            }

                            // Higher Estimated Returns
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Higher Estimated Returns",
                                    fontSize = 12.sp,
                                    color = theme.textSecondary,
                                    modifier = Modifier.weight(1f)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = highestReturnsOption?.item?.name ?: "",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.accent
                                    )
                                    Text(
                                        text = "₹ ${formatCurrency(highestReturnsOption?.estimatedReturns ?: 0.0)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.textPrimary
                                    )
                                }
                            }

                            // Investment Timing Layout
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Investment Timing", fontSize = 12.sp, color = theme.textSecondary)
                                results.forEach { res ->
                                    val timingText = if (res.item.type == InvestmentType.SIP) {
                                        "₹${formatCurrency(res.item.amount.toDoubleOrNull() ?: 0.0)}/month"
                                    } else {
                                        "₹${formatCurrency(res.totalInvested)} upfront"
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = "• ${res.item.name}", fontSize = 11.sp, color = theme.textSecondary)
                                        Text(text = timingText, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = theme.textPrimary)
                                    }
                                }
                            }

                            if (extraWealth > 0.0 && highestWealthOption != null) {
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Extra Wealth Created", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = theme.accent)
                                    Text("₹ ${formatCurrency(extraWealth)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                                }
                            }
                        }
                    }

                    // 5. Dynamic Winner & Recommendation Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
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

                            if (highestWealthOption != null) {
                                val sipOption = results.firstOrNull { it.item.type == InvestmentType.SIP }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (highestWealthOption.item.type == InvestmentType.LUMP_SUM && sipOption != null) {
                                        Text(
                                            text = "🏆 Best Wealth Creator — ${highestWealthOption.item.name}\nLump Sum generates the higher estimated maturity value because the full capital is invested from the beginning and remains invested for the entire tenure.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.textPrimary,
                                            lineHeight = 17.sp
                                        )

                                        Text(
                                            text = "💡 Best for Cash Flow — ${sipOption.item.name}\nSIP spreads the total investment over time, reducing the upfront cash requirement.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.textSecondary,
                                            lineHeight = 17.sp
                                        )
                                    } else {
                                        Text(
                                            text = "🏆 Best Wealth Creator — ${highestWealthOption.item.name}\nGenerates the highest maturity value of ₹${formatCurrency(highestWealthOption.totalWealth)}, creating ₹${formatCurrency(extraWealth)} more total wealth.",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = theme.textPrimary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }

                                HorizontalDivider(color = theme.accent.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))

                                // Dynamic Expandable Trigger
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
                                            text = "Why is ${highestWealthOption.item.name} recommended?",
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

                                // Detailed Dynamic Reasoning
                                AnimatedVisibility(visible = showWhyWinner) {
                                    val other = if (highestWealthOption.item.id == compareItems[0].id) results[1] else results[0]

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 6.dp)
                                            .background(theme.surface, RoundedCornerShape(10.dp))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Why ${highestWealthOption.item.name} Wins",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent
                                        )

                                        val samePrincipal = abs(highestWealthOption.totalInvested - other.totalInvested) < 0.01
                                        val sameRate = highestWealthOption.item.expectedReturnRate == other.item.expectedReturnRate

                                        ReasoningRow(
                                            label = "Total Investment",
                                            value = if (samePrincipal) "₹${formatCurrency(highestWealthOption.totalInvested)} (Both)" else "₹${formatCurrency(highestWealthOption.totalInvested)} vs ₹${formatCurrency(other.totalInvested)}"
                                        )
                                        ReasoningRow(
                                            label = "Expected Return",
                                            value = if (sameRate) "${highestWealthOption.item.expectedReturnRate}% p.a." else "${highestWealthOption.item.expectedReturnRate}% vs ${other.item.expectedReturnRate}%"
                                        )
                                        ReasoningRow(
                                            label = "Duration",
                                            value = "${highestWealthOption.item.duration} ${if (highestWealthOption.item.isDurationInYears) "Years" else "Months"}"
                                        )

                                        HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 2.dp))

                                        val isSipVsLump = (highestWealthOption.item.type == InvestmentType.LUMP_SUM && other.item.type == InvestmentType.SIP) ||
                                                (highestWealthOption.item.type == InvestmentType.SIP && other.item.type == InvestmentType.LUMP_SUM)

                                        if (isSipVsLump) {
                                            val lumpRes = if (highestWealthOption.item.type == InvestmentType.LUMP_SUM) highestWealthOption else other
                                            val sipRes = if (highestWealthOption.item.type == InvestmentType.SIP) highestWealthOption else other

                                            ReasoningRow(label = "${sipRes.item.name} Mode", value = "₹${formatCurrency(sipRes.item.amount.toDoubleOrNull() ?: 0.0)}/month")
                                            ReasoningRow(label = "${lumpRes.item.name} Mode", value = "₹${formatCurrency(lumpRes.totalInvested)} upfront")
                                            ReasoningRow(label = "${lumpRes.item.name} Returns", value = "₹${formatCurrency(lumpRes.estimatedReturns)}")
                                            ReasoningRow(label = "${sipRes.item.name} Returns", value = "₹${formatCurrency(sipRes.estimatedReturns)}")
                                            ReasoningRow(label = "Extra Estimated Returns", value = "₹${formatCurrency(extraWealth)}")

                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = "The difference occurs because the Lump Sum invests the full capital from the beginning, allowing more of the capital to earn returns for the full tenure.",
                                                fontSize = 11.sp,
                                                color = theme.textSecondary,
                                                lineHeight = 16.sp
                                            )
                                        } else {
                                            ReasoningRow(label = "${highestWealthOption.item.name} Returns", value = "₹${formatCurrency(highestWealthOption.estimatedReturns)}")
                                            ReasoningRow(label = "${other.item.name} Returns", value = "₹${formatCurrency(other.estimatedReturns)}")
                                            ReasoningRow(label = "Extra Estimated Returns", value = "₹${formatCurrency(extraWealth)}")
                                        }
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
        Text(text = label, fontSize = 11.sp, color = theme.textSecondary)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
    }
}