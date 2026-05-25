package com.kelompok.duidtracker.ui.personal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog untuk menambah transaksi baru (Pribadi & Grup).
 * FIX #2: Tambah parameter prefillNominal untuk integrasi SplitBill.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (nama: String, nominal: Double, kategori: String, type: String, tanggal: Long) -> Unit,
    prefillNominal: Double? = null // FIX #2: prefill dari SplitBillDialog
) {
    var nama by remember { mutableStateOf("") }

    // FIX #2: Pakai prefillNominal sebagai nilai awal jika ada
    var nominalString by remember {
        mutableStateOf(prefillNominal?.toLong()?.toString() ?: "")
    }

    var type by remember { mutableStateOf(TransactionEntity.TYPE_EXPENSE) }

    val incomeCategories = listOf("Gaji", "Uang Jajan", "Bonus", "Freelance", "Bisnis", "Hadiah", "Lainnya")
    val expenseCategories = listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Kesehatan", "Tagihan", "Lainnya")

    var selectedKategori by remember(type) {
        mutableStateOf(if (type == TransactionEntity.TYPE_INCOME) incomeCategories[0] else expenseCategories[0])
    }

    var expandedKategori by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDatePicker by remember { mutableStateOf(false) }

    val formattedDate = remember(datePickerState.selectedDateMillis) {
        val date = Date(datePickerState.selectedDateMillis ?: System.currentTimeMillis())
        SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(date)
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tambah Transaksi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                // FIX #2: Tampilkan badge jika nominal dari split bill
                if (prefillNominal != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nominal dari Split Bill",
                        fontSize = 11.sp,
                        color = Color(0xFF1565C0).copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Type Selector
                Row(modifier = Modifier.fillMaxWidth()) {
                    val types = listOf(
                        TransactionEntity.TYPE_INCOME to "Pemasukan",
                        TransactionEntity.TYPE_EXPENSE to "Pengeluaran"
                    )
                    types.forEach { (t, label) ->
                        val isSelected = type == t
                        val bgColor = if (isSelected) Color(0xFF1565C0) else Color.Transparent
                        val contentColor = if (isSelected) Color.White else Color(0xFF1565C0)

                        Card(
                            onClick = { type = t },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = bgColor),
                            border = if (!isSelected) BorderStroke(1.dp, Color(0xFF1565C0)) else null
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = label, color = contentColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Transaksi") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nominalString,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) nominalString = input
                    },
                    label = { Text("Jumlah (Rp)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("Rp ") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedKategori,
                    onExpandedChange = { expandedKategori = !expandedKategori },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedKategori,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedKategori,
                        onDismissRequest = { expandedKategori = false }
                    ) {
                        val currentCategories =
                            if (type == TransactionEntity.TYPE_INCOME) incomeCategories else expenseCategories
                        currentCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category) },
                                onClick = {
                                    selectedKategori = category
                                    expandedKategori = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Tanggal: $formattedDate", color = Color.Black)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Batal") }

                    val nominalValue = nominalString.toDoubleOrNull() ?: 0.0
                    val isFormValid = nama.isNotBlank() && nominalValue > 0

                    Button(
                        onClick = {
                            onConfirm(
                                nama,
                                nominalValue,
                                selectedKategori,
                                type,
                                datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            )
                        },
                        enabled = isFormValid,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                    ) { Text("Simpan") }
                }
            }
        }
    }
}
