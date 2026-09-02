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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberCheckerScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    var inputStr by remember { mutableStateOf("153") }
    var selectedProperty by remember { mutableStateOf("Prime") }

    val n = inputStr.toLongOrNull()

    // Color indicators
    val successGreen = Color(0xFF4CAF50)
    val failureLightRed = Color(0xFFEF5350)

    // 3 Structured Rows (4 chips each)
    val row1Chips = listOf("Prime", "Square", "Cube", "Fibonacci")
    val row2Chips = listOf("Palindrome", "Armstrong", "Harshad", "Automorphic")
    val row3Chips = listOf("Neon", "Happy", "Spy", "Perfect")

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
                text = "Number Checker",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CARD 1 — INPUT CARD
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "ENTER INTEGER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = inputStr,
                        onValueChange = {
                            if (it.length <= 15 && it.all { char -> char.isDigit() || char == '-' }) inputStr = it
                        },
                        placeholder = { Text("153", color = theme.textSecondary.copy(alpha = 0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
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
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (n != null) {
                val isPrime = checkPrime(n)
                val isEven = n % 2L == 0L
                val signStr = if (n > 0) "Positive (+)" else if (n < 0) "Negative (-)" else "Zero"

                // CARD 2 — COMPACT QUICK OVERVIEW STATUS (All 3 default white/textPrimary)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Prime", fontSize = 11.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isPrime) "Prime" else "Composite",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(30.dp), color = theme.funcBtn.copy(alpha = 0.5f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Even / Odd", fontSize = 11.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isEven) "Odd" else "Odd",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }

                        VerticalDivider(modifier = Modifier.height(30.dp), color = theme.funcBtn.copy(alpha = 0.5f))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sign", fontSize = 11.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = signStr,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                        }
                    }
                }

                // CARD 3 — 3-ROW PROPERTY SELECTOR CHIPS
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(row1Chips, row2Chips, row3Chips).forEach { rowList ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowList.forEach { prop ->
                                val isSelected = selectedProperty == prop
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) theme.accent else theme.surface,
                                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedProperty = prop }
                                ) {
                                    Text(
                                        text = prop,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                        modifier = Modifier.padding(vertical = 9.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD 4 — DYNAMIC DETAILED INSPECTION (Green for True, Light Red for False)
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$selectedProperty INSPECTION".uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent,
                            letterSpacing = 0.6.sp
                        )

                        when (selectedProperty) {
                            "Prime" -> {
                                val prevPrime = if (n > 2) findPrevPrime(n) else null
                                val nextPrime = findNextPrime(n)

                                Text(
                                    text = if (isPrime) "✓ Yes, $n is a Prime Number" else if (n <= 1) "✗ $n is neither Prime nor Composite" else "✗ $n is not a Prime (Composite)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPrime) successGreen else failureLightRed
                                )
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Previous Prime: ${prevPrime ?: "None"}", fontSize = 12.sp, color = theme.textSecondary)
                                    Text("Next Prime: $nextPrime", fontSize = 12.sp, color = theme.textSecondary)
                                }
                            }

                            "Square" -> {
                                val isSquare = checkPerfectSquare(n)
                                val sqrtVal = sqrt(n.toDouble())
                                val prevSq = if (n > 0) ((floor(sqrtVal) - (if (isSquare) 1 else 0)).toLong().coerceAtLeast(0)).let { it * it } else 0
                                val nextSq = ((floor(sqrtVal) + 1).toLong()).let { it * it }

                                Text(
                                    text = if (isSquare) "✓ Yes, $n = ${(sqrtVal).toInt()}²" else "✗ $n is not a Perfect Square",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSquare) successGreen else failureLightRed
                                )
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Previous Square: $prevSq", fontSize = 12.sp, color = theme.textSecondary)
                                    Text("Next Square: $nextSq", fontSize = 12.sp, color = theme.textSecondary)
                                }
                            }

                            "Cube" -> {
                                val isCube = checkPerfectCube(n)
                                val cbrtVal = Math.cbrt(n.toDouble())
                                Text(
                                    text = if (isCube) "✓ Yes, $n = ${(cbrtVal).toInt()}³" else "✗ $n is not a Perfect Cube",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCube) successGreen else failureLightRed
                                )
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                                Text("Cube Root: ~${DecimalFormat("#.####").format(cbrtVal)}", fontSize = 12.sp, color = theme.textSecondary)
                            }

                            "Fibonacci" -> {
                                val isFib = checkFibonacci(n)
                                Text(
                                    text = if (isFib) "✓ Yes, $n belongs to Fibonacci sequence" else "✗ $n is not a Fibonacci Number",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFib) successGreen else failureLightRed
                                )
                            }

                            "Palindrome" -> {
                                val isPal = checkPalindrome(n)
                                val rev = abs(n).toString().reversed()
                                Text(
                                    text = if (isPal) "✓ Yes, $n reads the same backwards" else "✗ $n is not a Palindrome",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPal) successGreen else failureLightRed
                                )
                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                                Text("Reversed Number: $rev", fontSize = 12.sp, color = theme.textSecondary)
                            }

                            "Armstrong" -> {
                                val isArm = checkArmstrong(n)
                                Text(
                                    text = if (isArm) "✓ Yes, Armstrong Number (sum of digitsⁿ = $n)" else "✗ $n is not an Armstrong Number",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isArm) successGreen else failureLightRed
                                )
                            }

                            "Harshad" -> {
                                val isHar = checkHarshad(n)
                                val sumDigits = n.toString().sumOf { it.digitToInt() }
                                Text(
                                    text = if (isHar) "✓ Yes, divisible by sum of digits ($n ÷ $sumDigits)" else "✗ Not a Harshad Number",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHar) successGreen else failureLightRed
                                )
                            }

                            "Automorphic" -> {
                                val isAuto = checkAutomorphic(n)
                                Text(
                                    text = if (isAuto) "✓ Yes, $n² = ${n * n} (ends with $n)" else "✗ Not Automorphic ($n² = ${n * n})",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAuto) successGreen else failureLightRed
                                )
                            }

                            "Neon" -> {
                                val isNeon = checkNeon(n)
                                val sq = n * n
                                val sumSqDigits = sq.toString().sumOf { it.digitToInt() }
                                Text(
                                    text = if (isNeon) "✓ Yes, $n² = $sq and digit sum ($sumSqDigits) = $n" else "✗ Not a Neon Number ($n² = $sq, digit sum = $sumSqDigits)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNeon) successGreen else failureLightRed
                                )
                            }

                            "Happy" -> {
                                val isHappy = checkHappy(n)
                                Text(
                                    text = if (isHappy) "✓ Yes, $n is a Happy Number (sum of squares of digits reaches 1)" else "✗ Not a Happy Number (enters a cyclic loop)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isHappy) successGreen else failureLightRed
                                )
                            }

                            "Spy" -> {
                                val isSpy = checkSpy(n)
                                val sumD = abs(n).toString().sumOf { it.digitToInt() }
                                val prodD = abs(n).toString().map { it.digitToInt().toLong() }.reduce { acc, i -> acc * i }
                                Text(
                                    text = if (isSpy) "✓ Yes, Sum of digits ($sumD) = Product of digits ($prodD)" else "✗ Not a Spy Number (Sum = $sumD, Product = $prodD)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSpy) successGreen else failureLightRed
                                )
                            }

                            "Perfect" -> {
                                val isPerf = checkPerfectNumber(n)
                                Text(
                                    text = if (isPerf) "✓ Yes, $n is a Perfect Number (sum of proper divisors = $n)" else "✗ $n is not a Perfect Number",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPerf) successGreen else failureLightRed
                                )
                            }
                        }
                    }
                }

                // CARD 5 — NUMBER METRICS
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
                            text = "NUMBER METRICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary,
                            letterSpacing = 0.6.sp
                        )

                        val digitsStr = abs(n).toString()
                        MetricProminentRow("Total Digits", digitsStr.length.toString())
                        MetricProminentRow("Sum of Digits", digitsStr.sumOf { it.digitToInt() }.toString())
                        MetricProminentRow("Digital Root", calculateDigitalRoot(n).toString())
                        MetricProminentRow("Total Factors", "${countFactors(n)} factors")
                        MetricProminentRow("Reversed Number", digitsStr.reversed())
                        MetricProminentRow("Binary Representation", if (n >= 0) n.toString(2) else "-${abs(n).toString(2)}")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun MetricProminentRow(label: String, value: String) {
    val theme = LocalAppTheme.current.value
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = theme.textSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
    }
}

