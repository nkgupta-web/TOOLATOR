package com.universal.calulator

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

data class CurrencyItem(val code: String, val name: String, val flag: String)

data class CurrencyHistoryRecord(
    val id: String = UUID.randomUUID().toString(),
    val fromCode: String,
    val fromFlag: String,
    val fromAmount: String,
    val toCode: String,
    val toFlag: String,
    val toAmount: String,
    val rateStatus: String,
    val timestamp: Long = System.currentTimeMillis()
)

object CurrencyHistoryManager {
    private const val PREFS_NAME = "currency_history_prefs"
    private const val KEY_HISTORY = "history_records"
    const val MAX_CURRENCY_HISTORY_LIMIT = 250

    fun loadHistory(context: Context): List<CurrencyHistoryRecord> {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = sp.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<CurrencyHistoryRecord>()
            for (i in 0 until arr.length().coerceAtMost(MAX_CURRENCY_HISTORY_LIMIT)) {
                val obj = arr.getJSONObject(i)
                list.add(
                    CurrencyHistoryRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        fromCode = obj.getString("fromCode"),
                        fromFlag = obj.optString("fromFlag", "🌐"),
                        fromAmount = obj.getString("fromAmount"),
                        toCode = obj.getString("toCode"),
                        toFlag = obj.optString("toFlag", "🌐"),
                        toAmount = obj.getString("toAmount"),
                        rateStatus = obj.optString("rateStatus", "Offline"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveHistory(context: Context, list: List<CurrencyHistoryRecord>) {
        try {
            val arr = JSONArray()
            list.take(MAX_CURRENCY_HISTORY_LIMIT).forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("fromCode", item.fromCode)
                    put("fromFlag", item.fromFlag)
                    put("fromAmount", item.fromAmount)
                    put("toCode", item.toCode)
                    put("toFlag", item.toFlag)
                    put("toAmount", item.toAmount)
                    put("rateStatus", item.rateStatus)
                    put("timestamp", item.timestamp)
                }
                arr.put(obj)
            }
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_HISTORY, arr.toString())
                .apply()
        } catch (_: Exception) {}
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY)
            .apply()
    }
}

object CurrencyRateStore {
    val fallbackRates = mapOf(
        "USD" to 1.0,
        "INR" to 95.7937,
        "EUR" to 0.95,
        "GBP" to 0.79,
        "AED" to 3.67,
        "SAR" to 3.75,
        "CAD" to 1.42,
        "AUD" to 1.58,
        "JPY" to 154.20,
        "CNY" to 7.28,
        "SGD" to 1.35,
        "CHF" to 0.88,
        "NZD" to 1.72,
        "KRW" to 1435.0,
        "HKD" to 7.78,
        "SEK" to 10.95,
        "NOK" to 11.10,
        "DKK" to 7.10,
        "RUB" to 98.50,
        "TRY" to 35.20,
        "BRL" to 5.80,
        "MXN" to 20.40,
        "ZAR" to 18.10,
        "KWD" to 0.31,
        "BHD" to 0.38,
        "OMR" to 0.38,
        "QAR" to 3.64,
        "JOD" to 0.71,
        "ILS" to 3.65,
        "IQD" to 1310.0,
        "LBP" to 89500.0,
        "EGP" to 50.40,
        "IRR" to 42000.0,
        "YER" to 250.0,
        "PKR" to 278.50,
        "BDT" to 120.0,
        "LKR" to 292.0,
        "NPR" to 153.20,
        "AFN" to 68.50,
        "MYR" to 4.45,
        "THB" to 34.50,
        "IDR" to 15900.0,
        "PHP" to 58.20,
        "VND" to 25400.0,
        "TWD" to 32.50,
        "MOP" to 8.01,
        "MMK" to 2100.0,
        "KHR" to 4050.0,
        "LAK" to 21800.0,
        "BND" to 1.35,
        "MVR" to 15.40,
        "BTN" to 95.79,
        "MNT" to 3410.0,
        "KZT" to 495.0,
        "UZS" to 12850.0,
        "AZN" to 1.70,
        "GEL" to 2.78,
        "AMD" to 387.0,
        "FJD" to 2.27,
        "PGK" to 3.96,
        "PLN" to 4.09,
        "CZK" to 24.10,
        "HUF" to 388.0,
        "RON" to 4.72,
        "BGN" to 1.86,
        "RSD" to 111.50,
        "UAH" to 41.60,
        "BYN" to 3.28,
        "ISK" to 138.0,
        "BAM" to 1.86,
        "ALL" to 93.50,
        "MKD" to 58.50,
        "MDL" to 18.20,
        "NGN" to 1680.0,
        "KES" to 129.50,
        "GHS" to 16.20,
        "MAD" to 10.05,
        "DZD" to 134.0,
        "TND" to 3.15,
        "UGX" to 3670.0,
        "TZS" to 2600.0,
        "ETB" to 124.0,
        "ZMW" to 27.50,
        "BWP" to 13.60,
        "MUR" to 46.50,
        "NAD" to 18.10,
        "RWF" to 1380.0,
        "MZN" to 63.80,
        "AOA" to 915.0,
        "XOF" to 623.0,
        "XAF" to 623.0,
        "SCR" to 14.10,
        "SDG" to 600.0,
        "LYD" to 4.88,
        "ARS" to 1020.0,
        "CLP" to 975.0,
        "COP" to 4380.0,
        "PEN" to 3.78,
        "UYU" to 42.80,
        "BOB" to 6.92,
        "PYG" to 7850.0,
        "CRC" to 508.0,
        "DOP" to 60.20,
        "GTQ" to 7.72,
        "HNL" to 25.20,
        "NIO" to 36.80,
        "PAB" to 1.0,
        "JMD" to 158.0,
        "TTD" to 6.78,
        "BSD" to 1.0,
        "BBD" to 2.0,
        "CUP" to 24.0,
        "VES" to 46.50,
        "XAU" to 0.00037,
        "XAG" to 0.031,
        "XPT" to 0.00105,
        "XPD" to 0.00102
    )

