package com.universal.calulator

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceCalculatorsScreen(
    toolId: String,
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val tid = toolId.lowercase()

    val screenTitle = when (tid) {
        "emi", "loan_planner" -> "Loan / EMI Calculator"
        "sip", "inv" -> "Investment / SIP"
        "simple_interest", "si" -> "Simple Interest"
        "compound_interest", "ci" -> "Compound Interest"
        else -> "Finance Calculator"
    }

    var principalInput by remember { mutableStateOf(if (tid in listOf("sip", "inv")) "5000" else "100000") }
    var rateInput by remember { mutableStateOf(if (tid in listOf("sip", "inv")) "12" else if (tid in listOf("emi", "loan_planner")) "8.5" else "10") }
    var tenureInput by remember { mutableStateOf(if (tid in listOf("emi", "loan_planner")) "5" else "3") }
    var tenureType by remember { mutableStateOf("Years") }
    var compoundingFrequency by remember { mutableStateOf("Yearly") }

    fun formatWithCommas(raw: Double): String {
        return try {
            val df = DecimalFormat("#,##,###")
            df.format(raw.toLong())
        } catch (_: Exception) {
            raw.toLong().toString()
        }
    }

    val P = principalInput.toDoubleOrNull() ?: 0.0
    val R = rateInput.toDoubleOrNull() ?: 0.0
    val T = tenureInput.toDoubleOrNull() ?: 0.0
    val totalYears = if (tenureType == "Years") T else T / 12.0
    val totalMonths = if (tenureType == "Years") T * 12.0 else T

    var card1Title = ""
    var card1Val = 0.0

    var card2Title = ""
    var card2Val = 0.0

    var card3Title = ""
    var card3Val = 0.0

    when (tid) {
        "emi", "loan_planner" -> {
            val monthlyRate = (R / 12.0) / 100.0
            val n = totalMonths
            val emi = if (monthlyRate > 0 && n > 0) {
                (P * monthlyRate * (1 + monthlyRate).pow(n)) / ((1 + monthlyRate).pow(n) - 1)
            } else if (n > 0) P / n else 0.0

            val totalPayment = emi * n
            val totalInterest = (totalPayment - P).coerceAtLeast(0.0)

            card1Title = "Monthly EMI"
            card1Val = emi
            card2Title = "Total Interest"
            card2Val = totalInterest
            card3Title = "Total Payable"
            card3Val = totalPayment
        }
        "sip", "inv" -> {
            val monthlyRate = (R / 12.0) / 100.0
            val n = totalMonths
            val invested = P * n
            val futureValue = if (monthlyRate > 0 && n > 0) {
                P * (((1 + monthlyRate).pow(n) - 1) / monthlyRate) * (1 + monthlyRate)
            } else invested
            val returns = (futureValue - invested).coerceAtLeast(0.0)

            card1Title = "Total Invested"
            card1Val = invested
            card2Title = "Estimated Returns"
            card2Val = returns
            card3Title = "Total Wealth"
            card3Val = futureValue
        }
        "simple_interest", "si" -> {
            val interest = (P * R * totalYears) / 100.0
            val totalAmount = P + interest

            card1Title = "Principal Amount"
            card1Val = P
            card2Title = "Total Interest"
            card2Val = interest
            card3Title = "Total Maturity"
            card3Val = totalAmount
        }
        "compound_interest", "ci" -> {
            val n = when (compoundingFrequency) {
                "Half-Yearly" -> 2.0
                "Every 4M" -> 3.0
                "Quarterly" -> 4.0
                "Every 2M" -> 6.0
                "Monthly" -> 12.0
                else -> 1.0
            }
            val amount = if (n > 0 && totalYears > 0) {
                P * (1 + (R / (100.0 * n))).pow(n * totalYears)
            } else P
            val interest = (amount - P).coerceAtLeast(0.0)

            card1Title = "Principal Amount"
            card1Val = P
            card2Title = "Compound Interest"
            card2Val = interest
            card3Title = "Total Maturity"
            card3Val = amount
        }
    }

    val principalFieldLabel = when (tid) {
        "emi", "loan_planner" -> "Loan Amount (₹)"
        "sip", "inv" -> "Monthly Investment (₹)"
        else -> "Principal Amount (₹)"
    }

    val card1Formatted = remember(card1Val) { formatWithCommas(card1Val) }
    val card2Formatted = remember(card2Val) { formatWithCommas(card2Val) }
    val card3Formatted = remember(card3Val) { formatWithCommas(card3Val) }

    val heroScrollState = rememberScrollState()
    val sub1ScrollState = rememberScrollState()
    val sub2ScrollState = rememberScrollState()

    val totalMaturityBase = if (card3Val > 0.0) card3Val else card1Val
    val rawPrincipalRatio = if (totalMaturityBase > 0.0) (card1Val / totalMaturityBase).toFloat() else 1f
    val targetRatio = if (card2Val <= 0.0) 1f else rawPrincipalRatio.coerceIn(0.02f, 0.98f)

    val animatedPrincipalWeight by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 250),
        label = "RatioAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
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
                text = screenTitle,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        text = card1Title,
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
                            text = "₹ $card1Formatted",
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(theme.textSecondary.copy(alpha = 0.35f))
                    ) {
                        if (card2Val <= 0.0) {
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            color = theme.numBtn.copy(alpha = 0.5f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = card2Title,
                                    fontSize = 11.sp,
                                    color = theme.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(sub1ScrollState)
                                ) {
                                    Text(
                                        text = "₹ $card2Formatted",
                                        fontSize = 16.sp,
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
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = card3Title,
                                    fontSize = 11.sp,
                                    color = theme.textSecondary,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(sub2ScrollState)
                                ) {
                                    Text(
                                        text = "₹ $card3Formatted",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

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
                        Text(
                            text = principalFieldLabel,
                            fontSize = 13.sp,
                            color = theme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = principalInput,
                            onValueChange = { if (it.length <= 15) principalInput = it },
                            textStyle = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedContainerColor = theme.numBtn,
                                unfocusedContainerColor = theme.numBtn,
                                focusedBorderColor = theme.accent,
                                unfocusedBorderColor = theme.funcBtn,
                                cursorColor = theme.accent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text(
                            text = if (tid in listOf("sip", "inv")) "Expected Return Rate (% p.a.)" else "Interest Rate (% p.a.)",
                            fontSize = 13.sp,
                            color = theme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(
                            value = rateInput,
                            onValueChange = { if (it.length <= 5) rateInput = it },
                            textStyle = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = theme.textPrimary,
                                unfocusedTextColor = theme.textPrimary,
                                focusedContainerColor = theme.numBtn,
                                unfocusedContainerColor = theme.numBtn,
                                focusedBorderColor = theme.accent,
                                unfocusedBorderColor = theme.funcBtn,
                                cursorColor = theme.accent
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column {
                        Text(
                            text = "Time Period / Tenure",
                            fontSize = 13.sp,
                            color = theme.textSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = tenureInput,
                                onValueChange = { if (it.length <= 4) tenureInput = it },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = theme.textPrimary,
                                    unfocusedTextColor = theme.textPrimary,
                                    focusedContainerColor = theme.numBtn,
                                    unfocusedContainerColor = theme.numBtn,
                                    focusedBorderColor = theme.accent,
                                    unfocusedBorderColor = theme.funcBtn,
                                    cursorColor = theme.accent
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.numBtn)
                                    .padding(4.dp)
                            ) {
                                listOf("Years", "Months").forEach { t ->
                                    val isSelected = tenureType == t
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) theme.accent else Color.Transparent,
                                        modifier = Modifier.clickable { tenureType = t }
                                    ) {
                                        Text(
                                            text = t,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (tid in listOf("compound_interest", "ci")) {
                        Column {
                            Text(
                                text = "Compounding Frequency",
                                fontSize = 13.sp,
                                color = theme.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(8.dp))

                            val row1 = listOf("Yearly", "Half-Yearly", "Quarterly")
                            val row2 = listOf("Every 4M", "Every 2M", "Monthly")

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(theme.numBtn)
                                    .padding(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    row1.forEach { freq ->
                                        val isSelected = compoundingFrequency == freq
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) theme.accent else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { compoundingFrequency = freq }
                                        ) {
                                            Text(
                                                text = freq,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                modifier = Modifier.padding(vertical = 11.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    row2.forEach { freq ->
                                        val isSelected = compoundingFrequency == freq
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) theme.accent else Color.Transparent,
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { compoundingFrequency = freq }
                                        ) {
                                            Text(
                                                text = freq,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                modifier = Modifier.padding(vertical = 11.dp),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}