// Math Algorithms
private fun calculateDigitalRoot(n: Long): Int {
    var num = abs(n)
    while (num >= 10) {
        num = num.toString().sumOf { it.digitToInt() }.toLong()
    }
    return num.toInt()
}

private fun countFactors(n: Long): Int {
    val num = abs(n)
    if (num == 0L) return 0
    var count = 0
    var i = 1L
    while (i * i <= num) {
        if (num % i == 0L) {
            count += if (i * i == num) 1 else 2
        }
        i++
    }
    return count
}

private fun checkPrime(n: Long): Boolean {
    if (n <= 1) return false
    if (n <= 3) return true
    if (n % 2 == 0L || n % 3 == 0L) return false
    var i = 5L
    while (i * i <= n) {
        if (n % i == 0L || n % (i + 2) == 0L) return false
        i += 6
    }
    return true
}

private fun findPrevPrime(n: Long): Long? {
    var candidate = n - 1
    while (candidate >= 2) {
        if (checkPrime(candidate)) return candidate
        candidate--
    }
    return null
}

private fun findNextPrime(n: Long): Long {
    var candidate = if (n < 2) 2 else n + 1
    while (true) {
        if (checkPrime(candidate)) return candidate
        candidate++
    }
}

private fun checkPerfectSquare(n: Long): Boolean {
    if (n < 0) return false
    val root = sqrt(n.toDouble()).toLong()
    return root * root == n
}

