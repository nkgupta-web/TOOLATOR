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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.*

data class RootResult(
    val label: String,
    val exact: String,
    val decimal: String,
    val nature: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquationSolverScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var eqType by remember { mutableStateOf("Quadratic") } // Linear, Quadratic, Cubic

    var aInput by remember { mutableStateOf("1") }
    var bInput by remember { mutableStateOf("0") }
    var cInput by remember { mutableStateOf("1") }
    var dInput by remember { mutableStateOf("0") }

    val a = aInput.toDoubleOrNull() ?: 1.0
    val b = bInput.toDoubleOrNull() ?: 0.0
    val c = cInput.toDoubleOrNull() ?: 0.0
    val d = dInput.toDoubleOrNull() ?: 0.0

    fun fmt(n: Double): String {
        val clean = if (abs(n) < 1e-9) 0.0 else n
        return DecimalFormat("#.####").format(clean)
    }

    // Dynamic equation preview
    val dynamicEqText = remember(eqType, a, b, c, d) {
        when (eqType) {
            "Linear" -> "${fmt(a)}x ${if (b >= 0) "+ ${fmt(b)}" else "- ${fmt(abs(b))}"} = 0"
            "Quadratic" -> "${fmt(a)}x² ${if (b >= 0) "+ ${fmt(b)}x" else "- ${fmt(abs(b))}x"} ${if (c >= 0) "+ ${fmt(c)}" else "- ${fmt(abs(c))}"} = 0"
            else -> "${fmt(a)}x³ ${if (b >= 0) "+ ${fmt(b)}x²" else "- ${fmt(abs(b))}x²"} ${if (c >= 0) "+ ${fmt(c)}x" else "- ${fmt(abs(c))}"} ${if (d >= 0) "+ ${fmt(d)}" else "- ${fmt(abs(d))}"} = 0"
        }
    }

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
                    text = "Equation Solver",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            }
            IconButton(
                onClick = {
                    aInput = "1"; bInput = "0"; cInput = "0"; dInput = "0"
                },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = theme.textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Mode Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(theme.surface)
                    .padding(3.dp)
            ) {
                listOf("Linear", "Quadratic", "Cubic").forEach { type ->
                    val isSelected = eqType == type
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) theme.accent else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { eqType = type }
                    ) {
                        Text(
                            text = type,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Input Coefficients Card
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("COEFFICIENTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                        Text(
                            text = dynamicEqText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = aInput,
                            onValueChange = { aInput = it },
                            label = { Text("a", fontSize = 11.sp, color = theme.textSecondary) },
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
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = bInput,
                            onValueChange = { bInput = it },
                            label = { Text("b", fontSize = 11.sp, color = theme.textSecondary) },
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
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        if (eqType == "Quadratic" || eqType == "Cubic") {
                            OutlinedTextField(
                                value = cInput,
                                onValueChange = { cInput = it },
                                label = { Text("c", fontSize = 11.sp, color = theme.textSecondary) },
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
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (eqType == "Cubic") {
                            OutlinedTextField(
                                value = dInput,
                                onValueChange = { dInput = it },
                                label = { Text("d", fontSize = 11.sp, color = theme.textSecondary) },
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
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Results Card
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "SOLUTIONS & NATURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(dynamicEqText))
                                Toast.makeText(context, "Equation Copied", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = theme.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    when (eqType) {
                        "Linear" -> {
                            if (a == 0.0) {
                                Text(
                                    text = if (b == 0.0) "Infinite solutions (0 = 0)" else "No solution (0 ≠ $b)",
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                val root = -b / a
                                val exact = simplifyFraction(-b, a)
                                UnifiedRootRow(
                                    result = RootResult(
                                        label = "x",
                                        exact = exact,
                                        decimal = fmt(root),
                                        nature = "Real Linear Root"
                                    )
                                )
                            }
                        }

                        "Quadratic" -> {
                            if (a == 0.0) {
                                Text(text = "Not a quadratic equation (a = 0). Switch to Linear.", color = Color(0xFFEF5350), fontSize = 12.sp)
                            } else {
                                val disc = (b * b) - (4 * a * c)

                                val natureText = when {
                                    disc > 0 -> "Real & Distinct"
                                    disc == 0.0 -> "Real & Equal"
                                    else -> "Complex Conjugate (Imaginary)"
                                }
                                val greenColor = Color(0xFF4CAF50)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Discriminant (D) = ${fmt(disc)}", fontSize = 12.sp, color = theme.textSecondary)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = greenColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = natureText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = greenColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.3f))

                                val roots = solveQuadraticClean(a, b, c, disc)
                                roots.forEach { rootRes ->
                                    UnifiedRootRow(result = rootRes)
                                }
                            }
                        }

                        "Cubic" -> {
                            if (a == 0.0) {
                                Text(text = "Not a cubic equation (a = 0). Switch to Quadratic.", color = Color(0xFFEF5350), fontSize = 12.sp)
                            } else {
                                val roots = solveCubicClean(a, b, c, d)
                                roots.forEach { rootRes ->
                                    UnifiedRootRow(result = rootRes)
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun UnifiedRootRow(result: RootResult) {
    val theme = LocalAppTheme.current.value
    val greenColor = Color(0xFF4CAF50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = result.label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)

        Column(horizontalAlignment = Alignment.End) {
            // Ans / Value: Always Theme Accent
            Text(
                text = result.exact,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = theme.accent
            )

            // Approximate Decimal Form (if different from exact)
            if (result.exact != result.decimal) {
                Text(
                    text = "≈ ${result.decimal}",
                    fontSize = 11.sp,
                    color = theme.textSecondary
                )
            }

            // Nature of the root: Always Green
            Text(
                text = result.nature,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = greenColor
            )
        }
    }
}

// Clean Mathematical Solvers

private fun solveQuadraticClean(a: Double, b: Double, c: Double, disc: Double): List<RootResult> {
    val df = DecimalFormat("#.####")
    fun cleanVal(v: Double) = if (abs(v) < 1e-9) 0.0 else v

    return when {
        disc > 0 -> {
            val sqrtD = sqrt(disc)
            val x1 = cleanVal((-b + sqrtD) / (2 * a))
            val x2 = cleanVal((-b - sqrtD) / (2 * a))

            val exactX1 = formatSurdClean(-b, disc, 2 * a, isPlus = true)
            val exactX2 = formatSurdClean(-b, disc, 2 * a, isPlus = false)

            listOf(
                RootResult("x₁", exactX1, df.format(x1), "Real Root"),
                RootResult("x₂", exactX2, df.format(x2), "Real Root")
            )
        }
        disc == 0.0 -> {
            val x = cleanVal(-b / (2 * a))
            val exactX = simplifyFraction(-b, 2 * a)
            listOf(
                RootResult("x₁ = x₂", exactX, df.format(x), "Repeated Real Root")
            )
        }
        else -> {
            val realPart = cleanVal(-b / (2 * a))
            val imagPart = cleanVal(sqrt(-disc) / (2 * a))

            val exactReal = if (realPart == 0.0) "" else simplifyFraction(-b, 2 * a)
            val exactImag = formatImaginaryCoeff(-disc, 2 * a)

            val exact1 = if (exactReal.isEmpty()) exactImag else "$exactReal + $exactImag"
            val exact2 = if (exactReal.isEmpty()) (if (exactImag.startsWith("-")) exactImag.substring(1) else "-$exactImag") else "$exactReal - $exactImag"

            val dec1 = if (realPart == 0.0) "${df.format(imagPart)}i" else "${df.format(realPart)} + ${df.format(abs(imagPart))}i"
            val dec2 = if (realPart == 0.0) "-${df.format(imagPart)}i" else "${df.format(realPart)} - ${df.format(abs(imagPart))}i"

            listOf(
                RootResult("x₁", exact1, dec1, "Complex Conjugate"),
                RootResult("x₂", exact2, dec2, "Complex Conjugate")
            )
        }
    }
}

private fun solveCubicClean(a: Double, b: Double, c: Double, d: Double): List<RootResult> {
    val df = DecimalFormat("#.####")
    fun cleanVal(v: Double) = if (abs(v) < 1e-9) 0.0 else v

    val p = (3 * a * c - b * b) / (3 * a * a)
    val q = (2 * b.pow(3) - 9 * a * b * c + 27 * a * a * d) / (27 * a.pow(3))
    val delta = (q / 2).pow(2) + (p / 3).pow(3)

    return when {
        delta > 0 -> {
            val u = cbrtCustom(-q / 2 + sqrt(delta))
            val v = cbrtCustom(-q / 2 - sqrt(delta))
            val r1 = cleanVal(u + v - (b / (3 * a)))
            val realPart = cleanVal(-(u + v) / 2 - (b / (3 * a)))
            val imagPart = cleanVal((u - v) * sqrt(3.0) / 2)

            val exactR1 = simplifyFraction(r1, 1.0)
            val exactReal = if (realPart == 0.0) "" else simplifyFraction(realPart, 1.0)
            val exactImag = formatCubicImagExact(imagPart)

            val ex2 = if (exactReal.isEmpty()) exactImag else "$exactReal + $exactImag"
            val ex3 = if (exactReal.isEmpty()) (if (exactImag.startsWith("-")) exactImag.substring(1) else "-$exactImag") else "$exactReal - $exactImag"

            val dec2 = if (realPart == 0.0) "${df.format(imagPart)}i" else "${df.format(realPart)} + ${df.format(abs(imagPart))}i"
            val dec3 = if (realPart == 0.0) "-${df.format(imagPart)}i" else "${df.format(realPart)} - ${df.format(abs(imagPart))}i"

            listOf(
                RootResult("x₁", exactR1, df.format(r1), "Real Root"),
                RootResult("x₂", ex2, dec2, "Complex Conjugate"),
                RootResult("x₃", ex3, dec3, "Complex Conjugate")
            )
        }
        delta == 0.0 -> {
            val r1 = cleanVal(2 * cbrtCustom(-q / 2) - (b / (3 * a)))
            val r2 = cleanVal(-cbrtCustom(-q / 2) - (b / (3 * a)))
            listOf(
                RootResult("x₁", simplifyFraction(r1, 1.0), df.format(r1), "Real Root"),
                RootResult("x₂", simplifyFraction(r2, 1.0), df.format(r2), "Repeated Real Root"),
                RootResult("x₃", simplifyFraction(r2, 1.0), df.format(r2), "Repeated Real Root")
            )
        }
        else -> {
            val r = sqrt(-(p / 3).pow(3))
            val phi = acos(-q / (2 * r))
            val r1 = cleanVal(2 * cbrtCustom(r) * cos(phi / 3) - (b / (3 * a)))
            val r2 = cleanVal(2 * cbrtCustom(r) * cos((phi + 2 * PI) / 3) - (b / (3 * a)))
            val r3 = cleanVal(2 * cbrtCustom(r) * cos((phi + 4 * PI) / 3) - (b / (3 * a)))
            listOf(
                RootResult("x₁", simplifyFraction(r1, 1.0), df.format(r1), "Real & Distinct"),
                RootResult("x₂", simplifyFraction(r2, 1.0), df.format(r2), "Real & Distinct"),
                RootResult("x₃", simplifyFraction(r3, 1.0), df.format(r3), "Real & Distinct")
            )
        }
    }
}

// Surd & Radical formatters
private fun formatSurdClean(minusB: Double, disc: Double, twoA: Double, isPlus: Boolean): String {
    val sqrtD = sqrt(disc)
    if (sqrtD % 1.0 == 0.0) {
        val num = if (isPlus) minusB + sqrtD else minusB - sqrtD
        return simplifyFraction(num, twoA)
    }
    val df = DecimalFormat("#.####")
    val sign = if (isPlus) "+" else "-"
    val bPart = if (minusB != 0.0) "${df.format(minusB)} $sign " else if (!isPlus) "-" else ""
    return "(${bPart}√${df.format(disc)}) / ${df.format(twoA)}"
}

private fun formatImaginaryCoeff(posDisc: Double, twoA: Double): String {
    val sqrtD = sqrt(posDisc)
    if (sqrtD % 1.0 == 0.0) {
        val frac = simplifyFraction(sqrtD, twoA)
        return when (frac) {
            "1" -> "i"
            "-1" -> "-i"
            else -> "${frac}i"
        }
    }
    val df = DecimalFormat("#.####")
    return "(√${df.format(posDisc)} / ${df.format(twoA)})i"
}

private fun formatCubicImagExact(imag: Double): String {
    val df = DecimalFormat("#.####")
    return if (abs(abs(imag) - (sqrt(3.0) / 2.0)) < 1e-4) {
        "(√3 / 2)i"
    } else if (abs(abs(imag) - sqrt(3.0)) < 1e-4) {
        "√3 i"
    } else {
        "${df.format(abs(imag))}i"
    }
}

private fun simplifyFraction(numerator: Double, denominator: Double): String {
    val df = DecimalFormat("#.####")
    if (denominator == 0.0) return "Undefined"
    if (abs(numerator) < 1e-9) return "0"

    if (numerator % 1.0 == 0.0 && denominator % 1.0 == 0.0) {
        val num = numerator.toLong()
        val den = denominator.toLong()
        val gcd = findGcd(abs(num), abs(den))
        val sNum = num / gcd
        val sDen = den / gcd
        return if (sDen == 1L) "$sNum" else if (sDen == -1L) "${-sNum}" else if (sDen < 0) "${-sNum}/${-sDen}" else "$sNum/$sDen"
    }
    return df.format(numerator / denominator)
}

private fun findGcd(a: Long, b: Long): Long = if (b == 0L) a else findGcd(b, a % b)

private fun cbrtCustom(x: Double): Double = if (x < 0) -(-x).pow(1.0 / 3.0) else x.pow(1.0 / 3.0)