package com.universal.calulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun formatConverterNumber(input: String, isBinary: Boolean, isDecOrUnit: Boolean): String {
    if (input.isEmpty() || input == "-" || input == ".") return input
    if (isBinary) {
        return BaseMathEngine.formatBinarySpaced(input)
    }
    if (!isDecOrUnit) {
        return input // HEX or OCTAL clean format
    }

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
fun UnitConverterScreen(
    toolId: String,
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val tid = toolId.lowercase().trim()

    val isTemperature = tid in listOf("temp", "temperature", "tool_temperature")
    val isNumSys = tid in listOf("num_sys", "number_system", "tool_num_system")

    val screenTitle = when (tid) {
        "len", "length", "tool_length" -> "Length Conversion"
        "wt", "weight", "mass", "tool_weight" -> "Weight Conversion"
        "area", "tool_area" -> "Area Conversion"
        "vol", "volume", "tool_volume" -> "Volume Conversion"
        "temp", "temperature", "tool_temperature" -> "Temperature Conversion"
        "spd", "speed", "tool_speed" -> "Speed Conversion"
        "time", "tool_time" -> "Time Conversion"
        "data", "tool_data" -> "Data Storage Conversion"
        "press", "pressure", "tool_pressure" -> "Pressure Conversion"
        "eng", "energy", "tool_energy" -> "Energy Conversion"
        "pwr", "power", "tool_power" -> "Power Conversion"
        "num_sys", "number_system", "tool_num_system" -> "Number System"
        else -> "Unit Conversion"
    }

    val unitList = remember(tid) { UnitConverterEngine.getUnitsForTool(tid) }
    var fromUnit by remember(tid) { mutableStateOf(unitList.first()) }
    var toUnit by remember(tid) { mutableStateOf(if (unitList.size > 1) unitList[1] else unitList.first()) }
    var inputStr by remember(tid) { mutableStateOf(if (isNumSys) "10" else "1") }
    var showFromMenu by remember { mutableStateOf(false) }
    var showToMenu by remember { mutableStateOf(false) }

    val rawOutput = remember(inputStr, fromUnit, toUnit, tid) {
        UnitConverterEngine.convert(inputStr, fromUnit, toUnit, tid)
    }

    // Input & Output Unified Formatter
    val isInputBin = isNumSys && fromUnit.id == "bin"
    val isInputDecOrUnit = !isNumSys || fromUnit.id == "dec"
    val formattedInput = remember(inputStr, isInputBin, isInputDecOrUnit) {
        formatConverterNumber(inputStr, isInputBin, isInputDecOrUnit)
    }

    val isOutputBin = isNumSys && toUnit.id == "bin"
    val isOutputDecOrUnit = !isNumSys || toUnit.id == "dec"
    val formattedOutput = remember(rawOutput, isOutputBin, isOutputDecOrUnit) {
        formatConverterNumber(rawOutput, isOutputBin, isOutputDecOrUnit)
    }

    val fromScrollState = rememberScrollState()
    val toScrollState = rememberScrollState()

    LaunchedEffect(formattedInput) {
        fromScrollState.scrollTo(fromScrollState.maxValue)
    }
    LaunchedEffect(formattedOutput) {
        toScrollState.scrollTo(toScrollState.maxValue)
    }

    fun isKeyEnabled(k: String): Boolean {
        if (!isNumSys) return true
        return when (fromUnit.id) {
            "bin" -> k in listOf("0", "1")
            "oct" -> k in listOf("0", "1", "2", "3", "4", "5", "6", "7")
            "dec" -> k in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9")
            "hex" -> true
            else -> true
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
                text = screenTitle,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        // Top-Anchored Conversion Cards Viewport
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // From Card
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
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showFromMenu = true }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${fromUnit.name} (${fromUnit.symbol})",
                                    fontSize = 13.sp,
                                    color = theme.textSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                            }

                            DropdownMenu(
                                expanded = showFromMenu,
                                onDismissRequest = { showFromMenu = false },
                                modifier = Modifier.background(theme.surface)
                            ) {
                                unitList.forEach { u ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${u.name} (${u.symbol})",
                                                color = if (u.id == fromUnit.id) theme.accent else theme.textPrimary,
                                                fontWeight = if (u.id == fromUnit.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            fromUnit = u
                                            inputStr = "0"
                                            showFromMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        val fromFontSize = when {
                            formattedInput.length <= 8 -> 34.sp
                            formattedInput.length <= 14 -> 26.sp
                            formattedInput.length <= 20 -> 20.sp
                            else -> 16.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(fromScrollState)
                        ) {
                            Text(
                                text = formattedInput.ifEmpty { "0" },
                                fontSize = fromFontSize,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // To Card
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
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showToMenu = true }
                                    .padding(vertical = 2.dp, horizontal = 4.dp)
                            ) {
                                Text(
                                    text = "${toUnit.name} (${toUnit.symbol})",
                                    fontSize = 13.sp,
                                    color = theme.textSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = theme.textSecondary, modifier = Modifier.size(18.dp))
                            }

                            DropdownMenu(
                                expanded = showToMenu,
                                onDismissRequest = { showToMenu = false },
                                modifier = Modifier.background(theme.surface)
                            ) {
                                unitList.forEach { u ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${u.name} (${u.symbol})",
                                                color = if (u.id == toUnit.id) theme.accent else theme.textPrimary,
                                                fontWeight = if (u.id == toUnit.id) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            toUnit = u
                                            showToMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        val toFontSize = when {
                            formattedOutput.length <= 8 -> 34.sp
                            formattedOutput.length <= 14 -> 26.sp
                            formattedOutput.length <= 20 -> 20.sp
                            else -> 16.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(toScrollState)
                        ) {
                            Text(
                                text = formattedOutput,
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

            // Centered Floating Swap Button
            Surface(
                shape = CircleShape,
                color = theme.funcBtn,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.4f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(42.dp)
                    .clickable {
                        val temp = fromUnit
                        fromUnit = toUnit
                        toUnit = temp
                        inputStr = "0"
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.SwapVert,
                        contentDescription = "Swap",
                        tint = theme.accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Spacer pushes keypad down and keeps cards anchored on top
        Spacer(Modifier.weight(1f))

        // Hex Alphabet Row
        if (isNumSys) {
            val hexLetters = listOf("A", "B", "C", "D", "E", "F")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                hexLetters.forEach { letter ->
                    val enabled = fromUnit.id == "hex"
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (enabled) theme.funcBtn else theme.numBtn.copy(alpha = 0.4f),
                        border = if (enabled) androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable(enabled = enabled) {
                                if (inputStr == "0") inputStr = letter
                                else if (inputStr.length < 64) inputStr += letter
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = letter,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (enabled) theme.accent else theme.textSecondary.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Keypad
        val keys = if (isNumSys) {
            listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3")
            )
        } else {
            val bottomSpecialKey = if (isTemperature) "±" else "00"
            listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf(bottomSpecialKey, "0", ".")
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Numbers Column
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                keys.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            val isSpecial = key in listOf("±", "00", ".")
                            val enabled = if (isSpecial) true else isKeyEnabled(key)

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (enabled) theme.numBtn else theme.numBtn.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(58.dp)
                                    .clickable(enabled = enabled) {
                                        when (key) {
                                            "±" -> {
                                                if (inputStr != "0" && inputStr.isNotEmpty()) {
                                                    inputStr = if (inputStr.startsWith("-")) inputStr.removePrefix("-") else "-$inputStr"
                                                }
                                            }
                                            "." -> {
                                                if (!inputStr.contains(".")) inputStr += "."
                                            }
                                            "00" -> {
                                                if (inputStr != "0" && inputStr.length < 60) inputStr += "00"
                                            }
                                            else -> {
                                                if (inputStr == "0") inputStr = key
                                                else if (inputStr == "-0") inputStr = "-$key"
                                                else if (inputStr.length < 64) inputStr += key
                                            }
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = key,
                                        fontSize = if (key == "±") 22.sp else 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (enabled) {
                                            if (key == "±") theme.accent else theme.textPrimary
                                        } else theme.textSecondary.copy(alpha = 0.35f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Number System Wide '0'
                if (isNumSys) {
                    val enabled = isKeyEnabled("0")
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (enabled) theme.numBtn else theme.numBtn.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .clickable(enabled = enabled) {
                                if (inputStr != "0" && inputStr.length < 64) {
                                    inputStr += "0"
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "0",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (enabled) theme.textPrimary else theme.textSecondary.copy(alpha = 0.35f)
                            )
                        }
                    }
                }
            }

            // Action Side Buttons (DEL & AC)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val actionHeight = if (isNumSys) 126.dp else 126.dp

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.funcBtn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(actionHeight)
                        .clickable {
                            if (inputStr.isNotEmpty()) {
                                inputStr = inputStr.dropLast(1)
                                if (inputStr.isEmpty() || inputStr == "-") inputStr = "0"
                            }
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Delete",
                            tint = theme.accent,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.funcBtn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(actionHeight)
                        .clickable { inputStr = "0" }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "AC",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent
                        )
                    }
                }
            }
        }
    }
}