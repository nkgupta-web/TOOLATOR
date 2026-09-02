package com.universal.calulator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

// -------------------------------------------------------------
// FRACTIONAL & ARBITRARY BASE CALCULATION ENGINE
// -------------------------------------------------------------
object BaseMathEngine {
    private const val DIGITS = "0123456789ABCDEF"

    fun getBaseName(base: Int): String = when (base) {
        2 -> "BIN"
        8 -> "OCT"
        10 -> "DEC"
        16 -> "HEX"
        else -> "Base $base"
    }

    fun isValidChar(char: Char, base: Int): Boolean {
        if (char == '.' || char == '-' || char == ' ') return true
        val upper = char.uppercaseChar()
        val index = DIGITS.indexOf(upper)
        return index in 0 until base
    }

    // Binary 4-bit block chunk formatter: "10101111" -> "1010 1111"
    fun formatBinarySpaced(binStr: String): String {
        if (binStr.contains("NaN") || binStr.contains("Infinity") || binStr == "Invalid Input") return binStr
        val isNegative = binStr.startsWith("-")
        val clean = binStr.removePrefix("-").replace(" ", "")
        if (clean.isEmpty()) return "0"

        val parts = clean.split(".")
        val intPart = parts[0]
        val fracPart = if (parts.size > 1) parts[1] else null

        // Group integer part from right to left
        val intGrouped = intPart.reversed().chunked(4).joinToString(" ").reversed()
        // Group fractional part from left to right
        val fracGrouped = fracPart?.chunked(4)?.joinToString(" ")

        val full = if (fracGrouped != null) "$intGrouped.$fracGrouped" else intGrouped
        return if (isNegative) "-$full" else full
    }

    fun toDecimal(value: String, base: Int): Double? {
        val trimmed = value.trim().replace(" ", "")
        if (trimmed.isEmpty() || trimmed == "-" || trimmed == ".") return null

        val isNegative = trimmed.startsWith("-")
        val cleanStr = trimmed.removePrefix("-")

        val parts = cleanStr.split(".")
        if (parts.size > 2) return null // Multiple decimal dots

        val intStr = parts[0].uppercase()
        var intPart = 0.0
        for (ch in intStr) {
            val digit = DIGITS.indexOf(ch)
            if (digit !in 0 until base) return null
            intPart = intPart * base + digit
        }

        var fracPart = 0.0
        if (parts.size > 1) {
            val fracStr = parts[1].uppercase()
            var weight = 1.0 / base
            for (ch in fracStr) {
                val digit = DIGITS.indexOf(ch)
                if (digit !in 0 until base) return null
                fracPart += digit * weight
                weight /= base
            }
        }

        val total = intPart + fracPart
        return if (isNegative) -total else total
    }

    fun fromDecimal(value: Double, base: Int, precision: Int = 6): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return "Infinity"

        val isNegative = value < 0
        val absVal = abs(value)

        var intPart = absVal.toLong()
        var fracPart = absVal - intPart

        var intStr = ""
        if (intPart == 0L) {
            intStr = "0"
        } else {
            while (intPart > 0) {
                val rem = (intPart % base).toInt()
                intStr = DIGITS[rem] + intStr
                intPart /= base
            }
        }

        var fracStr = ""
        var count = 0
        while (fracPart > 0.000001 && count < precision) {
            fracPart *= base
            val d = fracPart.toInt()
            fracStr += DIGITS[d]
            fracPart -= d
            count++
        }

        val result = if (fracStr.isNotEmpty()) "$intStr.$fracStr" else intStr
        val formatted = if (isNegative) "-$result" else result

        return if (base == 2) formatBinarySpaced(formatted) else formatted
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseCalculatorScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val context = LocalContext.current

    var num1Input by remember { mutableStateOf("1010.1") }
    var base1 by remember { mutableIntStateOf(2) }

    var operator by remember { mutableStateOf("+") }

    var num2Input by remember { mutableStateOf("15.5") }
    var base2 by remember { mutableIntStateOf(10) }

    var resultBase by remember { mutableIntStateOf(16) }

    var toastRef by remember { mutableStateOf<Toast?>(null) }

    fun showInvalidToast(char: Char, base: Int) {
        toastRef?.cancel()
        val baseName = BaseMathEngine.getBaseName(base)
        val msg = "'$char' is invalid for $baseName"
        toastRef = Toast.makeText(context, msg, Toast.LENGTH_SHORT).apply { show() }
    }

    fun filterInput(newText: String, oldText: String, base: Int): String {
        val uppercaseText = newText.uppercase()

        // Allow backspace/clearing
        if (uppercaseText.length < oldText.length) return uppercaseText

        // Check each newly typed character
        for (ch in uppercaseText) {
            if (!BaseMathEngine.isValidChar(ch, base)) {
                showInvalidToast(ch, base)
                return oldText
            }
        }

        // Allow max one decimal point
        if (uppercaseText.count { it == '.' } > 1) {
            return oldText
        }

        // Allow max one leading minus
        if (uppercaseText.lastIndexOf('-') > 0) {
            return oldText
        }

        return uppercaseText
    }

