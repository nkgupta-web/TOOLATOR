package com.universal.calulator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.media.AudioManager
import android.text.InputType
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.text.SimpleDateFormat
import java.util.*

fun formatMathExpression(expr: String): String {
    if (expr.isEmpty()) return ""
    val regex = Regex("""\d+(\.\d+)?""")
    return regex.replace(expr) { matchResult ->
        val numStr = matchResult.value
        val parts = numStr.split(".")
        val intPart = parts[0]
        val fracPart = if (parts.size > 1) ".${parts[1]}" else if (numStr.endsWith(".")) "." else ""
        val formattedInt = intPart.reversed().chunked(3).joinToString(",").reversed()
        "$formattedInt$fracPart"
    }
}

fun playKeyClickSound(context: Context, isEnabled: Boolean) {
    if (isEnabled) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK, 1.0f)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    onOpenTools: () -> Unit = {},
    onOpenAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    val view = LocalView.current

    val engine = remember { CalculatorEngine() }
    var resultPreview by remember { mutableStateOf("") }
    var isScientific by remember { mutableStateOf(false) }

    var isInverse by remember { mutableStateOf(false) }
    var angleMode by remember { mutableStateOf(AngleMode.DEG) }
    var isReciprocalTrigActive by remember { mutableStateOf(false) }

    val themeState = LocalAppTheme.current
    val hapticState = LocalHapticEnabled.current
    val selectedTheme = themeState.value

    var selectedLayout by remember { mutableStateOf(KeypadLayout.STANDARD) }

    var isKeySoundEnabled by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    var showCustomization by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showClearHistoryConfirmDialog by remember { mutableStateOf(false) }

    var historySearchQuery by remember { mutableStateOf("") }
    var tagDialogItem by remember { mutableStateOf<HistoryItem?>(null) }
    var tagInputText by remember { mutableStateOf("") }

    val historyList = remember {
        mutableStateListOf<HistoryItem>().apply {
            addAll(CalculatorHistoryManager.loadHistory(context))
        }
    }

    var rawInputText by remember { mutableStateOf("") }
    var editTextRef by remember { mutableStateOf<EditText?>(null) }
    var horizontalScrollRef by remember { mutableStateOf<HorizontalScrollView?>(null) }

    fun calculateDynamicTextSize(length: Int, sci: Boolean): Float {
        return if (sci) {
            when {
                length <= 8 -> 34f
                length <= 13 -> 26f
                length <= 18 -> 22f
                else -> 18f
            }
        } else {
            when {
                length <= 7 -> 46f
                length <= 10 -> 36f
                length <= 14 -> 28f
                length <= 18 -> 22f
                else -> 18f
            }
        }
    }

    fun updatePreview(text: String) {
        if (text.isBlank()) {
            resultPreview = ""
            return
        }
        val p = engine.evaluate(text)
        resultPreview = if (
            !p.contains("Invalid", ignoreCase = true) &&
            !p.contains("Error", ignoreCase = true) &&
            !p.contains("syntax", ignoreCase = true) &&
            !p.contains("Cannot", ignoreCase = true) &&
            !p.contains("Domain", ignoreCase = true) &&
            !p.contains("Negative", ignoreCase = true) &&
            !p.contains("Overflow", ignoreCase = true)
        ) {
            p
        } else {
            ""
        }
    }

    val copyToClipboard: (String) -> Unit = { text ->
        if (text.isNotEmpty() && text != "0") {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Calculator", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied: $text", Toast.LENGTH_SHORT).show()
        }
    }

    fun applyTextWithCursor(newFullText: String, targetCursorPos: Int) {
        rawInputText = newFullText
        val formatted = formatMathExpression(newFullText)

        editTextRef?.let { et ->
            et.setText(formatted)
            val safePos = targetCursorPos.coerceIn(0, formatted.length)
            et.setSelection(safePos)
            et.textSize = calculateDynamicTextSize(formatted.length, isScientific)
        }
        updatePreview(newFullText)
    }

    fun insertTextAtCursor(insertion: String) {
        val et = editTextRef
        val start = et?.selectionStart ?: rawInputText.length
        val end = et?.selectionEnd ?: rawInputText.length

        val prefix = rawInputText.substring(0, start.coerceAtMost(rawInputText.length))
        val suffix = rawInputText.substring(end.coerceAtMost(rawInputText.length))

        val newText = prefix + insertion + suffix
        val newCursor = start + insertion.length
        applyTextWithCursor(newText, newCursor)
    }

    fun deleteAtCursor() {
        val et = editTextRef
        val start = et?.selectionStart ?: rawInputText.length
        val end = et?.selectionEnd ?: rawInputText.length

        if (start != end) {
            val newText = rawInputText.substring(0, start) + rawInputText.substring(end)
            applyTextWithCursor(newText, start)
        } else if (start > 0) {
            val isAns = rawInputText.substring(0, start).endsWith("ANS")
            val isMod = rawInputText.substring(0, start).endsWith("MOD")
            val isDoubleFact = rawInputText.substring(0, start).endsWith("!!")
            val deleteLen = when {
                isAns || isMod -> 3
                isDoubleFact -> 2
                else -> 1
            }
            val newStart = (start - deleteLen).coerceAtLeast(0)
            val newText = rawInputText.substring(0, newStart) + rawInputText.substring(start)
            applyTextWithCursor(newText, newStart)
        }
    }

    val onButtonPress: (String) -> Unit = onButtonPress@ { key ->
        playKeyClickSound(context, isKeySoundEnabled)

        if (hapticState.value) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }

        when (key) {
            "AC", "C" -> {
                applyTextWithCursor("", 0)
                resultPreview = ""
                return@onButtonPress
            }
            "⌫", "DEL" -> {
                deleteAtCursor()
                return@onButtonPress
            }
            "=" -> {
                if (rawInputText.isNotEmpty()) {
                    val evaluated = engine.evaluate(rawInputText)
                    if (!evaluated.contains("Invalid", ignoreCase = true) &&
                        !evaluated.contains("Error", ignoreCase = true) &&
                        !evaluated.contains("syntax", ignoreCase = true) &&
                        !evaluated.contains("Cannot", ignoreCase = true) &&
                        !evaluated.contains("Domain", ignoreCase = true) &&
                        !evaluated.contains("Negative", ignoreCase = true) &&
                        !evaluated.contains("Overflow", ignoreCase = true)
                    ) {
                        engine.lastAnswer = evaluated
                        CalculatorHistoryManager.addRecordWithDeduplication(
                            context = context,
                            currentList = historyList,
                            expression = rawInputText,
                            result = evaluated
                        )
                        applyTextWithCursor(evaluated, evaluated.length)
                        resultPreview = ""
                    } else {
                        resultPreview = evaluated
                    }
                }
                return@onButtonPress
            }
            "MC" -> {
                engine.memoryClear()
                Toast.makeText(context, "Memory Cleared", Toast.LENGTH_SHORT).show()
                return@onButtonPress
            }
            "MR" -> {
                val mem = engine.memoryValue.stripTrailingZeros().toPlainString()
                insertTextAtCursor(mem)
                return@onButtonPress
            }
            "M+" -> {
                val target = if (resultPreview.isNotEmpty()) resultPreview else rawInputText
                if (target.isNotBlank()) {
                    engine.memoryAdd(target)
                    Toast.makeText(context, "M+ Added", Toast.LENGTH_SHORT).show()
                }
                return@onButtonPress
            }
            "M-" -> {
                val target = if (resultPreview.isNotEmpty()) resultPreview else rawInputText
                if (target.isNotBlank()) {
                    engine.memorySubtract(target)
                    Toast.makeText(context, "M- Subtracted", Toast.LENGTH_SHORT).show()
                }
                return@onButtonPress
            }
            "ANS" -> insertTextAtCursor("ANS")
            "±" -> {
                if (rawInputText.startsWith("-")) {
                    applyTextWithCursor(rawInputText.removePrefix("-"), (editTextRef?.selectionStart ?: 1) - 1)
                } else {
                    applyTextWithCursor("-$rawInputText", (editTextRef?.selectionStart ?: 0) + 1)
                }
                return@onButtonPress
            }
            "rad" -> {
                angleMode = AngleMode.RAD
                engine.angleMode = AngleMode.RAD
                updatePreview(rawInputText)
                return@onButtonPress
            }
            "deg" -> {
                angleMode = AngleMode.DEG
                engine.angleMode = AngleMode.DEG
                updatePreview(rawInputText)
                return@onButtonPress
            }
            "TRIG" -> {
                isReciprocalTrigActive = !isReciprocalTrigActive
                return@onButtonPress
            }
            "sin" -> insertTextAtCursor("sin(")
            "cos" -> insertTextAtCursor("cos(")
            "tan" -> insertTextAtCursor("tan(")
            "sin⁻¹" -> insertTextAtCursor("asin(")
            "cos⁻¹" -> insertTextAtCursor("acos(")
            "tan⁻¹" -> insertTextAtCursor("atan(")
            "csc" -> insertTextAtCursor("csc(")
            "sec" -> insertTextAtCursor("sec(")
            "cot" -> insertTextAtCursor("cot(")
            "csc⁻¹" -> insertTextAtCursor("acsc(")
            "sec⁻¹" -> insertTextAtCursor("asec(")
            "cot⁻¹" -> insertTextAtCursor("acot(")
            "log" -> insertTextAtCursor("log(")
            "ln" -> insertTextAtCursor("ln(")
            "10^" -> insertTextAtCursor("10^")
            "eˣ" -> insertTextAtCursor("e^")
            "√" -> insertTextAtCursor("sqrt(")
            "x²" -> insertTextAtCursor("^2")
            "!" -> insertTextAtCursor("!")
            "!!" -> insertTextAtCursor("!!")
            "xʸ", "^" -> insertTextAtCursor("^")
            "MOD" -> insertTextAtCursor(" MOD ")
            else -> insertTextAtCursor(key)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(selectedTheme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. Sleek Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = selectedTheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, selectedTheme.accent.copy(alpha = 0.5f)),
                modifier = Modifier.clickable(onClick = onOpenTools)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Tools Hub",
                        tint = selectedTheme.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Tools",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = selectedTheme.accent
                    )
                }
            }

            // Standardized Brand Logo Header (Tightened Spacing)
            AppBrandLogoText(
                fontSize = 17.sp,
                letterSpacing = 1.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isScientific) selectedTheme.accent.copy(alpha = 0.15f) else Color.Transparent,
                    border = if (isScientific) androidx.compose.foundation.BorderStroke(1.dp, selectedTheme.accent) else null,
                    modifier = Modifier.clickable {
                        isScientific = !isScientific
                        editTextRef?.let { et ->
                            et.textSize = calculateDynamicTextSize(et.text?.length ?: 0, isScientific)
                        }
                    }
                ) {
                    Text(
                        text = "ƒ(x)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isScientific) selectedTheme.accent else selectedTheme.textSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }

                IconButton(onClick = { showHistory = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = selectedTheme.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = selectedTheme.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(selectedTheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Customize Layout & Theme", color = selectedTheme.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Palette, contentDescription = null, tint = selectedTheme.accent) },
                            onClick = {
                                showMenu = false
                                showCustomization = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("About Toolator", color = selectedTheme.textPrimary) },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = selectedTheme.accent) },
                            onClick = {
                                showMenu = false
                                view.post {
                                    onOpenAbout()
                                }
                            }
                        )
                        HorizontalDivider(color = selectedTheme.funcBtn.copy(alpha = 0.5f))
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Key Sound", color = selectedTheme.textPrimary)
                                    Switch(
                                        checked = isKeySoundEnabled,
                                        onCheckedChange = { isKeySoundEnabled = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = selectedTheme.accent,
                                            checkedTrackColor = selectedTheme.accent.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            },
                            onClick = { isKeySoundEnabled = !isKeySoundEnabled }
                        )
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Key Vibration", color = selectedTheme.textPrimary)
                                    Switch(
                                        checked = hapticState.value,
                                        onCheckedChange = { hapticState.value = it },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = selectedTheme.accent,
                                            checkedTrackColor = selectedTheme.accent.copy(alpha = 0.4f)
                                        )
                                    )
                                }
                            },
                            onClick = { hapticState.value = !hapticState.value }
                        )
                    }
                }
            }
        }

        // 2. Display Viewport Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = selectedTheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, selectedTheme.funcBtn.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedTheme.numBtn)
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "NORM",
                            fontSize = 11.sp,
                            fontWeight = if (!isInverse) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isInverse) selectedTheme.accent else selectedTheme.textMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (!isInverse) selectedTheme.funcBtn else Color.Transparent)
                                .clickable { isInverse = false }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                        Text(
                            text = "INV",
                            fontSize = 11.sp,
                            fontWeight = if (isInverse) FontWeight.Bold else FontWeight.Medium,
                            color = if (isInverse) selectedTheme.accent else selectedTheme.textMuted,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isInverse) selectedTheme.funcBtn else Color.Transparent)
                                .clickable { isInverse = true }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val txt = rawInputText.ifEmpty { editTextRef?.text?.toString() ?: "" }
                            copyToClipboard(if (resultPreview.isNotEmpty()) resultPreview else txt)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = selectedTheme.textSecondary.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    AndroidView(
                        factory = { ctx ->
                            HorizontalScrollView(ctx).apply {
                                isFillViewport = true
                                isHorizontalScrollBarEnabled = false
                                horizontalScrollRef = this

                                val et = EditText(ctx).apply {
                                    background = null
                                    showSoftInputOnFocus = false
                                    isFocusable = true
                                    isFocusableInTouchMode = true
                                    isCursorVisible = true
                                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                    textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                                    hint = "0"
                                    setHintTextColor(selectedTheme.textSecondary.copy(alpha = 0.4f).toArgb())
                                    setTextColor(selectedTheme.textPrimary.toArgb())
                                    textSize = calculateDynamicTextSize(0, isScientific)
                                    isSingleLine = true
                                    setHorizontallyScrolling(true)
                                    setPadding(0, 0, 0, 0)
                                }
                                editTextRef = et
                                addView(et)
                            }
                        },
                        update = { _ ->
                            editTextRef?.setTextColor(selectedTheme.textPrimary.toArgb())
                            editTextRef?.setHintTextColor(selectedTheme.textSecondary.copy(alpha = 0.4f).toArgb())
                            editTextRef?.let { et ->
                                et.textSize = calculateDynamicTextSize(et.text?.length ?: 0, isScientific)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    AnimatedVisibility(
                        visible = resultPreview.isNotEmpty() && resultPreview != rawInputText,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        val previewScrollState = rememberScrollState()
                        val formattedPreview = formatMathExpression(resultPreview)

                        LaunchedEffect(formattedPreview) {
                            previewScrollState.scrollTo(previewScrollState.maxValue)
                        }

                        val previewFontSize = when {
                            formattedPreview.length <= 10 -> if (isScientific) 20.sp else 24.sp
                            formattedPreview.length <= 16 -> if (isScientific) 16.sp else 19.sp
                            else -> 14.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .horizontalScroll(previewScrollState),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Text(
                                text = "= $formattedPreview",
                                fontSize = previewFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = selectedTheme.accent,
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.clickable { copyToClipboard(resultPreview) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Memory Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("MC", "MR", "M+", "M-").forEach { mKey ->
                Text(
                    text = mKey,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = selectedTheme.textSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onButtonPress(mKey) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "ANS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = selectedTheme.accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onButtonPress("ANS") }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // 4. Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (isScientific) 8.dp else 10.dp)
        ) {
            if (isScientific) {
                val t1 = when {
                    isReciprocalTrigActive && isInverse -> "csc⁻¹"
                    isReciprocalTrigActive -> "csc"
                    isInverse -> "sin⁻¹"
                    else -> "sin"
                }
                val t2 = when {
                    isReciprocalTrigActive && isInverse -> "sec⁻¹"
                    isReciprocalTrigActive -> "sec"
                    isInverse -> "cos⁻¹"
                    else -> "cos"
                }
                val t3 = when {
                    isReciprocalTrigActive && isInverse -> "cot⁻¹"
                    isReciprocalTrigActive -> "cot"
                    isInverse -> "tan⁻¹"
                    else -> "tan"
                }

                val r1 = listOf(t1, t2, t3, "rad", "deg")
                val r2 = if (isInverse) listOf("10^", "eˣ", "(", ")", "TRIG") else listOf("log", "ln", "(", ")", "TRIG")
                val r3First = if (isInverse) "!!" else "!"
                val r5First = if (isInverse) "x²" else "√"

                val sciRows = listOf(
                    r1,
                    r2,
                    listOf(r3First, "AC", "%", "⌫", "÷"),
                    listOf("xʸ", getNum(selectedLayout, 0), getNum(selectedLayout, 1), getNum(selectedLayout, 2), "×"),
                    listOf(r5First, getNum(selectedLayout, 3), getNum(selectedLayout, 4), getNum(selectedLayout, 5), "−"),
                    listOf("π", getNum(selectedLayout, 6), getNum(selectedLayout, 7), getNum(selectedLayout, 8), "+"),
                    listOf("e", "MOD", "0", ".", "=")
                )

                sciRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { key ->
                            val isAccent = key == "="
                            val isDegActive = key == "deg" && angleMode == AngleMode.DEG
                            val isRadActive = key == "rad" && angleMode == AngleMode.RAD
                            val isTrigActive = key == "TRIG" && isReciprocalTrigActive
                            val isNum = key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", ".")

                            val bg = when {
                                isAccent -> selectedTheme.accent
                                isNum -> selectedTheme.numBtn
                                else -> selectedTheme.funcBtn
                            }

                            val textCol = when {
                                isAccent -> if (selectedTheme.isLight) Color.White else Color.Black
                                key == "AC" -> selectedTheme.accent
                                isDegActive || isRadActive || isTrigActive -> selectedTheme.accent
                                isNum -> selectedTheme.textPrimary
                                else -> selectedTheme.textPrimary
                            }

                            ThemeCalcKey(
                                text = key,
                                modifier = Modifier.weight(1f),
                                containerColor = bg,
                                contentColor = textCol,
                                isEqualsKey = isAccent,
                                fontSize = if (key.length > 3) 11.sp else if (key.length > 2) 13.sp else 17.sp,
                                cornerPercent = 30,
                                onClick = { onButtonPress(key) }
                            )
                        }
                    }
                }
            } else {
                val standardRows = getStandardGrid(selectedLayout)

                standardRows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { key ->
                            val isAccent = key == "="
                            val isNum = key in listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "00", ".")

                            val bg = when {
                                isAccent -> selectedTheme.accent
                                isNum -> selectedTheme.numBtn
                                else -> selectedTheme.funcBtn
                            }

                            val textCol = when {
                                isAccent -> if (selectedTheme.isLight) Color.White else Color.Black
                                key == "AC" -> selectedTheme.accent
                                isNum -> selectedTheme.textPrimary
                                else -> selectedTheme.textPrimary
                            }

                            ThemeCalcKey(
                                text = key,
                                modifier = Modifier.weight(1f),
                                containerColor = bg,
                                contentColor = textCol,
                                isEqualsKey = isAccent,
                                fontSize = if (key.length > 1) 22.sp else 26.sp,
                                cornerPercent = 32,
                                onClick = { onButtonPress(key) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCustomization) {
        CustomizationSheet(
            currentLayout = selectedLayout,
            currentTheme = selectedTheme,
            onLayoutChange = { selectedLayout = it },
            onThemeChange = { newTheme: AppTheme ->
                themeState.value = newTheme
            },
            onResetDefault = {
                selectedLayout = KeypadLayout.STANDARD
                themeState.value = AppThemePreset.CYBER_AMBER
            },
            onDismiss = { showCustomization = false }
        )
    }

    if (showHistory) {
        ModalBottomSheet(
            onDismissRequest = { showHistory = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = selectedTheme.bg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("History", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = selectedTheme.textPrimary)
                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearHistoryConfirmDialog = true }
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = selectedTheme.accent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = historySearchQuery,
                    onValueChange = { historySearchQuery = it },
                    textStyle = TextStyle(
                        color = selectedTheme.textPrimary,
                        fontSize = 14.sp
                    ),
                    placeholder = { Text("Search expression, result or note...", fontSize = 13.sp, color = selectedTheme.textSecondary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = selectedTheme.textSecondary) },
                    trailingIcon = {
                        if (historySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { historySearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = selectedTheme.textSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = selectedTheme.textPrimary,
                        unfocusedTextColor = selectedTheme.textPrimary,
                        focusedContainerColor = selectedTheme.numBtn,
                        unfocusedContainerColor = selectedTheme.numBtn,
                        focusedBorderColor = selectedTheme.accent,
                        unfocusedBorderColor = selectedTheme.funcBtn,
                        cursorColor = selectedTheme.accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                val filteredHistory = historyList.filter {
                    it.expression.contains(historySearchQuery, ignoreCase = true) ||
                            it.result.contains(historySearchQuery, ignoreCase = true) ||
                            it.tag.contains(historySearchQuery, ignoreCase = true)
                }

                if (filteredHistory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (historySearchQuery.isEmpty()) "No history recorded" else "No matching calculations", color = selectedTheme.textSecondary)
                    }
                } else {
                    val sdf = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredHistory, key = { it.id }) { item ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        applyTextWithCursor(item.result, item.result.length)
                                        resultPreview = ""
                                        showHistory = false
                                    },
                                shape = RoundedCornerShape(14.dp),
                                color = selectedTheme.numBtn,
                                border = androidx.compose.foundation.BorderStroke(1.dp, selectedTheme.funcBtn.copy(alpha = 0.4f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(formatMathExpression(item.expression), fontSize = 14.sp, color = selectedTheme.textSecondary)
                                        Text(sdf.format(Date(item.timestamp)), fontSize = 10.sp, color = selectedTheme.textSecondary.copy(alpha = 0.6f))
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("= ${formatMathExpression(item.result)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = selectedTheme.accent)

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    tagDialogItem = item
                                                    tagInputText = item.tag
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    if (item.tag.isEmpty()) Icons.Default.Edit else Icons.Default.Label,
                                                    contentDescription = "Add Note",
                                                    tint = if (item.tag.isEmpty()) selectedTheme.textSecondary else selectedTheme.accent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }

                                            IconButton(
                                                onClick = { copyToClipboard("${item.expression} = ${item.result}") },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = selectedTheme.textSecondary, modifier = Modifier.size(16.dp))
                                            }

                                            IconButton(
                                                onClick = {
                                                    historyList.remove(item)
                                                    CalculatorHistoryManager.saveHistory(context, historyList)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = selectedTheme.textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    if (item.tag.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = selectedTheme.funcBtn
                                        ) {
                                            Text(
                                                text = "📌 ${item.tag}",
                                                fontSize = 11.sp,
                                                color = selectedTheme.accent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
    }

    // Clear History Confirmation Dialog
    if (showClearHistoryConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryConfirmDialog = false },
            title = {
                Text(
                    text = "Clear calculation history?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = selectedTheme.textPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all ${historyList.size} saved calculations.",
                    fontSize = 14.sp,
                    color = selectedTheme.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyList.clear()
                        CalculatorHistoryManager.clearHistory(context)
                        showClearHistoryConfirmDialog = false
                    }
                ) {
                    Text("Clear", color = selectedTheme.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryConfirmDialog = false }) {
                    Text("Cancel", color = selectedTheme.textSecondary)
                }
            },
            containerColor = selectedTheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (tagDialogItem != null) {
        AlertDialog(
            onDismissRequest = { tagDialogItem = null },
            title = { Text("Add Label / Note", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = selectedTheme.textPrimary) },
            text = {
                OutlinedTextField(
                    value = tagInputText,
                    onValueChange = { tagInputText = it },
                    textStyle = TextStyle(
                        color = selectedTheme.textPrimary,
                        fontSize = 14.sp
                    ),
                    placeholder = { Text("e.g. Physics Formula, Grocery...", fontSize = 13.sp, color = selectedTheme.textSecondary.copy(alpha = 0.6f)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = selectedTheme.textPrimary,
                        unfocusedTextColor = selectedTheme.textPrimary,
                        focusedContainerColor = selectedTheme.numBtn,
                        unfocusedContainerColor = selectedTheme.numBtn,
                        focusedBorderColor = selectedTheme.accent,
                        unfocusedBorderColor = selectedTheme.funcBtn,
                        cursorColor = selectedTheme.accent
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val idx = historyList.indexOfFirst { it.id == tagDialogItem?.id }
                    if (idx != -1) {
                        historyList[idx] = historyList[idx].copy(tag = tagInputText)
                        CalculatorHistoryManager.saveHistory(context, historyList)
                    }
                    tagDialogItem = null
                }) {
                    Text("Save", color = selectedTheme.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { tagDialogItem = null }) {
                    Text("Cancel", color = selectedTheme.textSecondary)
                }
            },
            containerColor = selectedTheme.surface
        )
    }
}

fun getNum(layout: KeypadLayout, index: Int): String {
    return when (layout) {
        KeypadLayout.STANDARD, KeypadLayout.CLASSIC, KeypadLayout.COMPACT -> listOf("7", "8", "9", "4", "5", "6", "1", "2", "3")[index]
        KeypadLayout.ASCENDING -> listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")[index]
        KeypadLayout.REVERSE -> listOf("9", "8", "7", "6", "5", "4", "3", "2", "1")[index]
    }
}

fun getStandardGrid(layout: KeypadLayout): List<List<String>> {
    return when (layout) {
        KeypadLayout.STANDARD -> listOf(
            listOf("AC", "%", "⌫", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("00", "0", ".", "=")
        )
        KeypadLayout.ASCENDING -> listOf(
            listOf("AC", "%", "⌫", "÷"),
            listOf("1", "2", "3", "×"),
            listOf("4", "5", "6", "−"),
            listOf("7", "8", "9", "+"),
            listOf("00", "0", ".", "=")
        )
        KeypadLayout.REVERSE -> listOf(
            listOf("AC", "%", "⌫", "÷"),
            listOf("9", "8", "7", "×"),
            listOf("6", "5", "4", "−"),
            listOf("3", "2", "1", "+"),
            listOf("00", "0", ".", "=")
        )
        KeypadLayout.CLASSIC -> listOf(
            listOf("÷", "AC", "%", "⌫"),
            listOf("×", "7", "8", "9"),
            listOf("−", "4", "5", "6"),
            listOf("+", "1", "2", "3"),
            listOf("=", "00", "0", ".")
        )
        KeypadLayout.COMPACT -> listOf(
            listOf("AC", "(", ")", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )
    }
}

@Composable
fun ThemeCalcKey(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color,
    contentColor: Color,
    isEqualsKey: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    cornerPercent: Int = 32,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current.value

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(percent = cornerPercent))
            .then(
                if (isEqualsKey) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                theme.accent,
                                theme.accent.copy(alpha = 0.85f)
                            )
                        )
                    )
                } else {
                    Modifier.background(containerColor)
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = contentColor.copy(alpha = 0.2f)),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (text == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "Delete",
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = if (isEqualsKey) FontWeight.Bold else FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}