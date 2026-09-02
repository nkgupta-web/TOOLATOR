package com.universal.calulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeCalculatorScreen(
    toolId: String,
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val isAgeTool = toolId.lowercase().trim() in listOf("age", "tool_age")

    val displayDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    // Age Calculator States
    var birthDate by remember { mutableStateOf(LocalDate.of(2007, 8, 23)) }
    var targetDate by remember { mutableStateOf(LocalDate.now()) }

    // Date Difference States
    var dateDiffTab by remember { mutableStateOf("DateDiff") }
    var fromDate by remember { mutableStateOf(LocalDate.now().minusMonths(3)) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }

    // Age Difference States
    var person1Dob by remember { mutableStateOf(LocalDate.of(2000, 1, 15)) }
    var person2Dob by remember { mutableStateOf(LocalDate.of(2003, 5, 20)) }

    // Date Picker Dialog State
    var showDatePicker by remember { mutableStateOf(false) }
    var activeDateType by remember { mutableStateOf("dob") }

    fun formatNumber(n: Long): String = DecimalFormat("#,##,###").format(n)

    fun getZodiacSign(date: LocalDate): String {
        val d = date.dayOfMonth
        return when (date.monthValue) {
            1 -> if (d < 20) "Capricorn ♑" else "Aquarius ♒"
            2 -> if (d < 19) "Aquarius ♒" else "Pisces ♓"
            3 -> if (d < 21) "Pisces ♓" else "Aries ♈"
            4 -> if (d < 20) "Aries ♈" else "Taurus ♉"
            5 -> if (d < 21) "Taurus ♉" else "Gemini ♊"
            6 -> if (d < 21) "Gemini ♊" else "Cancer ♋"
            7 -> if (d < 23) "Cancer ♋" else "Leo ♌"
            8 -> if (d < 23) "Leo ♌" else "Virgo ♍"
            9 -> if (d < 23) "Virgo ♍" else "Libra ♎"
            10 -> if (d < 23) "Libra ♎" else "Scorpio ♏"
            11 -> if (d < 22) "Scorpio ♏" else "Sagittarius ♐"
            12 -> if (d < 22) "Sagittarius ♐" else "Capricorn ♑"
            else -> ""
        }
    }

    if (showDatePicker) {
        val initialDate = when (activeDateType) {
            "dob" -> birthDate
            "target" -> targetDate
            "from" -> fromDate
            "to" -> toDate
            "p1" -> person1Dob
            "p2" -> person2Dob
            else -> LocalDate.now()
        }

        val todayMillis = LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        val isDobField = activeDateType in listOf("dob", "p1", "p2")

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return if (isDobField) utcTimeMillis <= todayMillis else true
                }
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selected = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        when (activeDateType) {
                            "dob" -> birthDate = selected
                            "target" -> targetDate = selected
                            "from" -> fromDate = selected
                            "to" -> toDate = selected
                            "p1" -> person1Dob = selected
                            "p2" -> person2Dob = selected
                        }
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = theme.accent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = theme.textSecondary)
                }
            },
            colors = DatePickerDefaults.colors(containerColor = theme.surface)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = theme.surface,
                    titleContentColor = theme.textPrimary,
                    headlineContentColor = theme.textPrimary,
                    weekdayContentColor = theme.textSecondary,
                    yearContentColor = theme.textPrimary,
                    currentYearContentColor = theme.accent,
                    selectedYearContentColor = if (theme.isLight) Color.White else Color.Black,
                    selectedYearContainerColor = theme.accent,
                    dayContentColor = theme.textPrimary,
                    selectedDayContentColor = if (theme.isLight) Color.White else Color.Black,
                    selectedDayContainerColor = theme.accent
                )
            )
        }
    }

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
                text = if (isAgeTool) "Age Calculator" else "Date Difference",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(8.dp))

        if (isAgeTool) {
            // ==================== 1. AGE CALCULATOR ====================
            val startDate = if (birthDate.isAfter(targetDate)) targetDate else birthDate
            val endDate = if (birthDate.isAfter(targetDate)) birthDate else targetDate

            val period = Period.between(startDate, endDate)
            val totalDays = ChronoUnit.DAYS.between(startDate, endDate)
            val totalMonths = ChronoUnit.MONTHS.between(startDate, endDate)
            val totalWeeks = totalDays / 7
            val totalHours = totalDays * 24
            val totalMinutes = totalHours * 60
            val birthDayName = birthDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val zodiac = getZodiacSign(birthDate)

            var nextBirthday = birthDate.withYear(targetDate.year)
            if (nextBirthday.isBefore(targetDate) || nextBirthday.isEqual(targetDate)) {
                nextBirthday = nextBirthday.plusYears(1)
            }
            val daysToNextBirthday = ChronoUnit.DAYS.between(targetDate, nextBirthday)
            val nextAge = Period.between(birthDate, nextBirthday).years
            val nextBdayFormatted = nextBirthday.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            val nextBdayDayName = nextBirthday.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
            val bdayProgress = ((365f - daysToNextBirthday.toFloat()) / 365f).coerceIn(0.05f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Inputs Card
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DatePickerItem(
                            label = "DATE OF BIRTH",
                            dateStr = birthDate.format(displayDateFormatter),
                            onClick = {
                                activeDateType = "dob"
                                showDatePicker = true
                            }
                        )

                        DatePickerItem(
                            label = "CALCULATE AGE ON",
                            dateStr = if (targetDate.isEqual(LocalDate.now())) "Today (${targetDate.format(displayDateFormatter)})" else targetDate.format(displayDateFormatter),
                            onClick = {
                                activeDateType = "target"
                                showDatePicker = true
                            }
                        )
                    }
                }

                // Exact Age Hero Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "YOUR EXACT AGE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = theme.funcBtn
                            ) {
                                Text(
                                    text = zodiac,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.accent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${period.years} Years",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.accent
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "${period.months} Months • ${period.days} Days",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                    }
                }

                // Next Birthday Card with Progress Bar
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cake, contentDescription = null, tint = theme.accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "NEXT BIRTHDAY",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.textSecondary,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "$daysToNextBirthday days left • Turning $nextAge",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = nextBdayFormatted,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textPrimary
                            )
                            Text(
                                text = nextBdayDayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(theme.numBtn)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(bdayProgress)
                                    .fillMaxHeight()
                                    .background(theme.accent)
                            )
                        }
                    }
                }

                // Extended Lifetime Details Card
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
                            text = "LIFETIME BREAKDOWN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary,
                            letterSpacing = 1.sp
                        )

                        DetailRow(label = "Day of Birth", value = birthDayName)
                        DetailRow(label = "Total Months", value = formatNumber(totalMonths))
                        DetailRow(label = "Total Weeks", value = formatNumber(totalWeeks))
                        DetailRow(label = "Total Days", value = formatNumber(totalDays))
                        DetailRow(label = "Total Hours", value = "${formatNumber(totalHours)} hrs")
                        DetailRow(label = "Total Minutes", value = "${formatNumber(totalMinutes)} mins")
                    }
                }

                // Disclaimer Note
                Text(
                    text = "* Zodiac sign is based only on western date of birth and is not a Vedic Janma Rashi.",
                    fontSize = 11.sp,
                    color = theme.textSecondary.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(8.dp))
            }
        } else {
            // ==================== 2. DATE DIFF & AGE DIFF ====================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(theme.surface)
                        .padding(4.dp)
                ) {
                    listOf("Date Difference", "Age Difference").forEach { tab ->
                        val isSelected = (tab == "Date Difference" && dateDiffTab == "DateDiff") ||
                                (tab == "Age Difference" && dateDiffTab == "AgeDiff")
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) theme.accent else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { dateDiffTab = if (tab == "Date Difference") "DateDiff" else "AgeDiff" }
                        ) {
                            Text(
                                text = tab,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                modifier = Modifier.padding(vertical = 9.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (dateDiffTab == "DateDiff") {
                    val sDate = if (fromDate.isAfter(toDate)) toDate else fromDate
                    val eDate = if (fromDate.isAfter(toDate)) fromDate else toDate

                    val period = Period.between(sDate, eDate)
                    val totalDays = ChronoUnit.DAYS.between(sDate, eDate)
                    val totalMonths = ChronoUnit.MONTHS.between(sDate, eDate)
                    val totalWeeks = totalDays / 7
                    val totalHours = totalDays * 24

                    var workDays = 0L
                    var weekendDays = 0L
                    var curr = sDate
                    while (curr.isBefore(eDate)) {
                        if (curr.dayOfWeek == DayOfWeek.SATURDAY || curr.dayOfWeek == DayOfWeek.SUNDAY) {
                            weekendDays++
                        } else {
                            workDays++
                        }
                        curr = curr.plusDays(1)
                    }

                    // Inputs Card with Centered Swap
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
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            DatePickerItem(
                                label = "FROM DATE",
                                dateStr = fromDate.format(displayDateFormatter),
                                onClick = {
                                    activeDateType = "from"
                                    showDatePicker = true
                                }
                            )

                            CenterSwapButton(onClick = {
                                val tmp = fromDate
                                fromDate = toDate
                                toDate = tmp
                            })

                            DatePickerItem(
                                label = "TO DATE",
                                dateStr = toDate.format(displayDateFormatter),
                                onClick = {
                                    activeDateType = "to"
                                    showDatePicker = true
                                }
                            )
                        }
                    }

                    // Total Diff Hero Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "TOTAL DIFFERENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "${period.years} Years",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.accent
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${period.months} Months • ${period.days} Days",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.textPrimary
                            )
                        }
                    }

                    // Complete Details Card
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
                                text = "DETAILED DURATION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )

                            DetailRow(label = "Total Days", value = "${formatNumber(totalDays)} Days")
                            DetailRow(label = "Working Days", value = "${formatNumber(workDays)} Days")
                            DetailRow(label = "Weekend Days", value = "${formatNumber(weekendDays)} Days")
                            DetailRow(label = "Total Weeks", value = "${formatNumber(totalWeeks)} Weeks")
                            DetailRow(label = "Total Months", value = "${formatNumber(totalMonths)} Months")
                            DetailRow(label = "Total Hours", value = "${formatNumber(totalHours)} hrs")
                        }
                    }
                } else {
                    // Age Difference Mode
                    val isP1Older = person1Dob.isBefore(person2Dob)
                    val isSameAge = person1Dob.isEqual(person2Dob)

                    val olderDate = if (isP1Older) person1Dob else person2Dob
                    val youngerDate = if (isP1Older) person2Dob else person1Dob

                    val diffPeriod = Period.between(olderDate, youngerDate)
                    val diffDays = ChronoUnit.DAYS.between(olderDate, youngerDate)
                    val diffMonths = ChronoUnit.MONTHS.between(olderDate, youngerDate)
                    val diffWeeks = diffDays / 7
                    val diffHours = diffDays * 24

                    val p1DayName = person1Dob.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    val p2DayName = person2Dob.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    val p1Zodiac = getZodiacSign(person1Dob)
                    val p2Zodiac = getZodiacSign(person2Dob)

                    val olderPersonTitle = if (isSameAge) "Both are born on the same date" else if (isP1Older) "Person 1 is Older" else "Person 2 is Older"

                    // Inputs Card with Swap
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
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            DatePickerItem(
                                label = "PERSON 1 (DATE OF BIRTH)",
                                dateStr = person1Dob.format(displayDateFormatter),
                                onClick = {
                                    activeDateType = "p1"
                                    showDatePicker = true
                                }
                            )

                            CenterSwapButton(onClick = {
                                val tmp = person1Dob
                                person1Dob = person2Dob
                                person2Dob = tmp
                            })

                            DatePickerItem(
                                label = "PERSON 2 (DATE OF BIRTH)",
                                dateStr = person2Dob.format(displayDateFormatter),
                                onClick = {
                                    activeDateType = "p2"
                                    showDatePicker = true
                                }
                            )
                        }
                    }

                    // Age Diff Hero Card
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
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "AGE DIFFERENCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${diffPeriod.years} Years",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = theme.accent
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "${diffPeriod.months} Months • ${diffPeriod.days} Days",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = theme.textPrimary
                            )

                            Spacer(Modifier.height(10.dp))
                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.5f))
                            Spacer(Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = theme.numBtn,
                                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = olderPersonTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.textPrimary)
                                    Text(text = "by ${formatNumber(diffDays)} days", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = theme.accent)
                                }
                            }
                        }
                    }

                    // Details Card for Age Diff
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
                                text = "DETAILED COMPARISON",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = theme.textSecondary,
                                letterSpacing = 1.sp
                            )

                            DetailRow(label = "Total Days Gap", value = "${formatNumber(diffDays)} Days")
                            DetailRow(label = "Total Weeks Gap", value = "${formatNumber(diffWeeks)} Weeks")
                            DetailRow(label = "Total Months Gap", value = "${formatNumber(diffMonths)} Months")
                            DetailRow(label = "Total Hours Gap", value = "${formatNumber(diffHours)} hrs")
                            DetailRow(label = "Person 1", value = "$p1DayName ($p1Zodiac)")
                            DetailRow(label = "Person 2", value = "$p2DayName ($p2Zodiac)")
                        }
                    }

                    // Disclaimer Note
                    Text(
                        text = "* Zodiac sign is based only on western date of birth and is not a Vedic Janma Rashi.",
                        fontSize = 11.sp,
                        color = theme.textSecondary.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun CenterSwapButton(onClick: () -> Unit) {
    val theme = LocalAppTheme.current.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = theme.funcBtn,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.35f)),
            modifier = Modifier
                .size(34.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap",
                    tint = theme.accent,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    val theme = LocalAppTheme.current.value
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = theme.textSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
    }
}

@Composable
fun DatePickerItem(
    label: String,
    dateStr: String,
    onClick: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = theme.textSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = theme.numBtn,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dateStr, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = theme.textPrimary)
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Pick Date",
                    tint = theme.accent,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}