package com.universal.calulator

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCalculatorScreen(
    toolId: String = "bmi",
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value

    var gender by remember { mutableStateOf("Male") }
    var ageInput by remember { mutableStateOf("19") }

    var heightUnit by remember { mutableStateOf("cm") }
    var heightCmInput by remember { mutableStateOf("178") }
    var heightFtInput by remember { mutableStateOf("5") }
    var heightInInput by remember { mutableStateOf("10") }

    var weightUnit by remember { mutableStateOf("kg") }
    var weightInput by remember { mutableStateOf("78") }

    var goalWeightInput by remember { mutableStateOf("75") }
    var durationWeeksInput by remember { mutableStateOf("16") }

    val heightInMeters: Double = if (heightUnit == "cm") {
        (heightCmInput.toDoubleOrNull() ?: 0.0) / 100.0
    } else {
        val ft = heightFtInput.toDoubleOrNull() ?: 0.0
        val inches = heightInInput.toDoubleOrNull() ?: 0.0
        ((ft * 12.0) + inches) * 0.0254
    }

    val currentWeightKg: Double = if (weightUnit == "kg") {
        weightInput.toDoubleOrNull() ?: 0.0
    } else {
        (weightInput.toDoubleOrNull() ?: 0.0) * 0.45359237
    }

    val minHealthyWeight = 18.5 * (heightInMeters * heightInMeters)
    val maxHealthyWeight = 24.9 * (heightInMeters * heightInMeters)
    val midHealthyWeight = 21.7 * (heightInMeters * heightInMeters)

    val isAutoHealthyGoal = goalWeightInput.isBlank()
    val goalWeightKg: Double = if (isAutoHealthyGoal) {
        if (midHealthyWeight > 5.0) midHealthyWeight else currentWeightKg
    } else {
        val parsed = goalWeightInput.toDoubleOrNull() ?: currentWeightKg
        if (weightUnit == "kg") parsed else parsed * 0.45359237
    }

    val durationWeeks: Double = (durationWeeksInput.toDoubleOrNull() ?: 12.0).coerceAtLeast(1.0)
    val age: Double = (ageInput.toDoubleOrNull() ?: 19.0).coerceAtLeast(1.0)

    val bmi = if (heightInMeters > 0.4 && currentWeightKg > 5.0) {
        currentWeightKg / (heightInMeters * heightInMeters)
    } else {
        0.0
    }

    val (bmiStatus, statusColor) = when {
        bmi <= 0.0 -> Pair("Enter Valid Details", theme.textSecondary)
        bmi < 18.5 -> Pair("Underweight", Color(0xFF4FC3F7))
        bmi in 18.5..24.9 -> Pair("Healthy Weight", Color(0xFF4CAF50))
        bmi in 25.0..29.9 -> Pair("Overweight", Color(0xFFFFA726))
        else -> Pair("Obese", Color(0xFFEF5350))
    }

    val heightInCm = heightInMeters * 100.0
    val bmr = if (gender == "Male") {
        (10.0 * currentWeightKg) + (6.25 * heightInCm) - (5.0 * age) + 5.0
    } else {
        (10.0 * currentWeightKg) + (6.25 * heightInCm) - (5.0 * age) - 161.0
    }
    val maintenanceCalories = (bmr * 1.35).coerceAtLeast(1200.0)

    val totalWeightDiff = goalWeightKg - currentWeightKg
    val weeklyWeightChangeKg = totalWeightDiff / durationWeeks
    val isAggressiveTarget = abs(weeklyWeightChangeKg) > 1.0

    val recommendedSafeWeeks = if (abs(totalWeightDiff) > 0.1) {
        (abs(totalWeightDiff) / 0.75).toInt().coerceAtLeast(1)
    } else 1

    val minSafeFloor = if (gender == "Male") 1500.0 else 1200.0
    val maxSafeCap = maintenanceCalories + 1000.0

    val rawDailyAdjustment = (weeklyWeightChangeKg * 7700.0) / 7.0
    val unclampedCalories = maintenanceCalories + rawDailyAdjustment

    val isFloorClamped = unclampedCalories < minSafeFloor && abs(totalWeightDiff) > 0.05
    val isCeilingClamped = unclampedCalories > maxSafeCap && abs(totalWeightDiff) > 0.05

    val targetDailyCalories = unclampedCalories.coerceIn(minSafeFloor, maxSafeCap).toInt()

    val maintFormatted = DecimalFormat("#,###").format(maintenanceCalories.toInt())
    val targetFormatted = DecimalFormat("#,###").format(targetDailyCalories)
    val weeklyChangeFormatted = DecimalFormat("#0.00").format(abs(weeklyWeightChangeKg))

    fun format(n: Double): String = DecimalFormat("#0.0").format(n)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
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
            Spacer(Modifier.width(8.dp))
            Text(
                text = "BMI & Calorie Tracker",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = theme.textPrimary
            )
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. INPUTS CARD
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
                    // ROW 1: GENDER | AGE
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "GENDER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(theme.numBtn)
                                    .padding(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                listOf("Male", "Female").forEach { g ->
                                    val isSelected = gender == g
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = if (isSelected) theme.accent else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clickable { gender = g }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = g,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "AGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ageInput,
                                onValueChange = { if (it.length <= 3) ageInput = it },
                                placeholder = { Text("19", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )
                        }
                    }

                    // ROW 2: CURRENT WEIGHT | HEIGHT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Current Weight
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "CURRENT WEIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = weightInput,
                                onValueChange = { if (it.length <= 6) weightInput = it },
                                placeholder = { Text("78", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
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
                                trailingIcon = {
                                    Row(
                                        modifier = Modifier
                                            .padding(end = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.surface)
                                            .padding(2.dp)
                                    ) {
                                        listOf("kg", "lbs").forEach { u ->
                                            val isSelected = weightUnit == u
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) theme.accent else Color.Transparent,
                                                modifier = Modifier.clickable { weightUnit = u }
                                            ) {
                                                Text(
                                                    text = u,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )
                        }

                        // Height
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "HEIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))
                            if (heightUnit == "cm") {
                                OutlinedTextField(
                                    value = heightCmInput,
                                    onValueChange = { if (it.length <= 5) heightCmInput = it },
                                    placeholder = { Text("178", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.textPrimary
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
                                    trailingIcon = {
                                        Row(
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(theme.surface)
                                                .padding(2.dp)
                                        ) {
                                            listOf("cm", "ft").forEach { u ->
                                                val isSelected = heightUnit == u
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) theme.accent else Color.Transparent,
                                                    modifier = Modifier.clickable { heightUnit = u }
                                                ) {
                                                    Text(
                                                        text = u,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                )
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = heightFtInput,
                                        onValueChange = { if (it.length <= 2) heightFtInput = it },
                                        placeholder = { Text("5", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary
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
                                        modifier = Modifier
                                            .weight(0.9f)
                                            .fillMaxHeight()
                                    )
                                    OutlinedTextField(
                                        value = heightInInput,
                                        onValueChange = { if (it.length <= 2) heightInInput = it },
                                        placeholder = { Text("10", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary
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
                                        trailingIcon = {
                                            Row(
                                                modifier = Modifier
                                                    .padding(end = 2.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(theme.surface)
                                                    .padding(2.dp)
                                            ) {
                                                listOf("cm", "ft").forEach { u ->
                                                    val isSelected = heightUnit == u
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isSelected) theme.accent else Color.Transparent,
                                                        modifier = Modifier.clickable { heightUnit = u }
                                                    ) {
                                                        Text(
                                                            text = u,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .fillMaxHeight()
                                    )
                                }
                            }
                        }
                    }

                    // ROW 3: GOAL WEIGHT | DURATION
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "GOAL WEIGHT ($weightUnit)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = goalWeightInput,
                                onValueChange = { if (it.length <= 6) goalWeightInput = it },
                                placeholder = { Text("Auto (Healthy)", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "DURATION (WEEKS)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = durationWeeksInput,
                                onValueChange = { if (it.length <= 3) durationWeeksInput = it },
                                placeholder = { Text("16", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = theme.textPrimary
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            )
                        }
                    }
                }
            }

            // 2. BMI HERO & GRADIENT GAUGE CARD
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
                        text = "BODY MASS INDEX (BMI)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textSecondary,
                        letterSpacing = 0.6.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (bmi > 0) format(bmi) else "--",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Spacer(Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = bmiStatus,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    BmiGradientBarGauge(bmi = bmi)

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Healthy Weight Range", fontSize = 12.sp, color = theme.textSecondary)
                        Text(
                            text = if (heightInMeters > 0.4) "${format(minHealthyWeight)} – ${format(maxHealthyWeight)} kg" else "--",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                    }
                }
            }

            // 3. TARGET DAILY CALORIE GOAL CARD
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isAggressiveTarget) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEF5350).copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF5350), modifier = Modifier.size(15.dp))
                                Text(
                                    text = "Aggressive target! Safe recommended timeline is ~$recommendedSafeWeeks weeks.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFEF5350)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESTIMATED DAILY CALORIE TARGET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textSecondary,
                            letterSpacing = 0.6.sp
                        )
                        Text(
                            text = "Mifflin-St Jeor",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.accent
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "~$targetFormatted kcal / day",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = theme.accent
                        )

                        if (isFloorClamped) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFA726).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Capped at safe floor (${minSafeFloor.toInt()} kcal)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA726),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        } else if (isCeilingClamped) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFFA726).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "Capped at safe ceiling (${maxSafeCap.toInt()} kcal)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA726),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isAutoHealthyGoal) {
                            "To reach ideal healthy goal of ${format(goalWeightKg)} kg in ${durationWeeks.toInt()} weeks."
                        } else {
                            "To reach your goal of ${format(goalWeightKg)} kg in ${durationWeeks.toInt()} weeks."
                        },
                        fontSize = 12.sp,
                        color = theme.textPrimary.copy(alpha = 0.88f)
                    )

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Maintenance Calories", fontSize = 12.sp, color = theme.textSecondary)
                        Text(
                            text = "~$maintFormatted kcal/day",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Average Weekly Change", fontSize = 12.sp, color = theme.textSecondary)
                        Text(
                            text = if (totalWeightDiff >= 0) "+$weeklyChangeFormatted kg/week" else "-$weeklyChangeFormatted kg/week",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (totalWeightDiff >= 0) theme.accent else Color(0xFF4CAF50)
                        )
                    }
                }
            }

            // 4. DISCLAIMER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = theme.textSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Estimates are for general fitness guidance. Not medical advice.",
                    fontSize = 10.sp,
                    color = theme.textSecondary.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun BmiGradientBarGauge(bmi: Double) {
    val theme = LocalAppTheme.current.value

    val normalizedProgress = when {
        bmi <= 0.0 -> 0f
        bmi < 15.0 -> 0.02f
        bmi > 35.0 -> 0.98f
        else -> ((bmi - 15.0) / (35.0 - 15.0)).toFloat().coerceIn(0.02f, 0.98f)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            ) {
                val width = size.width
                val barHeight = size.height
                val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                val gradientBrush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF4FC3F7),
                        Color(0xFF4CAF50),
                        Color(0xFFFFA726),
                        Color(0xFFEF5350)
                    )
                )

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(0f, 0f),
                    size = Size(width, barHeight),
                    cornerRadius = cornerRadius
                )
            }

            if (bmi > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val indicatorX = width * normalizedProgress
                        drawRect(
                            color = theme.textPrimary,
                            topLeft = Offset(indicatorX - 1.5.dp.toPx(), 0f),
                            size = Size(3.dp.toPx(), 20.dp.toPx())
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "15 (Under)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = theme.textSecondary)
            Text(text = "18.5", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = theme.textSecondary)
            Text(text = "25.0", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = theme.textSecondary)
            Text(text = "30.0", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = theme.textSecondary)
            Text(text = "35+ (Obese)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = theme.textSecondary)
        }
    }
}