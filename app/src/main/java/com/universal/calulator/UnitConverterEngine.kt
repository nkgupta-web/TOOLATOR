package com.universal.calulator

import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

data class UnitItem(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseFactor: Double = 1.0
)

object UnitConverterEngine {

    fun getUnitsForTool(toolId: String): List<UnitItem> {
        return when (toolId.lowercase().trim()) {
            "num_sys", "number_system", "tool_num_system" -> listOf(
                UnitItem("dec", "Decimal", "Base 10", 1.0),
                UnitItem("bin", "Binary", "Base 2", 1.0),
                UnitItem("hex", "Hexadecimal", "Base 16", 1.0),
                UnitItem("oct", "Octal", "Base 8", 1.0)
            )
            "data", "tool_data" -> listOf(
                // Binary IEC Standard (1024 based)
                UnitItem("kib", "Kibibyte", "KiB", 1024.0),
                UnitItem("mib", "Mebibyte", "MiB", 1024.0 * 1024.0),
                UnitItem("gib", "Gibibyte", "GiB", 1024.0 * 1024.0 * 1024.0),
                UnitItem("tib", "Tebibyte", "TiB", 1024.0 * 1024.0 * 1024.0 * 1024.0),
                // Decimal SI Standard (1000 based)
                UnitItem("kb", "Kilobyte", "KB", 1000.0),
                UnitItem("mb", "Megabyte", "MB", 1000.0 * 1000.0),
                UnitItem("gb", "Gigabyte", "GB", 1000.0 * 1000.0 * 1000.0),
                UnitItem("tb", "Terabyte", "TB", 1000.0 * 1000.0 * 1000.0 * 1000.0),
                // Fundamental Base
                UnitItem("b", "Byte", "B", 1.0),
                UnitItem("bit", "Bit", "b", 0.125)
            )
            "len", "length", "tool_length" -> listOf(
                UnitItem("m", "Meter", "m", 1.0),
                UnitItem("km", "Kilometer", "km", 1000.0),
                UnitItem("cm", "Centimeter", "cm", 0.01),
                UnitItem("mm", "Millimeter", "mm", 0.001),
                UnitItem("mi", "Mile", "mi", 1609.344),
                UnitItem("yd", "Yard", "yd", 0.9144),
                UnitItem("ft", "Foot", "ft", 0.3048),
                UnitItem("in", "Inch", "in", 0.0254)
            )
            "wt", "weight", "mass", "tool_weight" -> listOf(
                UnitItem("kg", "Kilogram", "kg", 1.0),
                UnitItem("g", "Gram", "g", 0.001),
                UnitItem("mg", "Milligram", "mg", 0.000001),
                UnitItem("t", "Metric Ton", "t", 1000.0),
                UnitItem("lb", "Pound", "lb", 0.45359237),
                UnitItem("oz", "Ounce", "oz", 0.02834952)
            )
            "area", "tool_area" -> listOf(
                UnitItem("sqm", "Square Meter", "m²", 1.0),
                UnitItem("sqkm", "Square Kilometer", "km²", 1000000.0),
                UnitItem("sqft", "Square Foot", "sq ft", 0.092903),
                UnitItem("ac", "Acre", "ac", 4046.86),
                UnitItem("ha", "Hectare", "ha", 10000.0),
                UnitItem("sqin", "Square Inch", "sq in", 0.00064516)
            )
            "vol", "volume", "tool_volume" -> listOf(
                UnitItem("l", "Liter", "L", 1.0),
                UnitItem("ml", "Milliliter", "mL", 0.001),
                UnitItem("cbm", "Cubic Meter", "m³", 1000.0),
                UnitItem("gal", "Gallon (US)", "gal", 3.78541),
                UnitItem("floz", "Fluid Ounce (US)", "fl oz", 0.0295735)
            )
            "temp", "temperature", "tool_temperature" -> listOf(
                UnitItem("c", "Celsius", "°C", 1.0),
                UnitItem("f", "Fahrenheit", "°F", 1.0),
                UnitItem("k", "Kelvin", "K", 1.0)
            )
            "spd", "speed", "tool_speed" -> listOf(
                UnitItem("kmh", "Kilometers per hour", "km/h", 1.0),
                UnitItem("mps", "Meters per second", "m/s", 3.6),
                UnitItem("mph", "Miles per hour", "mph", 1.60934),
                UnitItem("kn", "Knots", "kn", 1.852)
            )
            "time", "tool_time" -> listOf(
                UnitItem("s", "Second", "s", 1.0),
                UnitItem("min", "Minute", "min", 60.0),
                UnitItem("h", "Hour", "h", 3600.0),
                UnitItem("d", "Day", "d", 86400.0),
                UnitItem("wk", "Week", "wk", 604800.0),
                UnitItem("yr", "Year", "yr", 31536000.0)
            )
            "press", "pressure", "tool_pressure" -> listOf(
                UnitItem("bar", "Bar", "bar", 1.0),
                UnitItem("pa", "Pascal", "Pa", 0.00001),
                UnitItem("kpa", "Kilopascal", "kPa", 0.01),
                UnitItem("psi", "Pound per sq in", "psi", 0.0689476),
                UnitItem("atm", "Atmosphere", "atm", 1.01325)
            )
            "eng", "energy", "tool_energy" -> listOf(
                UnitItem("j", "Joule", "J", 1.0),
                UnitItem("kj", "Kilojoule", "kJ", 1000.0),
                UnitItem("kcal", "Kilocalorie", "kcal", 4184.0),
                UnitItem("kwh", "Kilowatt-hour", "kWh", 3600000.0)
            )
            "pwr", "power", "tool_power" -> listOf(
                UnitItem("w", "Watt", "W", 1.0),
                UnitItem("kw", "Kilowatt", "kW", 1000.0),
                UnitItem("hp", "Horsepower", "hp", 745.699872),
                UnitItem("mw", "Megawatt", "MW", 1000000.0)
            )
            else -> listOf(
                UnitItem("m", "Meter", "m", 1.0),
                UnitItem("cm", "Centimeter", "cm", 0.01)
            )
        }
    }

