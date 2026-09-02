package com.universal.calulator.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    CONVERTERS("converters", "Converters", "Units & number systems", Icons.Default.SwapHoriz),
    FINANCE("finance", "Finance", "Interest, Loan & Investment", Icons.Default.AccountBalance),
    TAX_SHOPPING("tax_shopping", "Tax & Shopping", "GST & discount calculations", Icons.Default.ShoppingBag),
    DATE_TIME("date_time", "Date & Time", "Age & date differences", Icons.Default.EventNote),
    HEALTH("health", "Health & Habit", "BMI & habit tracking", Icons.Default.FavoriteBorder),
    MATH_UTILITIES("math_utilities", "Math Utilities", "Maths Calculation", Icons.Default.Calculate);

    companion object {
        fun fromId(id: String?): ToolCategory? = entries.firstOrNull { it.id.equals(id, ignoreCase = true) }
    }
}

data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val category: ToolCategory,
    val route: String
)

object ToolRegistry {
    val allTools: List<ToolItem> = listOf(
        // ================= 1. CONVERTERS (13 Tools) =================
        ToolItem("curr", "Currency", "Live exchange rates", Icons.Default.CurrencyExchange, ToolCategory.CONVERTERS, "tool_currency"),
        ToolItem("len", "Length", "Distance & height units", Icons.Default.Straighten, ToolCategory.CONVERTERS, "tool_length"),
        ToolItem("area", "Area", "Land & property surface", Icons.Default.GridView, ToolCategory.CONVERTERS, "tool_area"),
        ToolItem("wt", "Weight / Mass", "Kg, pounds & ounces", Icons.Default.Scale, ToolCategory.CONVERTERS, "tool_weight"),
        ToolItem("temp", "Temperature", "Celsius, Fahrenheit & Kelvin", Icons.Default.Thermostat, ToolCategory.CONVERTERS, "tool_temperature"),
        ToolItem("vol", "Volume", "Liters, gallons & milliliters", Icons.Default.Science, ToolCategory.CONVERTERS, "tool_volume"),
        ToolItem("spd", "Speed", "km/h, mph & knots", Icons.Default.Speed, ToolCategory.CONVERTERS, "tool_speed"),
        ToolItem("time", "Time", "Seconds to centuries", Icons.Default.HourglassBottom, ToolCategory.CONVERTERS, "tool_time"),
        ToolItem("data", "Data Storage", "MB, GB, TB & bits", Icons.Default.Memory, ToolCategory.CONVERTERS, "tool_data"),
        ToolItem("press", "Pressure", "Bar, PSI & atmosphere", Icons.Default.TireRepair, ToolCategory.CONVERTERS, "tool_pressure"),
        ToolItem("eng", "Energy", "Joules, calories & kWh", Icons.Default.OfflineBolt, ToolCategory.CONVERTERS, "tool_energy"),
        ToolItem("pwr", "Power", "Watts & horsepower", Icons.Default.ElectricBolt, ToolCategory.CONVERTERS, "tool_power"),
        ToolItem("num_sys", "Number System", "Binary, Octal, Decimal & Hex", Icons.Default.Pin, ToolCategory.CONVERTERS, "tool_num_system"),

        // ================= 2. FINANCE (4 Tools) =================
        ToolItem("si", "Simple Interest", "Principal, rate & duration", Icons.Default.Percent, ToolCategory.FINANCE, "tool_si"),
        ToolItem("ci", "Compound Interest", "Compounding growth multiplier", Icons.AutoMirrored.Filled.TrendingUp, ToolCategory.FINANCE, "tool_ci"),
        ToolItem("emi", "Loan & EMI Planner", "Monthly loan installments & compare", Icons.Default.AccountBalanceWallet, ToolCategory.FINANCE, "tool_emi"),
        ToolItem("inv", "Investment / SIP Planner", "Estimated wealth & returns", Icons.AutoMirrored.Filled.ShowChart, ToolCategory.FINANCE, "tool_investment"),

        // ================= 3. TAX & SHOPPING (2 Tools) =================
        ToolItem("gst", "GST Calculator", "Calculate GST inclusive / exclusive", Icons.Default.ReceiptLong, ToolCategory.TAX_SHOPPING, "tool_gst"),
        ToolItem("disc", "Discount Calculator", "Final sale price & savings", Icons.Default.LocalOffer, ToolCategory.TAX_SHOPPING, "tool_discount"),

        // ================= 4. DATE & TIME (2 Tools) =================
        ToolItem("datediff", "Date Difference", "Calculate exact duration between dates", Icons.Default.DateRange, ToolCategory.DATE_TIME, "tool_date_diff"),
        ToolItem("age", "Age Calculator", "Exact age & countdown", Icons.Default.Cake, ToolCategory.DATE_TIME, "tool_age"),

        // ================= 5. HEALTH & HABIT (2 Tools) =================
        ToolItem("bmi", "BMI & Calorie Tracker", "Body Mass Index & daily goals", Icons.Default.MonitorWeight, ToolCategory.HEALTH, "tool_bmi"),
        ToolItem("habit_tracker", "Habit Calendar Tracker", "Track daily goals & completion rate", Icons.Default.CheckCircleOutline, ToolCategory.HEALTH, "tool_habit_tracker"),

        // ================= 6. MATH UTILITIES (6 Tools) =================
        ToolItem("eq_solve", "Equation Solver", "Linear, Quadratic & Cubic equations", Icons.Default.Functions, ToolCategory.MATH_UTILITIES, "tool_eq_solve"),
        ToolItem("num_base", "Base Calculator", "Multi-base arithmetic (Bin, Oct, Dec, Hex)", Icons.Default.Numbers, ToolCategory.MATH_UTILITIES, "tool_num_base"),
        ToolItem("matrix", "Matrix Calculator", "Add, Mul, Determinant & Inverse up to 4x4", Icons.Default.ViewCompact, ToolCategory.MATH_UTILITIES, "tool_matrix"),
        ToolItem("lcm_hcf", "LCM & HCF Calculator", "LCM, GCD & Prime Factorization", Icons.Default.AccountTree, ToolCategory.MATH_UTILITIES, "tool_lcm_hcf"),
        ToolItem("ratio", "Ratio & Proportion", "Simplify, Missing terms & Proportion", Icons.Default.Balance, ToolCategory.MATH_UTILITIES, "tool_ratio"),
        ToolItem("num_check", "Number Checker", "Prime, Armstrong, Palindrome & Properties", Icons.Default.FactCheck, ToolCategory.MATH_UTILITIES, "tool_num_check")
    )

    fun getToolsByCategory(category: ToolCategory): List<ToolItem> =
        allTools.filter { it.category == category }

    fun getToolCount(category: ToolCategory): Int =
        allTools.count { it.category == category }

    fun getToolById(id: String): ToolItem? =
        allTools.firstOrNull { it.id.equals(id, ignoreCase = true) }

    fun searchTools(query: String): List<ToolItem> {
        if (query.isBlank()) return emptyList()
        val q = query.trim().lowercase()
        return allTools.filter {
            it.name.lowercase().contains(q) ||
                    it.description.lowercase().contains(q) ||
                    it.category.title.lowercase().contains(q)
        }
    }
}