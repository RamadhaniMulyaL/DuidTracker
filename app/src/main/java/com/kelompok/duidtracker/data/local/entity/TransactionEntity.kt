package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo // Digunakan untuk memberikan nama kolom kustom di database
import androidx.room.Entity // Menandai class ini sebagai tabel di database Room
import androidx.room.PrimaryKey // Menandai variabel sebagai kunci utama unik
import java.text.DecimalFormat // Alat untuk memformat angka menjadi teks rapi
import java.text.DecimalFormatSymbols // Mengatur simbol (seperti titik/koma) untuk format angka
import java.util.Locale // Mengatur wilayah (lokasi) untuk standar format (Indonesia)

/**
 * Representasi tabel "transactions" di database lokal.
 * Analogi: Ini adalah "Formulir Catatan Keuangan" yang harus diisi setiap ada uang masuk/keluar.
 */
@Entity(tableName = "transactions") // Mendefinisikan nama tabel di database SQL
data class TransactionEntity(
    @PrimaryKey // Setiap transaksi harus punya ID unik agar tidak tertukar (seperti nomor struk)
    @ColumnInfo(name = "id") // Nama kolom di database: id
    val id: String, // Menggunakan UUID (String unik) sebagai identitas transaksi

    @ColumnInfo(name = "user_id") // Nama kolom di database: user_id
    val userId: String, // ID pemilik transaksi agar data tidak campur dengan user lain

    @ColumnInfo(name = "group_id") // Nama kolom di database: group_id
    val groupId: String?, // ID grup jika transaksi dilakukan bersama (nullable/boleh kosong jika pribadi)

    @ColumnInfo(name = "type") // Nama kolom di database: type
    val type: String, // Tipe transaksi: "income" (masuk) atau "expense" (keluar)

    @ColumnInfo(name = "nama") // Nama kolom di database: nama
    val nama: String, // Keterangan atau nama transaksi (misal: "Beli Bakso")

    @ColumnInfo(name = "nominal") // Nama kolom di database: nominal
    val nominal: Double, // Jumlah uang dalam angka desimal

    @ColumnInfo(name = "kategori") // Nama kolom di database: kategori
    val kategori: String, // Kategori transaksi (misal: "Makanan", "Transportasi")

    @ColumnInfo(name = "tanggal") // Nama kolom di database: tanggal
    val tanggal: Long, // Waktu transaksi terjadi dalam format milidetik (epoch time)

    @ColumnInfo(name = "created_at") // Nama kolom di database: created_at
    val createdAt: Long // Waktu kapan catatan ini dibuat di aplikasi
) {
    // Objek pendamping untuk menyimpan nilai tetap (konstanta) agar tidak salah ketik
    companion object {
        const val TYPE_INCOME = "income" // Penanda untuk uang masuk
        const val TYPE_EXPENSE = "expense" // Penanda untuk uang keluar
    }
}

/**
 * Properti tambahan untuk mengubah angka nominal menjadi format Rupiah yang cantik.
 * Analogi: Seperti mesin kasir yang mengubah angka "5000" menjadi "Rp 5.000".
 */
val TransactionEntity.toFormattedNominal: String
    get() {
        // Mengatur standar wilayah ke Indonesia
        val localeID = Locale("in", "ID")
        
        // Mengatur simbol ribuan menggunakan titik (.) dan desimal menggunakan koma (,) sesuai standar ID
        val symbols = DecimalFormatSymbols(localeID).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        
        // Membuat pola format: #,### (angka dengan pemisah ribuan)
        val decimalFormat = DecimalFormat("#,###", symbols)
        
        // Mengembalikan teks final dengan awalan "Rp "
        return "Rp ${decimalFormat.format(nominal)}"
    }