    fun convert(valStr: String, from: UnitItem, to: UnitItem, toolId: String): String {
        val tid = toolId.lowercase().trim()
        val cleanInput = valStr.trim().replace(" ", "")
        if (cleanInput.isEmpty()) return "0"
        if (from.id == to.id) return cleanInput

        // Arbitrary-Length Number System Conversion via BigInteger
        if (tid in listOf("num_sys", "number_system", "tool_num_system")) {
            return try {
                val fromRadix = when (from.id) {
                    "bin" -> 2
                    "oct" -> 8
                    "hex" -> 16
                    else -> 10
                }
                val toRadix = when (to.id) {
                    "bin" -> 2
                    "oct" -> 8
                    "hex" -> 16
                    else -> 10
                }
                val bigIntVal = BigInteger(cleanInput, fromRadix)
                val converted = bigIntVal.toString(toRadix)
                if (to.id == "hex") converted.uppercase() else converted
            } catch (_: Exception) {
                "0"
            }
        }

        val v = cleanInput.toDoubleOrNull() ?: return "0"

        // Temperature Handling
        val result = if (tid in listOf("temp", "temperature", "tool_temperature")) {
            val celsius = when (from.id) {
                "f" -> (v - 32.0) * 5.0 / 9.0
                "k" -> v - 273.15
                else -> v
            }
            when (to.id) {
                "f" -> (celsius * 9.0 / 5.0) + 32.0
                "k" -> celsius + 273.15
                else -> celsius
            }
        } else {
            val baseVal = v * from.toBaseFactor
            baseVal / to.toBaseFactor
        }

        return if (result == 0.0) {
            "0"
        } else {
            BigDecimal.valueOf(result)
                .setScale(8, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
        }
    }
}