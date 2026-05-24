package com.kelompok.duidtracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kelompok.duidtracker.data.local.entity.TransactionEntity
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Komponen kartu untuk menampilkan satu item transaksi.
 * Sesuai persyaratan Step F4.1:
 * - Layout: Icon (Kiri), Info (Tengah), Nominal & Delete (Kanan)
 * - Dialog konfirmasi sebelum menghapus
 * - Format tanggal: dd MMM yyyy
 */
@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onDelete: (TransactionEntity) -> Unit,
    showDeleteButton: Boolean = true
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    val isIncome = transaction.type == TransactionEntity.TYPE_INCOME
    val statusColor = if (isIncome) Color(0xFF4CAF50) else Color(0xFFF44336)
    
    // Format Mata Uang Rupiah
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
        maximumFractionDigits = 0
    }
    val formattedNominal = formatter.format(transaction.nominal)
    val displayNominal = if (isIncome) "+$formattedNominal" else "-$formattedNominal"

    // Format Tanggal
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    val formattedDate = dateFormat.format(Date(transaction.tanggal))

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Hapus Transaksi") },
            text = { Text("Apakah Anda yakin ingin menghapus catatan '${transaction.nama}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(transaction)
                    showConfirmDialog = false
                }) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bagian Kiri: Ikon dalam lingkaran warna
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bagian Tengah: Info Transaksi
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.nama,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${transaction.kategori} • $formattedDate",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            // Bagian Kanan: Nominal & Tombol Hapus
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = displayNominal,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                if (showDeleteButton) {
                    IconButton(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
