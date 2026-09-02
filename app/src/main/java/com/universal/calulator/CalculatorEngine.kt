package com.universal.calulator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

enum class AngleMode { DEG, RAD }

class CalculatorEngine {
    private val mathContext = MathContext(34, RoundingMode.HALF_UP)
    var angleMode: AngleMode = AngleMode.DEG
    var memoryValue: BigDecimal = BigDecimal.ZERO
    var lastAnswer: String = "0"

    private val EPSILON = 1e-13

    fun memoryClear() { memoryValue = BigDecimal.ZERO }

    fun memoryAdd(value: String) {
        try { memoryValue = memoryValue.add(BigDecimal(value), mathContext) } catch (_: Exception) {}
    }

    fun memorySubtract(value: String) {
        try { memoryValue = memoryValue.subtract(BigDecimal(value), mathContext) } catch (_: Exception) {}
    }

    private fun preprocessPercentage(expr: String): String {
        var res = expr
        val addSubPercentRegex = Regex("""(\d+(?:\.\d+)?|\))\s*([\+\−\-])\s*(\d+(?:\.\d+)?)\%""")
        while (addSubPercentRegex.containsMatchIn(res)) {
            res = addSubPercentRegex.replace(res) { m ->
                val base = m.groupValues[1]
                val op = m.groupValues[2]
                val percent = m.groupValues[3]
                "$base $op ($base * $percent / 100)"
            }
        }
        return res
    }

    private fun autoCloseBrackets(expr: String): String {
        val openCount = expr.count { it == '(' }
        val closeCount = expr.count { it == ')' }
        return if (openCount > closeCount) {
            expr + ")".repeat(openCount - closeCount)
        } else {
            expr
        }
    }

    fun evaluate(expression: String): String {
        if (expression.isBlank()) return ""
        return try {
            val balancedExpr = autoCloseBrackets(expression)
            val processedPercents = preprocessPercentage(balancedExpr)

            var sanitized = processedPercents
                .replace("ANS", "($lastAnswer)")
                .replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace("MOD", "#")
                .replace("π", "(${PI})")

            // Safe Euler's 'e' replacement
            sanitized = sanitized.replace(Regex("""(?<![a-zA-Z])e(?![a-zA-Z])"""), "(${E})")
            sanitized = sanitized.replace(Regex("""(?<![a-zA-Z])e\^"""), "(${E})^")

            // Implicit multiplication
            sanitized = sanitized.replace(Regex("""(\d|\))\s*\("""), "$1*(")
            sanitized = sanitized.replace(Regex("""(\d)\s*([a-zA-Z])"""), "$1*$2")

            val result = eval(sanitized)
            if (result.isNaN() || result.isInfinite()) {
                "Error"
            } else {
                BigDecimal.valueOf(result)
                    .setScale(10, RoundingMode.HALF_UP)
                    .stripTrailingZeros()
                    .toPlainString()
            }
        } catch (e: Exception) {
            "Error"
        }
    }

