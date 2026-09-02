package com.universal.calulator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LcmHcfScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var inputNumbers by remember { mutableStateOf("12, 18, 24") }

    // Parse numbers from comma, space, semicolon, or newline
    val numbersList = remember(inputNumbers) {
        inputNumbers.split(",", " ", ";", "\n")
            .mapNotNull { it.trim().toLongOrNull() }
            .filter { it > 0 }
            .distinct()
            .take(20)
    }

    val hcf = remember(numbersList) {
        if (numbersList.isNotEmpty()) numbersList.reduce { acc, n -> findGcd(acc, n) } else 0L
    }

    val lcm = remember(numbersList) {
        if (numbersList.isNotEmpty()) {
            numbersList.fold(numbersList[0]) { acc, n ->
                val gcd = findGcd(acc, n)
                if (gcd != 0L) (acc / gcd) * n else 0L
            }
        } else 0L
    }

    val isCoprime = hcf == 1L && numbersList.size >= 2
    val isTwoNumbers = numbersList.size == 2

    fun fmt(n: Long): String = DecimalFormat("#,###").format(n)

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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "LCM & HCF Finder",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            }

            IconButton(
                onClick = { inputNumbers = "" },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Clear", tint = theme.textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Input Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ENTER POSITIVE NUMBERS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary
                        )
                        if (numbersList.isNotEmpty()) {
                            Text(
                                text = "${numbersList.size} numbers",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent
                            )
                        }
                    }

                    OutlinedTextField(
                        value = inputNumbers,
                        onValueChange = { inputNumbers = it },
                        placeholder = { Text("e.g. 12, 18, 24", color = theme.textSecondary.copy(alpha = 0.5f)) },
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Separate values using comma or space (Max 20)",
                        fontSize = 10.sp,
                        color = theme.textSecondary
                    )
                }
            }

            if (numbersList.size >= 2) {
                // Calculated Results Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CALCULATED RESULTS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isCoprime) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "✓ Co-prime",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("HCF: $hcf, LCM: $lcm for [${numbersList.joinToString(", ")}]"))
                                        Toast.makeText(context, "Results Copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = theme.textSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // LCM and HCF Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("HCF / GCD", fontSize = 12.sp, color = theme.textSecondary)
                                Text(fmt(hcf), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = theme.accent)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("LCM", fontSize = 12.sp, color = theme.textSecondary)
                                Text(fmt(lcm), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                            }
                        }

                        // Relationship Formula (2 Numbers case)
                        if (isTwoNumbers) {
                            val n1 = numbersList[0]
                            val n2 = numbersList[1]
                            val prodNum = n1 * n2
                            val prodHcfLcm = hcf * lcm

                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "RELATIONSHIP FORMULA (HCF × LCM = A × B)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textSecondary
                                )
                                Text(
                                    text = "${fmt(hcf)} × ${fmt(lcm)} = ${fmt(prodHcfLcm)} = ${fmt(n1)} × ${fmt(n2)} ✓",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textPrimary
                                )
                            }
                        }
                    }
                }

                // LCM Multiples Breakdown Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LCM AS MULTIPLE OF EACH INPUT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary
                        )

                        val multipleBreakdown = numbersList.joinToString(" = ") { num ->
                            "$num × ${lcm / num}"
                        }
                        Text(
                            text = "$lcm = $multipleBreakdown",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                    }
                }

                // Multi-Number Complete Euclidean Algorithm Steps Card
                val fullEuclideanStages = remember(numbersList) {
                    generateFullSequentialEuclideanSteps(numbersList)
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "EUCLIDEAN ALGORITHM STEPS (FULL SEQUENCE)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary
                        )

                        fullEuclideanStages.forEachIndexed { idx, stageLines ->
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                stageLines.forEach { line ->
                                    val isHeading = line.startsWith("▶")
                                    val isSummary = line.startsWith("→")
                                    Text(
                                        text = line,
                                        fontSize = 11.sp,
                                        fontWeight = if (isHeading || isSummary) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isHeading) theme.accent else if (isSummary) Color(0xFF4CAF50) else theme.textPrimary.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            if (idx < fullEuclideanStages.size - 1) {
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }

                // Prime Factorization Breakdown Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "PRIME FACTORIZATION (WITH POWERS)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary
                        )

                        numbersList.forEach { num ->
                            val factorsFormatted = formatPrimeFactors(num)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "$num", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                Text(text = factorsFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Enter at least 2 positive numbers to compute LCM & HCF",
                            fontSize = 12.sp,
                            color = theme.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

// Chained Euclidean Algorithm for N numbers
private fun generateFullSequentialEuclideanSteps(numbers: List<Long>): List<List<String>> {
    if (numbers.size < 2) return emptyList()

    val stages = mutableListOf<List<String>>()
    var currentGcd = numbers[0]

    for (i in 1 until numbers.size) {
        val nextNum = numbers[i]
        val stageSteps = mutableListOf<String>()

        if (numbers.size > 2) {
            stageSteps.add("▶ Step $i: GCD($currentGcd, $nextNum)")
        }

        var x = maxOf(currentGcd, nextNum)
        var y = minOf(currentGcd, nextNum)

        while (y != 0L) {
            val q = x / y
            val r = x % y
            stageSteps.add("$x = $y × $q + $r")
            x = y
            y = r
        }

        currentGcd = x
        stageSteps.add("→ Current HCF = $currentGcd")
        stages.add(stageSteps)
    }

    return stages
}

private fun findGcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else findGcd(b, a % b)

private fun formatPrimeFactors(number: Long): String {
    if (number <= 1L) return number.toString()
    val factorsMap = mutableMapOf<Long, Int>()
    var n = number
    var d = 2L

    while (d * d <= n) {
        while (n % d == 0L) {
            factorsMap[d] = factorsMap.getOrDefault(d, 0) + 1
            n /= d
        }
        d++
    }
    if (n > 1L) {
        factorsMap[n] = factorsMap.getOrDefault(n, 0) + 1
    }

    val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹'
    )

    return factorsMap.entries.joinToString(" × ") { (prime, count) ->
        if (count == 1) "$prime"
        else {
            val superCount = count.toString().map { superscriptMap[it] ?: it }.joinToString("")
            "$prime$superCount"
        }
    }
}