    val val1 = BaseMathEngine.toDecimal(num1Input, base1)
    val val2 = BaseMathEngine.toDecimal(num2Input, base2)

    val rawResult: Double? = if (val1 != null && val2 != null) {
        when (operator) {
            "+" -> val1 + val2
            "-" -> val1 - val2
            "×" -> val1 * val2
            "÷" -> if (val2 != 0.0) val1 / val2 else null
            else -> null
        }
    } else null

    val mainResultText = remember(rawResult, resultBase) {
        if (rawResult != null) BaseMathEngine.fromDecimal(rawResult, resultBase) else "Invalid Input"
    }

    val resultScrollState = rememberScrollState()
    LaunchedEffect(mainResultText) {
        resultScrollState.scrollTo(resultScrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
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
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Base Calculator",
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
            // Input 1 Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("NUMBER 1", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    BaseSelectorRow(
                        selectedBase = base1,
                        onSelect = { newBase ->
                            base1 = newBase
                            val validChars = num1Input.filter { BaseMathEngine.isValidChar(it, newBase) }
                            num1Input = validChars
                        }
                    )
                    OutlinedTextField(
                        value = num1Input,
                        onValueChange = { newText ->
                            num1Input = filterInput(newText, num1Input, base1)
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        placeholder = { Text("0.0", color = theme.textSecondary.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedContainerColor = theme.numBtn,
                            unfocusedContainerColor = theme.numBtn,
                            focusedBorderColor = theme.accent,
                            unfocusedBorderColor = theme.funcBtn,
                            cursorColor = theme.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Operator Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                listOf("+", "-", "×", "÷").forEach { op ->
                    val isSelected = operator == op
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) theme.accent else theme.surface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)) else null,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clickable { operator = op }
                    ) {
                        Text(
                            text = op,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Input 2 Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("NUMBER 2", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    BaseSelectorRow(
                        selectedBase = base2,
                        onSelect = { newBase ->
                            base2 = newBase
                            val validChars = num2Input.filter { BaseMathEngine.isValidChar(it, newBase) }
                            num2Input = validChars
                        }
                    )
                    OutlinedTextField(
                        value = num2Input,
                        onValueChange = { newText ->
                            num2Input = filterInput(newText, num2Input, base2)
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        placeholder = { Text("0.0", color = theme.textSecondary.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = theme.textPrimary,
                            unfocusedTextColor = theme.textPrimary,
                            focusedContainerColor = theme.numBtn,
                            unfocusedContainerColor = theme.numBtn,
                            focusedBorderColor = theme.accent,
                            unfocusedBorderColor = theme.funcBtn,
                            cursorColor = theme.accent
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Result Card with Horizontal Auto-Scroll & Dynamic Text Sizing
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("RESULT BASE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                    BaseSelectorRow(selectedBase = resultBase, onSelect = { resultBase = it })

                    Spacer(Modifier.height(4.dp))

                    val dynamicResultFontSize = when {
                        mainResultText.length <= 12 -> 24.sp
                        mainResultText.length <= 20 -> 20.sp
                        mainResultText.length <= 30 -> 16.sp
                        else -> 14.sp
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(resultScrollState)
                    ) {
                        Text(
                            text = mainResultText,
                            fontSize = dynamicResultFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.accent,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    if (rawResult != null) {
                        HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                        MultiBaseResultRow(label = "BIN", value = BaseMathEngine.fromDecimal(rawResult, 2), color = theme.textSecondary)
                        MultiBaseResultRow(label = "OCT", value = BaseMathEngine.fromDecimal(rawResult, 8), color = theme.textSecondary)
                        MultiBaseResultRow(label = "DEC", value = BaseMathEngine.fromDecimal(rawResult, 10), color = theme.textSecondary)
                        MultiBaseResultRow(label = "HEX", value = BaseMathEngine.fromDecimal(rawResult, 16), color = theme.textSecondary)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun MultiBaseResultRow(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    val scrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState)
        ) {
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun BaseSelectorRow(selectedBase: Int, onSelect: (Int) -> Unit) {
    val theme = LocalAppTheme.current.value
    val bases = listOf(Pair("BIN", 2), Pair("OCT", 8), Pair("DEC", 10), Pair("HEX", 16))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(theme.numBtn)
            .padding(2.dp)
    ) {
        bases.forEach { (name, b) ->
            val isSelected = selectedBase == b
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSelected) theme.accent else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelect(b) }
            ) {
                Text(
                    text = name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                    modifier = Modifier.padding(vertical = 6.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}