package com.kelompok.duidtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.*

/**
 * Komponen kartu ringkasan saldo (Pemasukan vs Pengeluaran).
 * Sesuai persyaratan Step F4.2.
 */
@Composable
fun BalanceSummaryCard(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
        maximumFractionDigits = 0
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)) // Blue Primary
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Total Saldo",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
            Text(
                text = formatter.format(balance),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                BalanceItem(
                    label = "Pemasukan",
                    amount = totalIncome,
                    icon = Icons.Default.ArrowUpward,
                    iconColor = Color(0xFF00BFA5), // Teal/Green
                    modifier = Modifier.weight(1f),
                    formatter = formatter
                )
                BalanceItem(
                    label = "Pengeluaran",
                    amount = totalExpense,
                    icon = Icons.Default.ArrowDownward,
                    iconColor = Color(0xFFFF5252), // Red
                    modifier = Modifier.weight(1f),
                    formatter = formatter
                )
            }
        }
    }
}

@Composable
private fun BalanceItem(
    label: String,
    amount: Double,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier,
    formatter: NumberFormat
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
            Text(
                text = formatter.format(amount),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