    fun getSafeRate(code: String, currentMap: Map<String, Double>): Double {
        val cleanCode = code.trim().uppercase()
        val rateFromMap = currentMap[cleanCode]
        if (rateFromMap != null && rateFromMap > 0.0) {
            return rateFromMap
        }
        val rateFromFallback = fallbackRates[cleanCode]
        if (rateFromFallback != null && rateFromFallback > 0.0) {
            return rateFromFallback
        }
        return 1.0
    }
}

fun formatLiveComma(input: String): String {
    if (input.isEmpty() || input == "-" || input == ".") return input
    val isNegative = input.startsWith("-")
    val clean = input.removePrefix("-")
    val parts = clean.split(".")
    val intPart = parts[0]
    val fracPart = if (parts.size > 1) ".${parts[1]}" else if (input.endsWith(".")) "." else ""
    if (intPart.isEmpty()) return "${if (isNegative) "-" else ""}$fracPart"
    val formattedInt = intPart.reversed().chunked(3).joinToString(",").reversed()
    return "${if (isNegative) "-" else ""}$formattedInt$fracPart"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isHapticEnabled = LocalHapticEnabled.current.value
    val coroutineScope = rememberCoroutineScope()

    fun triggerHaptic() {
        if (isHapticEnabled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    var amountInput by remember { mutableStateOf("1") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("INR") }

    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val historyList = remember {
        mutableStateListOf<CurrencyHistoryRecord>().apply {
            addAll(CurrencyHistoryManager.loadHistory(context))
        }
    }

    val initialCachedData = remember { readCache(context) }
    var ratesMap by remember {
        mutableStateOf<Map<String, Double>>(initialCachedData?.first ?: CurrencyRateStore.fallbackRates)
    }
    var lastUpdated by remember {
        mutableStateOf(if (initialCachedData != null) "Offline (${initialCachedData.second})" else "Standard Offline")
    }
    var isRefreshing by remember { mutableStateOf(false) }

    val currencies = remember {
        listOf(
            CurrencyItem("USD", "US Dollar", "🇺🇸"),
            CurrencyItem("INR", "Indian Rupee", "🇮🇳"),
            CurrencyItem("EUR", "Euro", "🇪🇺"),
            CurrencyItem("GBP", "British Pound", "🇬🇧"),
            CurrencyItem("JPY", "Japanese Yen", "🇯🇵"),
            CurrencyItem("AED", "UAE Dirham", "🇦🇪"),
            CurrencyItem("SAR", "Saudi Riyal", "🇸🇦"),
            CurrencyItem("CAD", "Canadian Dollar", "🇨🇦"),
            CurrencyItem("AUD", "Australian Dollar", "🇦🇺"),
            CurrencyItem("CHF", "Swiss Franc", "🇨🇭"),
            CurrencyItem("CNY", "Chinese Yuan", "🇨🇳"),
            CurrencyItem("SGD", "Singapore Dollar", "🇸🇬"),
            CurrencyItem("NZD", "New Zealand Dollar", "🇳🇿"),
            CurrencyItem("KRW", "South Korean Won", "🇰🇷"),
            CurrencyItem("HKD", "Hong Kong Dollar", "🇭🇰"),
            CurrencyItem("SEK", "Swedish Krona", "🇸🇪"),
            CurrencyItem("NOK", "Norwegian Krone", "🇳🇴"),
            CurrencyItem("DKK", "Danish Krone", "🇩🇰"),
            CurrencyItem("RUB", "Russian Ruble", "🇷🇺"),
            CurrencyItem("TRY", "Turkish Lira", "🇹🇷"),
            CurrencyItem("BRL", "Brazilian Real", "🇧🇷"),
            CurrencyItem("MXN", "Mexican Peso", "🇲🇽"),
            CurrencyItem("ZAR", "South African Rand", "🇿🇦"),
            CurrencyItem("KWD", "Kuwaiti Dinar", "🇰🇼"),
            CurrencyItem("BHD", "Bahraini Dinar", "🇧🇭"),
            CurrencyItem("OMR", "Omani Rial", "🇴🇲"),
            CurrencyItem("QAR", "Qatari Riyal", "🇶🇦"),
            CurrencyItem("JOD", "Jordanian Dinar", "🇯🇴"),
            CurrencyItem("ILS", "Israeli Shekel", "🇮🇱"),
            CurrencyItem("IQD", "Iraqi Dinar", "🇮🇶"),
            CurrencyItem("LBP", "Lebanese Pound", "🇱🇧"),
            CurrencyItem("EGP", "Egyptian Pound", "🇪🇬"),
            CurrencyItem("IRR", "Iranian Rial", "🇮🇷"),
            CurrencyItem("YER", "Yemeni Rial", "🇾🇪"),
            CurrencyItem("PKR", "Pakistani Rupee", "🇵🇰"),
            CurrencyItem("BDT", "Bangladeshi Taka", "🇧🇩"),
            CurrencyItem("LKR", "Sri Lankan Rupee", "🇱🇰"),
            CurrencyItem("NPR", "Nepalese Rupee", "🇳🇵"),
            CurrencyItem("AFN", "Afghan Afghani", "🇦🇫"),
            CurrencyItem("MYR", "Malaysian Ringgit", "🇲🇾"),
            CurrencyItem("THB", "Thai Baht", "🇹🇭"),
            CurrencyItem("IDR", "Indonesian Rupiah", "🇮🇩"),
            CurrencyItem("PHP", "Philippine Peso", "🇵🇭"),
            CurrencyItem("VND", "Vietnamese Dong", "🇻🇳"),
            CurrencyItem("TWD", "New Taiwan Dollar", "🇹🇼"),
            CurrencyItem("MOP", "Macanese Pataca", "🇲🇴"),
            CurrencyItem("MMK", "Myanmar Kyat", "🇲🇲"),
            CurrencyItem("KHR", "Cambodian Riel", "🇰🇭"),
            CurrencyItem("LAK", "Lao Kip", "🇱🇦"),
            CurrencyItem("BND", "Brunei Dollar", "🇧🇳"),
            CurrencyItem("MVR", "Maldivian Rufiyaa", "🇲🇻"),
            CurrencyItem("BTN", "Bhutanese Ngultrum", "🇧🇹"),
            CurrencyItem("MNT", "Mongolian Tugrik", "🇲🇳"),
            CurrencyItem("KZT", "Kazakhstani Tenge", "🇰🇿"),
            CurrencyItem("UZS", "Uzbekistani Som", "🇺🇿"),
            CurrencyItem("AZN", "Azerbaijani Manat", "🇦🇿"),
            CurrencyItem("GEL", "Georgian Lari", "🇬🇪"),
            CurrencyItem("AMD", "Armenian Dram", "🇦🇲"),
            CurrencyItem("FJD", "Fijian Dollar", "🇫🇯"),
            CurrencyItem("PGK", "Papua New Guinean Kina", "🇵🇬"),
            CurrencyItem("PLN", "Polish Zloty", "🇵🇱"),
            CurrencyItem("CZK", "Czech Koruna", "🇨🇿"),
            CurrencyItem("HUF", "Hungarian Forint", "🇭🇺"),
            CurrencyItem("RON", "Romanian Leu", "🇷🇴"),
            CurrencyItem("BGN", "Bulgarian Lev", "🇧🇬"),
            CurrencyItem("RSD", "Serbian Dinar", "🇷🇸"),
            CurrencyItem("UAH", "Ukrainian Hryvnia", "🇺🇦"),
            CurrencyItem("BYN", "Belarusian Ruble", "🇧🇾"),
            CurrencyItem("ISK", "Icelandic Krona", "🇮🇸"),
            CurrencyItem("BAM", "Bosnia Convertible Mark", "🇧🇦"),
            CurrencyItem("ALL", "Albanian Lek", "🇦🇱"),
            CurrencyItem("MKD", "Macedonian Denar", "🇲🇰"),
            CurrencyItem("MDL", "Moldovan Leu", "🇲🇩"),
            CurrencyItem("NGN", "Nigerian Naira", "🇳🇬"),
            CurrencyItem("KES", "Kenyan Shilling", "🇰🇪"),
            CurrencyItem("GHS", "Ghanaian Cedi", "🇬🇭"),
            CurrencyItem("MAD", "Moroccan Dirham", "🇲🇦"),
            CurrencyItem("DZD", "Algerian Dinar", "🇩🇿"),
            CurrencyItem("TND", "Tunisian Dinar", "🇹🇳"),
            CurrencyItem("UGX", "Ugandan Shilling", "🇺🇬"),
            CurrencyItem("TZS", "Tanzanian Shilling", "🇹🇿"),
            CurrencyItem("ETB", "Ethiopian Birr", "🇪🇹"),
            CurrencyItem("ZMW", "Zambian Kwacha", "🇿🇲"),
            CurrencyItem("BWP", "Botswanan Pula", "🇧🇼"),
            CurrencyItem("MUR", "Mauritian Rupee", "🇲🇺"),
            CurrencyItem("NAD", "Namibian Dollar", "🇳🇦"),
            CurrencyItem("RWF", "Rwandan Franc", "🇷🇼"),
            CurrencyItem("MZN", "Mozambican Metical", "🇲🇿"),
            CurrencyItem("AOA", "Angolan Kwanza", "🇦🇴"),
            CurrencyItem("XOF", "West African CFA Franc", "🌍"),
            CurrencyItem("XAF", "Central African CFA Franc", "🌍"),
            CurrencyItem("SCR", "Seychellois Rupee", "🇸🇨"),
            CurrencyItem("SDG", "Sudanese Pound", "🇸🇩"),
            CurrencyItem("LYD", "Libyan Dinar", "🇱🇾"),
            CurrencyItem("ARS", "Argentine Peso", "🇦🇷"),
            CurrencyItem("CLP", "Chilean Peso", "🇨🇱"),
            CurrencyItem("COP", "Colombian Peso", "🇨🇴"),
            CurrencyItem("PEN", "Peruvian Sol", "🇵🇪"),
            CurrencyItem("UYU", "Uruguayan Peso", "🇺🇾"),
            CurrencyItem("BOB", "Bolivian Boliviano", "🇧🇴"),
            CurrencyItem("PYG", "Paraguayan Guarani", "🇵🇾"),
            CurrencyItem("CRC", "Costa Rican Colon", "🇨🇷"),
            CurrencyItem("DOP", "Dominican Peso", "🇩🇴"),
            CurrencyItem("GTQ", "Guatemalan Quetzal", "🇬🇹"),
            CurrencyItem("HNL", "Honduran Lempira", "🇭🇳"),
            CurrencyItem("NIO", "Nicaraguan Cordoba", "🇳🇮"),
            CurrencyItem("PAB", "Panamanian Balboa", "🇵🇦"),
            CurrencyItem("JMD", "Jamaican Dollar", "🇯🇲"),
            CurrencyItem("TTD", "Trinidad Dollar", "🇹🇹"),
            CurrencyItem("BSD", "Bahamian Dollar", "🇧🇸"),
            CurrencyItem("BBD", "Barbadian Dollar", "🇧🇧"),
            CurrencyItem("CUP", "Cuban Peso", "🇨🇺"),
            CurrencyItem("VES", "Venezuelan Bolivar", "🇻🇪"),
            CurrencyItem("XAU", "Gold (Troy Ounce)", "🪙"),
            CurrencyItem("XAG", "Silver (Troy Ounce)", "🥈"),
            CurrencyItem("XPT", "Platinum (Troy Ounce)", "⚪"),
            CurrencyItem("XPD", "Palladium (Troy Ounce)", "🔘")
        )
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isRefreshing = true
            val live = fetchNetworkRates(context)
            if (live != null) {
                ratesMap = live.first
                lastUpdated = "Live (${live.second})"
            } else {
                val currentCache = readCache(context)
                if (currentCache != null) {
                    ratesMap = currentCache.first
                    lastUpdated = "Offline (${currentCache.second})"
                }
            }
            isRefreshing = false
        }
    }

    val amount = amountInput.toDoubleOrNull() ?: 0.0

    val fromRate = CurrencyRateStore.getSafeRate(fromCurrency, ratesMap)
    val toRate = CurrencyRateStore.getSafeRate(toCurrency, ratesMap)

    val convertedAmount = (amount / fromRate) * toRate
    val unitRate = (1.0 / fromRate) * toRate

    fun fmtAmount(n: Double): String {
        return when {
            n == 0.0 -> "0.00"
            n < 0.0001 -> DecimalFormat("0.######").format(n)
            n < 1.0 -> DecimalFormat("0.####").format(n)
            else -> DecimalFormat("#,##0.00").format(n)
        }
    }

    val displayInputFormatted = remember(amountInput) { formatLiveComma(amountInput) }
    val convertedStr = fmtAmount(convertedAmount)

    val fromItem = currencies.find { it.code == fromCurrency } ?: CurrencyItem(fromCurrency, "", "🌐")
    val toItem = currencies.find { it.code == toCurrency } ?: CurrencyItem(toCurrency, "", "🌐")

    val fromScrollState = rememberScrollState()
    val toScrollState = rememberScrollState()

    LaunchedEffect(displayInputFormatted) {
        fromScrollState.scrollTo(fromScrollState.maxValue)
    }
    LaunchedEffect(convertedStr) {
        toScrollState.scrollTo(toScrollState.maxValue)
    }

    fun saveCalculationToHistory() {
        if (amount <= 0.0) return

        val rateMode = if (lastUpdated.startsWith("Live", ignoreCase = true)) "Live" else "Offline"
        val first = historyList.firstOrNull()

        // Smart Duplicate Avoidance
        val isDuplicate = first != null &&
                first.fromCode == fromCurrency &&
                first.toCode == toCurrency &&
                first.fromAmount == displayInputFormatted &&
                first.toAmount == convertedStr

        if (!isDuplicate) {
            val record = CurrencyHistoryRecord(
                fromCode = fromCurrency,
                fromFlag = fromItem.flag,
                fromAmount = displayInputFormatted,
                toCode = toCurrency,
                toFlag = toItem.flag,
                toAmount = convertedStr,
                rateStatus = rateMode
            )
            historyList.add(0, record)

            // Auto-drop oldest if size exceeds 250
            if (historyList.size > CurrencyHistoryManager.MAX_CURRENCY_HISTORY_LIMIT) {
                historyList.removeAt(historyList.lastIndex)
            }

            CurrencyHistoryManager.saveHistory(context, historyList)
        }
    }

    fun onKeyPress(key: String) {
        triggerHaptic()
        when (key) {
            "AC" -> amountInput = "0"
            "DEL" -> {
                amountInput = if (amountInput.length > 1) amountInput.dropLast(1) else "0"
            }
            "=" -> {
                saveCalculationToHistory()
            }
            "." -> {
                if (!amountInput.contains(".")) amountInput += "."
            }
            "00" -> {
                if (amountInput != "0" && amountInput.length <= 16) amountInput += "00"
            }
            else -> {
                if (amountInput == "0") amountInput = key else if (amountInput.length <= 18) amountInput += key
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Bar
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
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Currency Converter",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // History Button (Clean icon without badge number)
                IconButton(onClick = { showHistorySheet = true }, modifier = Modifier.size(34.dp)) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = theme.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Refresh Live Rates
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isRefreshing = true
                            val live = fetchNetworkRates(context)
                            if (live != null) {
                                ratesMap = live.first
                                lastUpdated = "Live (${live.second})"
                            } else {
                                val cached = readCache(context)
                                lastUpdated = if (cached != null) "Offline (${cached.second})" else "Offline"
                            }
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = theme.accent, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = theme.textSecondary, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Conversion Display Cards
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Input Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showFromPicker = true }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${fromItem.name} (${fromItem.code})",
                                fontSize = 13.sp,
                                color = theme.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                        }

                        val fromFontSize = when {
                            displayInputFormatted.length <= 8 -> 34.sp
                            displayInputFormatted.length <= 13 -> 26.sp
                            displayInputFormatted.length <= 18 -> 20.sp
                            else -> 16.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(fromScrollState)
                        ) {
                            Text(
                                text = displayInputFormatted,
                                fontSize = fromFontSize,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Bottom Output Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showToPicker = true }
                                .padding(vertical = 2.dp, horizontal = 4.dp)
                        ) {
                            Text(
                                text = "${toItem.name} (${toItem.code})",
                                fontSize = 13.sp,
                                color = theme.textSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                        }

                        val toFontSize = when {
                            convertedStr.length <= 8 -> 34.sp
                            convertedStr.length <= 13 -> 26.sp
                            convertedStr.length <= 18 -> 20.sp
                            else -> 16.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(toScrollState)
                        ) {
                            Text(
                                text = convertedStr,
                                fontSize = toFontSize,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            // Floating Swap Currency Button
            Surface(
                shape = CircleShape,
                color = theme.funcBtn,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.4f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        triggerHaptic()
                        val temp = fromCurrency
                        fromCurrency = toCurrency
                        toCurrency = temp
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SwapVert,
                        contentDescription = "Swap",
                        tint = theme.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Subtitle Rates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "1 $fromCurrency = ${fmtAmount(unitRate)} $toCurrency",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.textSecondary
            )
            Text(
                text = lastUpdated,
                fontSize = 10.sp,
                color = theme.textSecondary
            )
        }

        Spacer(Modifier.weight(1f))

        // Keypad Grid with Dedicated = Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Numbers 3-Column
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val numGrid = listOf(
                    listOf("7", "8", "9"),
                    listOf("4", "5", "6"),
                    listOf("1", "2", "3"),
                    listOf("00", "0", ".")
                )

                numGrid.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = theme.numBtn,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable { onKeyPress(key) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Function Side Keys (DEL, AC, =)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.funcBtn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onKeyPress("DEL") }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = theme.accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.funcBtn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { onKeyPress("AC") }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AC",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent
                        )
                    }
                }

                // Dedicated Equals Button (Saves to History)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.3f)
                        .clickable { onKeyPress("=") }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "=",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (theme.isLight) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }

    // Currency Selection Modal Dialogs
    if (showFromPicker) {
        CurrencySelectionDialog(
            currencies = currencies,
            selected = fromCurrency,
            onDismiss = { showFromPicker = false },
            onSelect = { fromCurrency = it; showFromPicker = false }
        )
    }

    if (showToPicker) {
        CurrencySelectionDialog(
            currencies = currencies,
            selected = toCurrency,
            onDismiss = { showToPicker = false },
            onSelect = { toCurrency = it; showToPicker = false }
        )
    }

    // Conversion History Bottom Sheet
    if (showHistorySheet) {
        val sdf = remember { SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault()) }

        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Conversion History", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                        Text("Tap to restore conversion", fontSize = 12.sp, color = theme.textSecondary)
                    }

                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearConfirmDialog = true }
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = theme.accent)
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No currency conversions recorded yet.\nPress '=' to save a conversion.",
                            color = theme.textSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(historyList, key = { it.id }) { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = theme.numBtn,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        fromCurrency = item.fromCode
                                        toCurrency = item.toCode
                                        amountInput = item.fromAmount.replace(",", "")
                                        showHistorySheet = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${item.fromFlag} ${item.fromAmount} ${item.fromCode}  ➔  ${item.toFlag} ${item.toAmount} ${item.toCode}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            text = "${sdf.format(Date(item.timestamp))} • ${item.rateStatus}",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            historyList.remove(item)
                                            CurrencyHistoryManager.saveHistory(context, historyList)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = theme.textSecondary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Clear History Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear conversion history?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = theme.textPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all ${historyList.size} saved conversions.",
                    fontSize = 14.sp,
                    color = theme.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyList.clear()
                        CurrencyHistoryManager.clearHistory(context)
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear", color = theme.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
            containerColor = theme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CurrencySelectionDialog(
    currencies: List<CurrencyItem>,
    selected: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val theme = LocalAppTheme.current.value
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery) {
        if (searchQuery.isBlank()) currencies
        else currencies.filter {
            it.code.contains(searchQuery, ignoreCase = true) || it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = theme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
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
                    Text("Select Currency", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = theme.textSecondary)
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    textStyle = TextStyle(
                        color = theme.textPrimary,
                        fontSize = 14.sp
                    ),
                    placeholder = { Text("Search by code or name...", fontSize = 12.sp, color = theme.textSecondary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary,
                        focusedContainerColor = theme.numBtn,
                        unfocusedContainerColor = theme.numBtn,
                        focusedBorderColor = theme.accent,
                        unfocusedBorderColor = theme.funcBtn,
                        cursorColor = theme.accent
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredList) { curr ->
                        val isSelected = curr.code == selected
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) theme.accent.copy(alpha = 0.15f) else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(curr.code) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(text = curr.flag, fontSize = 20.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = curr.code,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) theme.accent else theme.textPrimary
                                    )
                                    Text(text = curr.name, fontSize = 11.sp, color = theme.textSecondary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun fetchNetworkRates(context: Context): Pair<Map<String, Double>, String>? {
    return withContext(Dispatchers.IO) {
        try {
            val map = HashMap<String, Double>(CurrencyRateStore.fallbackRates)

            val url = URL("https://open.er-api.com/v6/latest/USD")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 3000
                requestMethod = "GET"
            }

            if (conn.responseCode == 200) {
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                reader.close()

                val json = JSONObject(sb.toString())
                if (json.has("rates")) {
                    val ratesObj = json.getJSONObject("rates")
                    val keys = ratesObj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        map[k.trim().uppercase()] = ratesObj.getDouble(k)
                    }
                }
            }
            conn.disconnect()

            try {
                val metalsUrl = URL("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.json")
                val mConn = (metalsUrl.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 3000
                    readTimeout = 3000
                    requestMethod = "GET"
                }
                if (mConn.responseCode == 200) {
                    val mReader = BufferedReader(InputStreamReader(mConn.inputStream))
                    val mSb = StringBuilder()
                    var mLine: String?
                    while (mReader.readLine().also { mLine = it } != null) {
                        mSb.append(mLine)
                    }
                    mReader.close()

                    val mJson = JSONObject(mSb.toString())
                    if (mJson.has("usd")) {
                        val usdObj = mJson.getJSONObject("usd")
                        if (usdObj.has("xau")) map["XAU"] = usdObj.getDouble("xau")
                        if (usdObj.has("xag")) map["XAG"] = usdObj.getDouble("xag")
                        if (usdObj.has("xpt")) map["XPT"] = usdObj.getDouble("xpt")
                        if (usdObj.has("xpd")) map["XPD"] = usdObj.getDouble("xpd")
                    }
                }
                mConn.disconnect()
            } catch (_: Exception) {}

            val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
            saveCache(context, map, timeStr)
            Pair(map, timeStr)
        } catch (_: Exception) {
            null
        }
    }
}

private fun saveCache(context: Context, map: Map<String, Double>, timeStr: String) {
    try {
        val sp = context.getSharedPreferences("app_currency_cache_v4", Context.MODE_PRIVATE)
        val json = JSONObject(map as Map<*, *>)
        sp.edit()
            .putString("data", json.toString())
            .putString("time", timeStr)
            .apply()
    } catch (_: Exception) {}
}

private fun readCache(context: Context): Pair<Map<String, Double>, String>? {
    return try {
        val sp = context.getSharedPreferences("app_currency_cache_v4", Context.MODE_PRIVATE)
        val dataStr = sp.getString("data", null) ?: return null
        val timeStr = sp.getString("time", "Cached") ?: "Cached"

        val json = JSONObject(dataStr)
        val map = HashMap<String, Double>(CurrencyRateStore.fallbackRates)
        val keys = json.keys()
        while (keys.hasNext()) {
            val k = keys.next()
            map[k.trim().uppercase()] = json.getDouble(k)
        }
        if (map.size > 20 && map.containsKey("INR")) Pair(map, timeStr) else null
    } catch (_: Exception) {
        null
    }
}