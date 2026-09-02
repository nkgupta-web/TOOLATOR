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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Refresh
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
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixCalculatorScreen(
    onBack: () -> Unit
) {
    val theme = LocalAppTheme.current.value

    var operation by remember { mutableStateOf("det") } // det, inv, trans, rank, scalar, add, sub, mul
    var scalarVal by remember { mutableStateOf("2") }

    // Matrix A dimensions
    var rowsA by remember { mutableIntStateOf(3) }
    var colsA by remember { mutableIntStateOf(3) }

    // Matrix B dimensions
    var rowsB by remember { mutableIntStateOf(3) }
    var colsB by remember { mutableIntStateOf(3) }

    val isSquareRequired = operation == "det" || operation == "inv"
    val isAddSub = operation == "add" || operation == "sub"

    // Operation switch effect
    LaunchedEffect(operation) {
        if (isSquareRequired && rowsA != colsA) {
            colsA = rowsA
        } else if (isAddSub) {
            rowsB = rowsA
            colsB = colsA
        }
    }

    // State maps for cells
    val matAState = remember {
        mutableStateMapOf<Pair<Int, Int>, String>().apply {
            for (r in 0 until 4) {
                for (c in 0 until 4) {
                    put(r to c, if (r == c) "1" else "0")
                }
            }
        }
    }

    val matBState = remember {
        mutableStateMapOf<Pair<Int, Int>, String>().apply {
            for (r in 0 until 4) {
                for (c in 0 until 4) {
                    put(r to c, if (r == c) "1" else "0")
                }
            }
        }
    }

    val parsedMatA = remember(rowsA, colsA, matAState.toMap()) {
        Array(rowsA) { r ->
            DoubleArray(colsA) { c ->
                parseMathExpression(matAState[r to c] ?: "0")
            }
        }
    }

    val parsedMatB = remember(rowsB, colsB, matBState.toMap()) {
        Array(rowsB) { r ->
            DoubleArray(colsB) { c ->
                parseMathExpression(matBState[r to c] ?: "0")
            }
        }
    }

    val operationsList = listOf(
        Pair("det", "det(A)"),
        Pair("inv", "A⁻¹"),
        Pair("trans", "Aᵀ"),
        Pair("rank", "Rank(A)"),
        Pair("scalar", "k · A"),
        Pair("add", "A + B"),
        Pair("sub", "A − B"),
        Pair("mul", "A × B")
    )

    fun fmt(n: Double): String {
        val clean = if (abs(n) < 1e-9) 0.0 else n
        return DecimalFormat("#.##").format(clean)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Top Header
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
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Matrix Calculator",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
            }

            IconButton(
                onClick = {
                    for (r in 0 until 4) {
                        for (c in 0 until 4) {
                            matAState[r to c] = if (r == c) "1" else "0"
                            matBState[r to c] = if (r == c) "1" else "0"
                        }
                    }
                },
                modifier = Modifier.size(34.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = theme.textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Operations Chip Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                operationsList.forEach { (opId, label) ->
                    val isSelected = operation == opId
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) theme.accent else theme.surface,
                        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)) else null,
                        modifier = Modifier.clickable { operation = opId }
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) (if (theme.isLight) Color.White else Color.Black) else theme.textPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Scalar Input
            if (operation == "scalar") {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Scalar Value (k):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                        OutlinedTextField(
                            value = scalarVal,
                            onValueChange = { scalarVal = it },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            textStyle = TextStyle(
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }
            }

            // Matrix A Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("MATRIX A (${rowsA} × ${colsA})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.accent)

                        // Dimension Selector for Matrix A
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            DimensionDropdown(
                                label = "R",
                                selected = rowsA,
                                onSelect = { newR ->
                                    rowsA = newR
                                    if (isSquareRequired) colsA = newR
                                    if (isAddSub) {
                                        rowsB = newR
                                    }
                                }
                            )
                            Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                            DimensionDropdown(
                                label = "C",
                                selected = colsA,
                                onSelect = { newC ->
                                    colsA = newC
                                    if (isSquareRequired) rowsA = newC
                                    if (isAddSub) {
                                        colsB = newC
                                    }
                                }
                            )
                        }
                    }

                    // Grid Inputs A
                    for (r in 0 until rowsA) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until colsA) {
                                OutlinedTextField(
                                    value = matAState[r to c] ?: "",
                                    onValueChange = { matAState[r to c] = it },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = theme.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
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
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                    shape = RoundedCornerShape(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Matrix B Card (Binary Ops)
            if (operation in listOf("add", "sub", "mul")) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = theme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("MATRIX B (${rowsB} × ${colsB})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = theme.accent)

                            // Dimension Selector for Matrix B
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                DimensionDropdown(
                                    label = "R",
                                    selected = rowsB,
                                    onSelect = { newR ->
                                        rowsB = newR
                                        if (isAddSub) {
                                            rowsA = newR
                                        }
                                    }
                                )
                                Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)
                                DimensionDropdown(
                                    label = "C",
                                    selected = colsB,
                                    onSelect = { newC ->
                                        colsB = newC
                                        if (isAddSub) {
                                            colsA = newC
                                        }
                                    }
                                )
                            }
                        }

                        // Grid Inputs B
                        for (r in 0 until rowsB) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                for (c in 0 until colsB) {
                                    OutlinedTextField(
                                        value = matBState[r to c] ?: "",
                                        onValueChange = { matBState[r to c] = it },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            color = theme.textPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center
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
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Results Display Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = theme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("CALCULATED RESULT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = theme.textSecondary)

                    when (operation) {
                        "det" -> {
                            if (rowsA != colsA) {
                                Text("Determinant is only defined for square matrices ($rowsA ≠ $colsA).", color = Color(0xFFEF5350), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                val detA = determinant(parsedMatA, rowsA)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Determinant |A|", fontSize = 14.sp, color = theme.textSecondary)
                                    Text(fmt(detA), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = theme.accent)
                                }
                            }
                        }

                        "rank" -> {
                            val rankA = computeGeneralRank(parsedMatA, rowsA, colsA)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Matrix Rank (${rowsA} × ${colsA})", fontSize = 14.sp, color = theme.textSecondary)
                                Text(rankA.toString(), fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4CAF50))
                            }
                        }

                        "trans" -> {
                            val transA = transposeGeneral(parsedMatA, rowsA, colsA)
                            Text("Aᵀ (${colsA} × ${rowsA}):", fontSize = 11.sp, color = theme.textSecondary)
                            MatrixResultDisplayGeneral(transA, colsA, rowsA)
                        }

                        "scalar" -> {
                            val k = parseMathExpression(scalarVal)
                            val res = Array(rowsA) { r -> DoubleArray(colsA) { c -> cleanZero(parsedMatA[r][c] * k) } }
                            MatrixResultDisplayGeneral(res, rowsA, colsA)
                        }

                        "add" -> {
                            val res = Array(rowsA) { r -> DoubleArray(colsA) { c -> cleanZero(parsedMatA[r][c] + parsedMatB[r][c]) } }
                            MatrixResultDisplayGeneral(res, rowsA, colsA)
                        }

                        "sub" -> {
                            val res = Array(rowsA) { r -> DoubleArray(colsA) { c -> cleanZero(parsedMatA[r][c] - parsedMatB[r][c]) } }
                            MatrixResultDisplayGeneral(res, rowsA, colsA)
                        }

                        "mul" -> {
                            if (colsA != rowsB) {
                                Text("Multiplication impossible: Columns of A ($colsA) must match Rows of B ($rowsB).", color = Color(0xFFEF5350), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                val res = multiplyGeneral(parsedMatA, parsedMatB, rowsA, colsA, colsB)
                                Text("Product Dimension: (${rowsA} × ${colsB})", fontSize = 11.sp, color = theme.textSecondary)
                                MatrixResultDisplayGeneral(res, rowsA, colsB)
                            }
                        }

                        "inv" -> {
                            if (rowsA != colsA) {
                                Text("Inverse is only defined for square matrices ($rowsA ≠ $colsA).", color = Color(0xFFEF5350), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            } else {
                                val detA = determinant(parsedMatA, rowsA)
                                if (abs(detA) < 1e-9) {
                                    Text("Matrix is Singular (|A| = 0). Inverse does not exist.", color = Color(0xFFEF5350), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    val invA = inverseGeneral(parsedMatA, rowsA, detA)
                                    MatrixResultDisplayGeneral(invA, rowsA, rowsA)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun DimensionDropdown(label: String, selected: Int, onSelect: (Int) -> Unit) {
    val theme = LocalAppTheme.current.value
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = theme.numBtn,
            border = androidx.compose.foundation.BorderStroke(1.dp, theme.funcBtn.copy(alpha = 0.5f)),
            modifier = Modifier.clickable { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "$label: $selected",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = theme.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(theme.surface)
        ) {
            (1..4).forEach { num ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$num",
                            fontWeight = if (num == selected) FontWeight.Bold else FontWeight.Normal,
                            color = if (num == selected) theme.accent else theme.textPrimary
                        )
                    },
                    onClick = {
                        onSelect(num)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MatrixResultDisplayGeneral(matrix: Array<DoubleArray>, rows: Int, cols: Int) {
    val theme = LocalAppTheme.current.value
    fun fmt(n: Double): String {
        val clean = if (abs(n) < 1e-9) 0.0 else n
        return DecimalFormat("#.##").format(clean)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(theme.numBtn)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (c in 0 until cols) {
                    Text(
                        text = fmt(matrix[r][c]),
                        fontSize = if (cols >= 4) 12.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.accent
                    )
                }
            }
        }
    }
}

// Clean negative zero helper
private fun cleanZero(v: Double): Double = if (abs(v) < 1e-9) 0.0 else v

// Mathematical Expression Parser for Matrix Cells
private fun parseMathExpression(str: String): Double {
    val s = str.trim().replace(" ", "").replace("√", "sqrt")
    if (s.isEmpty()) return 0.0

    return try {
        when {
            s.contains("/") -> {
                val parts = s.split("/")
                val num = parseMathExpression(parts[0])
                val den = parseMathExpression(parts[1])
                if (den != 0.0) num / den else 0.0
            }
            s.startsWith("sqrt(") && s.endsWith(")") -> {
                val inner = s.substring(5, s.length - 1).toDouble()
                sqrt(inner)
            }
            s.startsWith("sqrt") -> {
                val inner = s.substring(4).toDouble()
                sqrt(inner)
            }
            s.contains("*") -> {
                val parts = s.split("*")
                parseMathExpression(parts[0]) * parseMathExpression(parts[1])
            }
            else -> s.toDoubleOrNull() ?: 0.0
        }
    } catch (_: Exception) {
        0.0
    }
}

// General N x N Matrix Algorithms (1x1 to 4x4)
private fun determinant(mat: Array<DoubleArray>, n: Int): Double {
    if (n == 1) return cleanZero(mat[0][0])
    if (n == 2) return cleanZero((mat[0][0] * mat[1][1]) - (mat[0][1] * mat[1][0]))

    var det = 0.0
    for (f in 0 until n) {
        val subMat = getSubMatrix(mat, 0, f, n)
        val sign = if (f % 2 == 0) 1.0 else -1.0
        det += sign * mat[0][f] * determinant(subMat, n - 1)
    }
    return cleanZero(det)
}

private fun getSubMatrix(mat: Array<DoubleArray>, skipRow: Int, skipCol: Int, n: Int): Array<DoubleArray> {
    val sub = Array(n - 1) { DoubleArray(n - 1) }
    var r = 0
    for (i in 0 until n) {
        if (i == skipRow) continue
        var c = 0
        for (j in 0 until n) {
            if (j == skipCol) continue
            sub[r][c] = mat[i][j]
            c++
        }
        r++
    }
    return sub
}

private fun inverseGeneral(mat: Array<DoubleArray>, n: Int, det: Double): Array<DoubleArray> {
    if (n == 1) return arrayOf(doubleArrayOf(cleanZero(1.0 / mat[0][0])))

    val inv = Array(n) { DoubleArray(n) }
    for (i in 0 until n) {
        for (j in 0 until n) {
            val subMat = getSubMatrix(mat, i, j, n)
            val sign = if ((i + j) % 2 == 0) 1.0 else -1.0
            val cofactor = sign * determinant(subMat, n - 1)
            inv[j][i] = cleanZero(cofactor / det)
        }
    }
    return inv
}

private fun transposeGeneral(mat: Array<DoubleArray>, rows: Int, cols: Int): Array<DoubleArray> {
    return Array(cols) { c -> DoubleArray(rows) { r -> cleanZero(mat[r][c]) } }
}

private fun multiplyGeneral(a: Array<DoubleArray>, b: Array<DoubleArray>, rA: Int, cA: Int, cB: Int): Array<DoubleArray> {
    val res = Array(rA) { DoubleArray(cB) }
    for (i in 0 until rA) {
        for (j in 0 until cB) {
            var sum = 0.0
            for (k in 0 until cA) {
                sum += a[i][k] * b[k][j]
            }
            res[i][j] = cleanZero(sum)
        }
    }
    return res
}

private fun computeGeneralRank(mat: Array<DoubleArray>, rows: Int, cols: Int): Int {
    val a = Array(rows) { r -> mat[r].clone() }
    var rank = minOf(rows, cols)
    var row = 0

    for (col in 0 until cols) {
        if (row >= rows) break

        var pivot = row
        while (pivot < rows && abs(a[pivot][col]) < 1e-9) {
            pivot++
        }

        if (pivot < rows) {
            val temp = a[row]
            a[row] = a[pivot]
            a[pivot] = temp

            val div = a[row][col]
            for (j in col until cols) a[row][j] /= div

            for (i in 0 until rows) {
                if (i != row && abs(a[i][col]) > 1e-9) {
                    val factor = a[i][col]
                    for (j in col until cols) {
                        a[i][j] -= factor * a[row][j]
                    }
                }
            }
            row++
        }
    }

    var nonZeroRows = 0
    for (i in 0 until rows) {
        if (a[i].any { abs(it) > 1e-9 }) nonZeroRows++
    }
    return nonZeroRows
}