package com.universal.calulator

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
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
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JvmTextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val context = LocalContext.current

    val today = remember { LocalDate.now() }
    val installDate = remember { HabitTrackerManager.getInstallDate(context) }
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }

    var habitRecords by remember {
        mutableStateOf(HabitTrackerManager.loadRecords(context))
    }

    // Modal Sheet States
    var selectedDateForDetail by remember { mutableStateOf<LocalDate?>(null) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingStatus by remember { mutableStateOf(HabitStatus.DONE) }
    var editingNote by remember { mutableStateOf("") }

    // Year-Month Picker Dialog State
    var showYearMonthPicker by remember { mutableStateOf(false) }
    var pickerSelectedYear by remember { mutableIntStateOf(currentYearMonth.year) }

    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }
    val displayMonthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH) }
    val displayDateHeader = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH) }

    // Premium Solid Colors
    val greenColor = Color(0xFF2ECC71)
    val yellowColor = Color(0xFFF39C12)
    val redColor = Color(0xFFE74C3C)
    val noDataBg = theme.numBtn.copy(alpha = 0.5f)
    val pendingBorderColor = theme.accent

    // Monthly Statistics Calculation
    val daysInMonth = currentYearMonth.lengthOfMonth()
    var doneCount = 0
    var halfDoneCount = 0
    var missedCount = 0
    var totalTrackedDays = 0

    for (day in 1..daysInMonth) {
        val date = currentYearMonth.atDay(day)
        val key = date.format(dateFormatter)

        if (date.isAfter(today)) continue

        val record = habitRecords[key]
        if (record != null) {
            when (record.status) {
                HabitStatus.DONE -> { doneCount++; totalTrackedDays++ }
                HabitStatus.HALF_DONE -> { halfDoneCount++; totalTrackedDays++ }
                HabitStatus.MISSED -> { missedCount++; totalTrackedDays++ }
                HabitStatus.NO_DATA -> { /* Not counted */ }
                HabitStatus.PENDING -> {
                    if (date.isBefore(today) && !date.isBefore(installDate)) {
                        missedCount++
                        totalTrackedDays++
                    }
                }
            }
        } else {
            if (date.isBefore(today) && !date.isBefore(installDate)) {
                missedCount++
                totalTrackedDays++
            }
        }
    }

    val score = if (totalTrackedDays > 0) {
        ((doneCount * 1.0 + halfDoneCount * 0.5) / totalTrackedDays) * 100.0
    } else 0.0

    // Year-Month Picker Dialog
    if (showYearMonthPicker) {
        AlertDialog(
            onDismissRequest = { showYearMonthPicker = false },
            containerColor = theme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { pickerSelectedYear-- }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Year", tint = theme.accent)
                    }
                    Text(
                        text = "$pickerSelectedYear",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    IconButton(
                        onClick = { if (pickerSelectedYear < today.year) pickerSelectedYear++ },
                        enabled = pickerSelectedYear < today.year
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Next Year",
                            tint = if (pickerSelectedYear < today.year) theme.accent else theme.textSecondary.copy(alpha = 0.3f)
                        )
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "SELECT MONTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(Month.values()) { month ->
                            val isCurrentSelection = currentYearMonth.year == pickerSelectedYear && currentYearMonth.month == month
                            val isFutureMonth = pickerSelectedYear == today.year && month.value > today.monthValue

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCurrentSelection) theme.accent else theme.numBtn,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = !isFutureMonth) {
                                        currentYearMonth = YearMonth.of(pickerSelectedYear, month)
                                        showYearMonthPicker = false
                                    }
                            ) {
                                Text(
                                    text = month.getDisplayName(JvmTextStyle.SHORT, Locale.ENGLISH).uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrentSelection) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isFutureMonth -> theme.textSecondary.copy(alpha = 0.3f)
                                        isCurrentSelection -> (if (theme.isLight) Color.White else Color.Black)
                                        else -> theme.textPrimary
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showYearMonthPicker = false }) {
                    Text("Close", color = theme.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Header
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
                text = "Habit Calendar Tracker",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month Navigation Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = theme.textPrimary)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = theme.numBtn,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                pickerSelectedYear = currentYearMonth.year
                                showYearMonthPicker = true
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = currentYearMonth.format(displayMonthFormatter).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent,
                                letterSpacing = 0.5.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pick Year",
                                tint = theme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = theme.textPrimary)
                    }
                }
            }

            // Calendar Grid Card
            Surface(
                shape = RoundedCornerShape(18.dp),
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
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { dayName ->
                            Text(
                                text = dayName,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary.copy(alpha = 0.7f),
                                modifier = Modifier.width(38.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    val firstOfMonth = currentYearMonth.atDay(1)
                    val firstDayOfWeek = firstOfMonth.dayOfWeek.value
                    val totalSlots = ((firstDayOfWeek - 1) + daysInMonth + 6) / 7 * 7

                    val rows = totalSlots / 7
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (c in 0..6) {
                                val slotIndex = r * 7 + c
                                val dayNum = slotIndex - (firstDayOfWeek - 1) + 1

                                if (dayNum in 1..daysInMonth) {
                                    val date = currentYearMonth.atDay(dayNum)
                                    val key = date.format(dateFormatter)
                                    val isToday = date.isEqual(today)
                                    val isFuture = date.isAfter(today)
                                    val isBeforeInstall = date.isBefore(installDate)

                                    val record = habitRecords[key]

                                    // Determine Circle Style & Color
                                    val (bgColor, textColor) = when {
                                        isFuture -> Pair(Color.Transparent, theme.textSecondary.copy(alpha = 0.3f))
                                        record?.status == HabitStatus.DONE -> Pair(greenColor, Color.White)
                                        record?.status == HabitStatus.HALF_DONE -> Pair(yellowColor, Color.Black)
                                        record?.status == HabitStatus.MISSED -> Pair(redColor, Color.White)
                                        record?.status == HabitStatus.NO_DATA -> Pair(noDataBg, theme.textSecondary)
                                        isToday -> Pair(theme.accent.copy(alpha = 0.2f), theme.accent)
                                        isBeforeInstall -> Pair(noDataBg, theme.textSecondary)
                                        else -> Pair(redColor, Color.White)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(bgColor)
                                            .then(
                                                if (isToday) Modifier.border(1.5.dp, pendingBorderColor, CircleShape)
                                                else Modifier
                                            )
                                            .clickable(enabled = !isFuture) {
                                                selectedDateForDetail = date

                                                val hasRecordedExplicitly = record != null && record.status != HabitStatus.NO_DATA && record.status != HabitStatus.PENDING

                                                isEditMode = !hasRecordedExplicitly
                                                editingStatus = record?.status ?: when {
                                                    isToday -> HabitStatus.DONE
                                                    isBeforeInstall -> HabitStatus.DONE
                                                    else -> HabitStatus.MISSED
                                                }
                                                editingNote = record?.note ?: ""
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$dayNum",
                                            fontSize = 13.sp,
                                            fontWeight = if (isToday || record != null) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }

                    // Legend
                    HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendCircleBadge(color = greenColor, label = "Done")
                        LegendCircleBadge(color = yellowColor, label = "Half Done")
                        LegendCircleBadge(color = redColor, label = "Missed")
                        LegendCircleBadge(color = noDataBg, label = "No Data")
                    }
                }
            }

            // Monthly Statistics Card
            Surface(
                shape = RoundedCornerShape(18.dp),
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
                        Text(
                            text = "MONTHLY STATISTICS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${String.format(Locale.ENGLISH, "%.1f", score)}% Score",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.accent
                        )
                    }

                    LinearProgressIndicator(
                        progress = { (score / 100f).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = theme.accent,
                        trackColor = theme.numBtn
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCounterItem(count = doneCount, label = "Done", color = greenColor)
                        StatCounterItem(count = halfDoneCount, label = "Half Done", color = yellowColor)
                        StatCounterItem(count = missedCount, label = "Missed", color = redColor)
                        StatCounterItem(count = totalTrackedDays, label = "Tracked", color = theme.textPrimary)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }

    // Dynamic Safe View vs Direct Input Bottom Sheet
    selectedDateForDetail?.let { targetDate ->
        val dateKey = targetDate.format(dateFormatter)
        val existingRecord = habitRecords[dateKey]
        val isBeforeInstall = targetDate.isBefore(installDate)
        val hasRecordedExplicitly = existingRecord != null && existingRecord.status != HabitStatus.NO_DATA && existingRecord.status != HabitStatus.PENDING

        val activeStatus = existingRecord?.status ?: when {
            targetDate.isEqual(today) -> HabitStatus.PENDING
            isBeforeInstall -> HabitStatus.NO_DATA
            else -> HabitStatus.MISSED
        }

        val activeColor = when (activeStatus) {
            HabitStatus.DONE -> greenColor
            HabitStatus.HALF_DONE -> yellowColor
            HabitStatus.MISSED -> redColor
            HabitStatus.NO_DATA -> theme.textSecondary
            HabitStatus.PENDING -> theme.accent
        }

        ModalBottomSheet(
            onDismissRequest = { selectedDateForDetail = null },
            containerColor = theme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = targetDate.format(displayDateHeader),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = if (targetDate.isEqual(today)) "Today's Log" else if (isBeforeInstall) "Past Date (Before Install)" else "Past Record",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.accent
                        )
                    }

                    if (!isEditMode && hasRecordedExplicitly) {
                        OutlinedButton(
                            onClick = { isEditMode = true },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = theme.accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Edit Log", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                        }
                    }
                }

                if (!isEditMode) {
                    // Protected View (Recorded Items Only)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = theme.numBtn,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)),
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
                                Text("Recorded Status", fontSize = 12.sp, color = theme.textSecondary)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = activeColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = when (activeStatus) {
                                            HabitStatus.DONE -> "● DONE"
                                            HabitStatus.HALF_DONE -> "● HALF DONE"
                                            HabitStatus.MISSED -> "● MISSED"
                                            HabitStatus.NO_DATA -> "● NO DATA"
                                            HabitStatus.PENDING -> "● PENDING"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.3f))

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Reason / Note", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                                Text(
                                    text = if (!existingRecord?.note.isNullOrBlank()) existingRecord!!.note else "No note added for this date.",
                                    fontSize = 13.sp,
                                    color = if (!existingRecord?.note.isNullOrBlank()) theme.textPrimary else theme.textSecondary.copy(alpha = 0.6f),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = theme.textSecondary.copy(alpha = 0.6f), modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Protected. Click 'Edit Log' above to change.", fontSize = 11.sp, color = theme.textSecondary.copy(alpha = 0.7f))
                    }
                } else {
                    // Direct Input Mode (First Time & Edit)
                    Text(
                        text = if (hasRecordedExplicitly) "CHANGE STATUS" else "SET STATUS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusSelectButton(
                            label = "Done",
                            color = greenColor,
                            isSelected = editingStatus == HabitStatus.DONE,
                            modifier = Modifier.weight(1f),
                            onClick = { editingStatus = HabitStatus.DONE }
                        )
                        StatusSelectButton(
                            label = "Half Done",
                            color = yellowColor,
                            isSelected = editingStatus == HabitStatus.HALF_DONE,
                            modifier = Modifier.weight(1f),
                            onClick = { editingStatus = HabitStatus.HALF_DONE }
                        )
                        StatusSelectButton(
                            label = "Missed",
                            color = redColor,
                            isSelected = editingStatus == HabitStatus.MISSED,
                            modifier = Modifier.weight(1f),
                            onClick = { editingStatus = HabitStatus.MISSED }
                        )
                    }

                    OutlinedTextField(
                        value = editingNote,
                        onValueChange = { editingNote = it },
                        label = { Text("Reason / Note (Optional)", fontSize = 12.sp, color = theme.textSecondary) },
                        placeholder = { Text("e.g. Completed today's workout.", fontSize = 12.sp, color = theme.textSecondary.copy(alpha = 0.5f)) },
                        leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null, tint = theme.accent) },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(
                            color = theme.textPrimary,
                            fontSize = 14.sp
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (hasRecordedExplicitly) {
                                    isEditMode = false
                                } else {
                                    selectedDateForDetail = null
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn)
                        ) {
                            Text("Cancel", color = theme.textSecondary)
                        }

                        Button(
                            onClick = {
                                val record = HabitRecord(
                                    id = existingRecord?.id ?: UUIDGenerator.get(),
                                    dateStr = dateKey,
                                    status = editingStatus,
                                    note = editingNote.trim(),
                                    createdAt = existingRecord?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                HabitTrackerManager.saveRecord(context, record)
                                habitRecords = HabitTrackerManager.loadRecords(context)
                                isEditMode = false
                                selectedDateForDetail = null
                                Toast.makeText(context, "Log updated successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = theme.accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Save Record",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (theme.isLight) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
            }
        }
    }
}

@Composable
fun StatusSelectButton(
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current.value

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) color.copy(alpha = 0.25f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else color.copy(alpha = 0.4f)
        ),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else theme.textPrimary
            )
        }
    }
}

@Composable
fun StatCounterItem(count: Int, label: String, color: Color) {
    val theme = LocalAppTheme.current.value
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "$count", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 10.sp, color = theme.textSecondary)
    }
}

@Composable
fun LegendCircleBadge(color: Color, label: String) {
    val theme = LocalAppTheme.current.value
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = theme.textSecondary)
    }
}