private fun checkPerfectCube(n: Long): Boolean {
    val root = Math.cbrt(n.toDouble()).toLong()
    return root * root * root == n
}

private fun checkPalindrome(n: Long): Boolean {
    val s = abs(n).toString()
    return s == s.reversed()
}

private fun checkArmstrong(n: Long): Boolean {
    if (n < 0) return false
    val s = n.toString()
    val len = s.length
    val sum = s.sumOf { (it.digitToInt().toDouble().pow(len)).toLong() }
    return sum == n
}

private fun checkHarshad(n: Long): Boolean {
    if (n <= 0) return false
    val sumOfDigits = n.toString().sumOf { it.digitToInt() }
    return n % sumOfDigits == 0L
}

private fun checkFibonacci(n: Long): Boolean {
    if (n < 0) return false
    return checkPerfectSquare(5 * n * n + 4) || checkPerfectSquare(5 * n * n - 4)
}

private fun checkAutomorphic(n: Long): Boolean {
    if (n < 0) return false
    val square = n * n
    return square.toString().endsWith(n.toString())
}

private fun checkNeon(n: Long): Boolean {
    if (n < 0) return false
    val square = n * n
    val sumOfDigits = square.toString().sumOf { it.digitToInt() }
    return sumOfDigits.toLong() == n
}

private fun checkHappy(n: Long): Boolean {
    if (n <= 0) return false
    var current = n
    val seen = mutableSetOf<Long>()
    while (current != 1L && !seen.contains(current)) {
        seen.add(current)
        current = current.toString().sumOf { (it.digitToInt() * it.digitToInt()).toLong() }
    }
    return current == 1L
}

private fun checkSpy(n: Long): Boolean {
    val s = abs(n).toString()
    val sum = s.sumOf { it.digitToInt() }
    val product = s.map { it.digitToInt().toLong() }.reduce { acc, i -> acc * i }
    return sum.toLong() == product
}

private fun checkPerfectNumber(n: Long): Boolean {
    if (n <= 1) return false
    var sum = 1L
    var i = 2L
    while (i * i <= n) {
        if (n % i == 0L) {
            sum += i
            if (i * i != n) sum += n / i
        }
        i++
    }
    return sum == n
}