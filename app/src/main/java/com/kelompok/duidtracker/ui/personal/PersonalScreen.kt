package com.kelompok.duidtracker.ui.personal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import com.kelompok.duidtracker.data.local.entity.toFormattedNominal
import com.kelompok.duidtracker.viewmodel.TransactionViewModel

/**
 * Layar Utama Transaksi Pribadi.
 */
@Composable
fun PersonalScreen(viewModel: TransactionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    // Menampilkan pesan error jika ada (Issue 2)
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(padding)
        ) {
            SummarySection(income = uiState.totalIncome, expense = uiState.totalExpense)

            Text(
                text = "Transaksi Terakhir",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.transactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(uiState.transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nama, nominal, kategori, tipe, tanggal ->
                viewModel.addTransaction(nama, nominal, kategori, tipe, tanggal)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SummarySection(income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Total Saldo", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(
                text = "Rp ${formatNominal(income - expense)}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem("Pemasukan", income, Icons.Default.ArrowUpward, Color(0xFF00BFA5), Modifier.weight(1f))
                SummaryItem("Pengeluaran", expense, Icons.Default.ArrowDownward, Color(0xFFFF5252), Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, icon: ImageVector, iconColor: Color, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text("Rp ${formatNominal(amount)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TransactionItem(transaction: TransactionEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val isIncome = transaction.type == TransactionEntity.TYPE_INCOME
            Box(
                modifier = Modifier.size(40.dp).background(if (isIncome) Color(0xFFE0F2F1) else Color(0xFFFFEBEE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null, tint = if (isIncome) Color(0xFF00BFA5) else Color(0xFFFF5252), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.nama, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(transaction.kategori, color = Color.Gray, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = (if (isIncome) "+ " else "- ") + transaction.toFormattedNominal, color = if (isIncome) Color(0xFF00BFA5) else Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, "Hapus", tint = Color.LightGray)
                }
            }
        }
    }
}

private fun formatNominal(amount: Double): String {
    val symbols = java.text.DecimalFormatSymbols(java.util.Locale.forLanguageTag("id-ID")).apply { groupingSeparator = '.' }
    return java.text.DecimalFormat("#,###", symbols).format(amount)
}
