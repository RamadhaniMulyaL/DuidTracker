package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "group_id")
    val groupId: String?,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "nominal")
    val nominal: Double,

    @ColumnInfo(name = "kategori")
    val kategori: String,

    @ColumnInfo(name = "tanggal")
    val tanggal: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    companion object {
        const val TYPE_INCOME = "income"
        const val TYPE_EXPENSE = "expense"
    }
}

/**
 * Extension property to format the nominal value into a Rupiah string.
 * Example: 1500000.0 -> Rp 1.500.000
 */
val TransactionEntity.toFormattedNominal: String
    get() {
        val localeID = Locale("in", "ID")
        val symbols = DecimalFormatSymbols(localeID).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        return "Rp ${decimalFormat.format(nominal)}"
    }
