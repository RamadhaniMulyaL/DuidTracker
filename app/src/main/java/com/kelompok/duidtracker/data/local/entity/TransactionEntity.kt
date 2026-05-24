package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo // Digunakan untuk memberikan nama kolom kustom di database
import androidx.room.Entity // Menandai class ini sebagai tabel di database Room
import androidx.room.PrimaryKey // Menandai variabel sebagai kunci utama unik
import java.text.DecimalFormat // Alat untuk memformat angka menjadi teks rapi
import java.text.DecimalFormatSymbols // Mengatur simbol (seperti titik/koma) untuk format angka
import java.util.Locale // Mengatur wilayah (lokasi) untuk standar format (Indonesia)

/**
 * Representasi tabel "transactions" di database lokal.
 * Sesuai dengan persyaratan:
 * - Menggunakan UUID sebagai ID (String)
 * - Mendukung transaksi personal (groupId = null) dan grup
 * - Memiliki konstanta tipe (Income/Expense)
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String, // UUID, not auto-generate

    @ColumnInfo(name = "user_id")
    val userId: String,

    @ColumnInfo(name = "group_id")
    val groupId: String?, // Nullable, null means personal

    @ColumnInfo(name = "type")
    val type: String, // "income" or "expense"

    @ColumnInfo(name = "nama")
    val nama: String,

    @ColumnInfo(name = "nominal")
    val nominal: Double,

    @ColumnInfo(name = "kategori")
    val kategori: String,

    @ColumnInfo(name = "tanggal")
    val tanggal: Long, // Epoch milliseconds

    @ColumnInfo(name = "created_at")
    val createdAt: Long // Epoch milliseconds
) {
    // Companion object untuk menyimpan konstanta tipe transaksi
    companion object {
        const val TYPE_INCOME = "income"
        const val TYPE_EXPENSE = "expense"
    }
}

/**
 * Properti ekstensi untuk mengubah angka nominal menjadi format Rupiah yang cantik.
 * Contoh: Rp 1.500.000
 */
val TransactionEntity.toFormattedNominal: String
    get() {
        val localeID = Locale("id", "ID")
        val symbols = DecimalFormatSymbols(localeID).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        return "Rp ${decimalFormat.format(nominal)}"
    }
