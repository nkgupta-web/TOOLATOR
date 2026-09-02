package com.universal.calulator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

data class ShoppingItemData(
    val id: Long = System.currentTimeMillis() + (0..1000).random(),
    var price: String = "",
    var discount: String = "",
    var qty: String = "1"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaxShoppingCalculator(
    toolId: String,
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value
    val isGst = toolId.lowercase() in listOf("gst", "tool_gst")

    // GST States
    var gstAmountInput by remember { mutableStateOf("1000") }
    var gstRateInput by remember { mutableStateOf("18") }
    var isGstInclusive by remember { mutableStateOf(false) }
    var isIgstMode by remember { mutableStateOf(false) }

    // Discount States
    var discountMode by remember { mutableStateOf("Single") }
    var singlePriceInput by remember { mutableStateOf("1000") }
    var singleDiscountInput by remember { mutableStateOf("20") }
    var singleExtraDiscountInput by remember { mutableStateOf("") }
    var singleDiscountType by remember { mutableStateOf("%") }
    var singleQtyInput by remember { mutableStateOf("1") }

    // Multi-Item State
    var cartItems by remember {
        mutableStateOf(
            listOf(
                ShoppingItemData(price = "800", discount = "30", qty = "1"),
                ShoppingItemData(price = "1500", discount = "25", qty = "1")
            )
        )
    }

    fun formatWithCommas(raw: Double): String {
        return try {
            val bd = BigDecimal.valueOf(raw).setScale(2, RoundingMode.HALF_UP)
            val df = DecimalFormat("#,##,###.##")
            val formatted = df.format(bd)
            if (!formatted.contains(".")) "$formatted.00" else formatted
        } catch (_: Exception) {
            raw.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = theme.textPrimary)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (isGst) "GST Calculator" else "Discount Calculator",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = theme.textPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isGst) {
                // ==================== GST CALCULATOR ====================
                val rawAmount = gstAmountInput.toDoubleOrNull() ?: 0.0
                val rate = gstRateInput.toDoubleOrNull() ?: 0.0

                val netPrice: Double
                val totalGst: Double
                val totalAmount: Double

                if (isGstInclusive) {
                    totalAmount = rawAmount
                    netPrice = if (rate > 0) rawAmount / (1.0 + (rate / 100.0)) else rawAmount
                    totalGst = (totalAmount - netPrice).coerceAtLeast(0.0)
                } else {
                    netPrice = rawAmount
                    totalGst = (netPrice * rate) / 100.0
                    totalAmount = netPrice + totalGst
                }

                val halfTax = totalGst / 2.0
                val totalAmountStr = "₹ ${formatWithCommas(totalAmount)}"
                val totalAmountScrollState = rememberScrollState()

                // GST Result Summary Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isGstInclusive) "Total Amount (Inclusive)" else "Total Amount (Post GST)",
                            fontSize = 13.sp,
                            color = theme.textSecondary
                        )
                        Spacer(Modifier.height(4.dp))

                        val dynamicBigFontSize = when {
                            totalAmountStr.length <= 14 -> 30.sp
                            totalAmountStr.length <= 20 -> 24.sp
                            totalAmountStr.length <= 26 -> 19.sp
                            else -> 16.sp
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(totalAmountScrollState),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = totalAmountStr,
                                fontSize = dynamicBigFontSize,
                                fontWeight = FontWeight.Bold,
                                color = theme.accent,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                        Spacer(Modifier.height(14.dp))

                        // Net Price & Total GST Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Net Price", fontSize = 12.sp, color = theme.textSecondary)
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = "₹ ${formatWithCommas(netPrice)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.textPrimary,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(text = "Total GST ($rate%)", fontSize = 12.sp, color = theme.textSecondary)
                                Spacer(Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "₹ ${formatWithCommas(totalGst)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = theme.accent,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Tax Breakup (CGST/SGST vs IGST)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.numBtn,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isIgstMode) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Integrated GST (IGST)",
                                        fontSize = 12.sp,
                                        color = theme.textSecondary
                                    )
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        Text(
                                            text = "₹ ${formatWithCommas(totalGst)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.Start
                                    ) {
                                        Text(
                                            text = "CGST (${rate / 2.0}%)",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                            Text(
                                                text = "₹ ${formatWithCommas(halfTax)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(30.dp)
                                            .background(theme.funcBtn)
                                    )

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.End
                                    ) {
                                        Text(
                                            text = "SGST (${rate / 2.0}%)",
                                            fontSize = 11.sp,
                                            color = theme.textSecondary
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Row(
                                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.End
                                        ) {
                                            Text(
                                                text = "₹ ${formatWithCommas(halfTax)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = theme.textPrimary,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // GST Inputs
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Inclusive / Exclusive Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.numBtn)
                                .padding(4.dp)
                        ) {
                            listOf("GST Exclusive (+)", "GST Inclusive (-)").forEachIndexed { idx, label ->
                                val isSelected = (idx == 1) == isGstInclusive
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) theme.accent else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isGstInclusive = (idx == 1) }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Tax Type Switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.numBtn)
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val options = listOf(
                                Pair("Intra-state", "(CGST + SGST)"),
                                Pair("Inter-state", "(IGST)")
                            )

                            options.forEachIndexed { idx, item ->
                                val isSelected = (idx == 1) == isIgstMode
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) theme.accent else Color.Transparent,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { isIgstMode = (idx == 1) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = item.first,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary
                                        )
                                        Text(
                                            text = item.second,
                                            fontSize = 10.sp,
                                            color = if (isSelected) (if (theme.isLight) Color.White.copy(alpha = 0.85f) else Color.Black.copy(alpha = 0.85f)) else theme.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Amount Input
                        Column {
                            Text(
                                text = if (isGstInclusive) "Total Price (MRP with GST)" else "Base Price (Before GST)",
                                fontSize = 13.sp,
                                color = theme.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))
                            OutlinedTextField(
                                value = gstAmountInput,
                                onValueChange = { gstAmountInput = it },
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // GST Rate Slabs & Custom Input
                        Column {
                            Text(
                                text = "GST Rate Slab (%)",
                                fontSize = 13.sp,
                                color = theme.textSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(Modifier.height(6.dp))

                            val presets = listOf("0", "5", "12", "18", "28")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                presets.forEach { p ->
                                    val isSelected = gstRateInput == p
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) theme.accent else Color.Transparent,
                                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)) else null,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { gstRateInput = p }
                                    ) {
                                        Text(
                                            text = "$p%",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = gstRateInput,
                                onValueChange = { if (it.length <= 5) gstRateInput = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                placeholder = { Text("Custom GST %", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                textStyle = TextStyle(
                                    color = theme.textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                shape = RoundedCornerShape(12.dp),
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
                        }
                    }
                }
            } else {
                // ==================== DISCOUNT CALCULATOR ====================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(theme.surface)
                        .padding(4.dp)
                ) {
                    listOf("Single Item", "Multi-Item (Cart)").forEach { m ->
                        val isSelected = (m.startsWith("Single") && discountMode == "Single") ||
                                (m.startsWith("Multi") && discountMode == "Multi")
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.accent else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { discountMode = if (m.startsWith("Single")) "Single" else "Multi" }
                        ) {
                            Text(
                                text = m,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                if (discountMode == "Single") {
                    val price = singlePriceInput.toDoubleOrNull() ?: 0.0
                    val disc = singleDiscountInput.toDoubleOrNull() ?: 0.0
                    val extraDisc = singleExtraDiscountInput.toDoubleOrNull() ?: 0.0
                    val qty = (singleQtyInput.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)

                    val totalOriginal = price * qty

                    val firstStageDiscount = if (singleDiscountType == "%") (totalOriginal * disc) / 100.0 else (disc * qty)
                    val priceAfterFirst = (totalOriginal - firstStageDiscount).coerceAtLeast(0.0)

                    val extraDiscountAmount = (priceAfterFirst * extraDisc) / 100.0
                    val finalPayable = (priceAfterFirst - extraDiscountAmount).coerceAtLeast(0.0)
                    val totalSavings = totalOriginal - finalPayable

                    val finalPayableStr = "₹ ${formatWithCommas(finalPayable)}"

                    // Result Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Final Price to Pay", fontSize = 13.sp, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))

                            val dynamicDiscFontSize = when {
                                finalPayableStr.length <= 14 -> 30.sp
                                finalPayableStr.length <= 20 -> 24.sp
                                finalPayableStr.length <= 26 -> 19.sp
                                else -> 16.sp
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = finalPayableStr,
                                    fontSize = dynamicDiscFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accent,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Total MRP (${qty.toInt()} items)", fontSize = 12.sp, color = theme.textSecondary)
                                    Spacer(Modifier.height(2.dp))
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                        Text(
                                            text = "₹ ${formatWithCommas(totalOriginal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(text = "You Save", fontSize = 12.sp, color = theme.textSecondary)
                                    Spacer(Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "₹ ${formatWithCommas(totalSavings)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Single Item Inputs
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(text = "Original Price per item (₹)", fontSize = 13.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = singlePriceInput,
                                        onValueChange = { singlePriceInput = it },
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Quantity", fontSize = 13.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = singleQtyInput,
                                        onValueChange = { if (it.length <= 4) singleQtyInput = it },
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Discount & Type Switch
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Discount", fontSize = 13.sp, color = theme.textSecondary, fontWeight = FontWeight.Medium)

                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.numBtn)
                                            .padding(2.dp)
                                    ) {
                                        listOf("%", "₹").forEach { sym ->
                                            val isSelected = singleDiscountType == sym
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isSelected) theme.accent else Color.Transparent,
                                                modifier = Modifier.clickable { singleDiscountType = sym }
                                            ) {
                                                Text(
                                                    text = sym,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(6.dp))

                                if (singleDiscountType == "%") {
                                    val presets = listOf("10", "20", "30", "50")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        presets.forEach { p ->
                                            val isSelected = singleDiscountInput == p
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) theme.accent else Color.Transparent,
                                                border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)) else null,
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { singleDiscountInput = p }
                                            ) {
                                                Text(
                                                    text = "$p%",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                                                    modifier = Modifier.padding(vertical = 7.dp),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                }

                                OutlinedTextField(
                                    value = singleDiscountInput,
                                    onValueChange = { if (it.length <= 6) singleDiscountInput = it },
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
                                    placeholder = { Text("Enter discount", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Extra Additional Discount
                            Column {
                                Text(
                                    text = "Extra Discount (After First Discount)",
                                    fontSize = 13.sp,
                                    color = theme.textSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Applied on the discounted price",
                                    fontSize = 11.sp,
                                    color = theme.textSecondary.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = singleExtraDiscountInput,
                                    onValueChange = { if (it.length <= 4) singleExtraDiscountInput = it },
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
                                    placeholder = { Text("e.g. Extra 10% off", color = theme.textSecondary.copy(alpha = 0.5f)) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    // Multi-Item Calculations
                    var combinedOriginal = 0.0
                    var combinedFinal = 0.0

                    cartItems.forEach { item ->
                        val p = item.price.toDoubleOrNull() ?: 0.0
                        val d = item.discount.toDoubleOrNull() ?: 0.0
                        val q = (item.qty.toDoubleOrNull() ?: 1.0).coerceAtLeast(1.0)

                        val orig = p * q
                        val discounted = (orig - (orig * d / 100.0)).coerceAtLeast(0.0)

                        combinedOriginal += orig
                        combinedFinal += discounted
                    }

                    val combinedSavings = combinedOriginal - combinedFinal
                    val combinedFinalStr = "₹ ${formatWithCommas(combinedFinal)}"

                    // Multi-Item Result Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = theme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Total Cart Bill", fontSize = 13.sp, color = theme.textSecondary)
                            Spacer(Modifier.height(4.dp))

                            val dynamicMultiFontSize = when {
                                combinedFinalStr.length <= 14 -> 30.sp
                                combinedFinalStr.length <= 20 -> 24.sp
                                combinedFinalStr.length <= 26 -> 19.sp
                                else -> 16.sp
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = combinedFinalStr,
                                    fontSize = dynamicMultiFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.accent,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = theme.funcBtn.copy(alpha = 0.4f))
                            Spacer(Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Total MRP (${cartItems.size} items)", fontSize = 12.sp, color = theme.textSecondary)
                                    Spacer(Modifier.height(2.dp))
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                        Text(
                                            text = "₹ ${formatWithCommas(combinedOriginal)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = theme.textPrimary,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(text = "Total Savings", fontSize = 12.sp, color = theme.textSecondary)
                                    Spacer(Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Text(
                                            text = "₹ ${formatWithCommas(combinedSavings)}",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = theme.accent,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Multi Items List
                    cartItems.forEachIndexed { index, item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = theme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Item #${index + 1}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = theme.accent
                                    )
                                    if (cartItems.size > 1) {
                                        IconButton(
                                            onClick = {
                                                cartItems = cartItems.toMutableList().also { it.removeAt(index) }
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteOutline,
                                                contentDescription = "Remove",
                                                tint = theme.textSecondary
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "Price per item (₹)", fontSize = 11.sp, color = theme.textSecondary, modifier = Modifier.weight(1.4f))
                                    Text(text = "Discount (%)", fontSize = 11.sp, color = theme.textSecondary, modifier = Modifier.weight(1f))
                                    Text(text = "Quantity", fontSize = 11.sp, color = theme.textSecondary, modifier = Modifier.weight(0.9f))
                                }

                                Spacer(Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = item.price,
                                        onValueChange = { newVal ->
                                            cartItems = cartItems.toMutableList().also {
                                                it[index] = it[index].copy(price = newVal)
                                            }
                                        },
                                        placeholder = { Text("0", fontSize = 12.sp, color = theme.textSecondary.copy(alpha = 0.5f)) },
                                        textStyle = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 14.sp,
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1.4f)
                                    )

                                    OutlinedTextField(
                                        value = item.discount,
                                        onValueChange = { newVal ->
                                            cartItems = cartItems.toMutableList().also {
                                                it[index] = it[index].copy(discount = newVal)
                                            }
                                        },
                                        placeholder = { Text("0", fontSize = 12.sp, color = theme.textSecondary.copy(alpha = 0.5f)) },
                                        textStyle = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 14.sp,
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
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )

                                    OutlinedTextField(
                                        value = item.qty,
                                        onValueChange = { newVal ->
                                            cartItems = cartItems.toMutableList().also {
                                                it[index] = it[index].copy(qty = newVal)
                                            }
                                        },
                                        placeholder = { Text("1", fontSize = 12.sp, color = theme.textSecondary.copy(alpha = 0.5f)) },
                                        textStyle = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 14.sp,
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(0.9f)
                                    )
                                }
                            }
                        }
                    }

                    // Add Item Button
                    Button(
                        onClick = {
                            cartItems = cartItems + ShoppingItemData()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.funcBtn),
                        border = androidx.compose.foundation.BorderStroke(1.dp, theme.accent.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = theme.accent)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Another Item", color = theme.accent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}