    private fun factorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 170.0) throw IllegalArgumentException("Error")
        var res = 1.0
        for (i in 1..n.toLong()) res *= i
        return res
    }

    private fun doubleFactorial(n: Double): Double {
        if (n < 0 || n != floor(n) || n > 300.0) throw IllegalArgumentException("Error")
        var res = 1.0
        var i = n.toLong()
        while (i > 0) {
            res *= i
            i -= 2
        }
        return res
    }

    // Singularity-Safe Exact Trig Handlers
    private fun safeSin(rad: Double): Double {
        val s = sin(rad)
        return if (abs(s) < EPSILON) 0.0 else s
    }

    private fun safeCos(rad: Double): Double {
        val c = cos(rad)
        return if (abs(c) < EPSILON) 0.0 else c
    }

    private fun safeTan(rad: Double): Double {
        val c = safeCos(rad)
        if (abs(c) < EPSILON) throw ArithmeticException("Undefined")
        val s = safeSin(rad)
        return s / c
    }

    private fun safeCot(rad: Double): Double {
        val s = safeSin(rad)
        if (abs(s) < EPSILON) throw ArithmeticException("Undefined")
        val c = safeCos(rad)
        return c / s
    }

    private fun safeSec(rad: Double): Double {
        val c = safeCos(rad)
        if (abs(c) < EPSILON) throw ArithmeticException("Undefined")
        return 1.0 / c
    }

    private fun safeCsc(rad: Double): Double {
        val s = safeSin(rad)
        if (abs(s) < EPSILON) throw ArithmeticException("Undefined")
        return 1.0 / s
    }

    private fun toRad(degVal: Double): Double = Math.toRadians(degVal)
    private fun toDeg(radVal: Double): Double = Math.toDegrees(radVal)

    private fun eval(str: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < str.length) str[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.code)) x += parseTerm()
                    else if (eat('-'.code)) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.code)) {
                        x *= parseFactor()
                    } else if (eat('/'.code)) {
                        val divisor = parseFactor()
                        if (abs(divisor) < EPSILON) throw ArithmeticException("Division by Zero")
                        x /= divisor
                    } else if (eat('#'.code)) {
                        val divisor = parseFactor()
                        if (abs(divisor) < EPSILON) throw ArithmeticException("Modulo by Zero")
                        x %= divisor
                    } else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos

                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = str.substring(startPos, pos).toDouble()
                } else if (ch in 'a'.code..'z'.code) {
                    while (ch in 'a'.code..'z'.code) nextChar()
                    val func = str.substring(startPos, pos)

                    if (eat('('.code)) {
                        x = parseExpression()
                        eat(')'.code)
                    } else {
                        x = parseFactor()
                    }

                    val isDeg = angleMode == AngleMode.DEG
                    val rad = if (isDeg) toRad(x) else x

                    x = when (func) {
                        // Standard Trig with Singularity Guard
                        "sin" -> safeSin(rad)
                        "cos" -> safeCos(rad)
                        "tan" -> safeTan(rad)

                        // Reciprocal Trig with Denominator Guard
                        "csc" -> safeCsc(rad)
                        "sec" -> safeSec(rad)
                        "cot" -> safeCot(rad)

                        // Inverse Standard Trig with Domain Validation
                        "asin" -> {
                            if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain Error")
                            val res = asin(x)
                            if (isDeg) toDeg(res) else res
                        }
                        "acos" -> {
                            if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain Error")
                            val res = acos(x)
                            if (isDeg) toDeg(res) else res
                        }
                        "atan" -> {
                            val res = atan(x)
                            if (isDeg) toDeg(res) else res
                        }

                        // Inverse Reciprocal Trig with Domain Validation
                        "acsc" -> {
                            if (abs(x) < 1.0) throw ArithmeticException("Domain Error: |x| >= 1 required")
                            val res = asin(1.0 / x)
                            if (isDeg) toDeg(res) else res
                        }
                        "asec" -> {
                            if (abs(x) < 1.0) throw ArithmeticException("Domain Error: |x| >= 1 required")
                            val res = acos(1.0 / x)
                            if (isDeg) toDeg(res) else res
                        }
                        "acot" -> {
                            if (abs(x) < EPSILON) throw ArithmeticException("Domain Error")
                            val res = atan(1.0 / x)
                            if (isDeg) toDeg(res) else res
                        }

                        // Logarithmic & Roots with Domain Validation
                        "log" -> {
                            if (x <= 0.0) throw ArithmeticException("Domain Error: x > 0 required")
                            log10(x)
                        }
                        "ln" -> {
                            if (x <= 0.0) throw ArithmeticException("Domain Error: x > 0 required")
                            ln(x)
                        }
                        "sqrt" -> {
                            if (x < 0.0) throw ArithmeticException("Domain Error: x >= 0 required")
                            sqrt(x)
                        }
                        else -> throw RuntimeException("Unknown function: $func")
                    }
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }

                while (true) {
                    if (eat('^'.code)) {
                        val exp = parseFactor()
                        val res = x.pow(exp)
                        if (res.isNaN() || res.isInfinite()) throw ArithmeticException("Power Domain Error")
                        x = res
                    } else if (eat('%'.code)) {
                        x /= 100.0
                    } else if (eat('!'.code)) {
                        if (eat('!'.code)) {
                            x = doubleFactorial(x)
                        } else {
                            x = factorial(x)
                        }
                    } else {
                        break
                    }
                }

                return x
            }
        }.parse()
    }
}