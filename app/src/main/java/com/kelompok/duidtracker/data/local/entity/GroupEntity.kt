package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo // Digunakan untuk memberikan nama kolom khusus di database agar rapi (snake_case)
import androidx.room.Entity // Menandai class ini sebagai tabel database permanen di dalam handphone
import androidx.room.PrimaryKey // Menentukan identitas unik untuk setiap baris data agar tidak tertukar
import com.google.gson.Gson // Library untuk mengubah daftar objek menjadi satu baris teks (JSON)
import com.google.gson.reflect.TypeToken // Membantu Gson mengenali tipe data List saat proses pembongkaran teks
import java.text.DecimalFormat // Alat untuk merapikan angka menjadi format mata uang
import java.text.DecimalFormatSymbols // Mengatur simbol pemisah ribuan (titik) sesuai standar Indonesia
import java.util.Locale // Mengatur wilayah (Indonesia) untuk standar format angka dan bahasa

/**
 * Representasi tabel "groups" di database lokal Room.
 * Analogi: Ini adalah "Buku Induk Kelompok" yang mencatat siapa saja anggota kelompok patungan belanja.
 */
@Entity(tableName = "groups") // Memberitahu Room untuk membuat tabel bernama "groups" di SQL
data class GroupEntity(
    @PrimaryKey // Setiap grup wajib punya ID unik (seperti nomor sertifikat pendirian)
    @ColumnInfo(name = "id") // Nama kolom di database adalah 'id'
    val id: String, // Menggunakan kode unik (UUID) sebagai identitas grup

    @ColumnInfo(name = "name") // Nama kolom: name
    val name: String, // Nama kelompok (misal: "Kost Ceria" atau "Keluarga")

    @ColumnInfo(name = "description") // Nama kolom: description
    val description: String, // Keterangan singkat mengenai tujuan kelompok tersebut

    @ColumnInfo(name = "owner_id") // Nama kolom: owner_id
    val ownerId: String, // ID pengguna yang menciptakan grup (bertindak sebagai Ketua)

    @ColumnInfo(name = "members_json") // Nama kolom: members_json
    val membersJson: String, // Daftar ID anggota yang dipadatkan jadi satu baris teks panjang (format JSON)

    @ColumnInfo(name = "budget") // Nama kolom: budget
    val budget: Double, // Batas maksimal pengeluaran uang bersama (Anggaran)

    @ColumnInfo(name = "icon") // Nama kolom: icon
    val icon: String, // Nama atau alamat gambar ikon untuk visual kelompok

    @ColumnInfo(name = "invite_code") // Nama kolom: invite_code
    val inviteCode: String, // Kode unik 6 karakter untuk mengajak orang lain bergabung

    @ColumnInfo(name = "created_at") // Nama kolom: created_at
    val createdAt: Long // Waktu kapan grup ini didaftarkan (dalam format milidetik)
) {
    /**
     * Membongkar teks JSON di database kembali menjadi daftar (List) ID pengguna asli.
     * Analogi: Seperti membuka kardus paket (JSON) untuk mengambil isinya (Daftar Nama Anggota).
     */
    fun getMembersList(): List<String> {
        return try {
            // Langkah 1: Mendefinisikan bahwa hasil bongkaran harus berbentuk List berisi String
            val type = object : TypeToken<List<String>>() {}.type
            // Langkah 2: Menggunakan Gson untuk mengubah teks kembali menjadi daftar nama asli
            Gson().fromJson(membersJson, type) ?: emptyList()
        } catch (e: Exception) {
            // Langkah 3: Jika data teks rusak, berikan list kosong agar aplikasi tidak berhenti mendadak
            emptyList()
        }
    }

    /**
     * Mengecek apakah seorang pengguna adalah anggota resmi dari kelompok ini.
     * Analogi: Satpam yang mengecek apakah nama Anda ada di dalam daftar tamu grup.
     */
    fun isMember(userId: String): Boolean {
        // Memeriksa apakah ID user yang dicari ada di dalam daftar anggota hasil bongkar JSON
        return getMembersList().contains(userId)
    }
}

/**
 * Fungsi tambahan (Extension) untuk mengubah angka anggaran menjadi format Rupiah yang rapi.
 * Analogi: Seperti label harga di supermarket yang mengubah angka "50000" menjadi "Rp 50.000".
 */
val GroupEntity.toFormattedBudget: String
    get() {
        // Langkah 1: Mengatur wilayah ke Indonesia (ID)
        val localeID = Locale.forLanguageTag("id-ID")
        
        // Langkah 2: Mengatur simbol titik (.) untuk ribuan sesuai standar Indonesia
        val symbols = DecimalFormatSymbols(localeID).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        
        // Langkah 3: Membuat pola format angka dengan pemisah ribuan (#,###)
        val decimalFormat = DecimalFormat("#,###", symbols)
        
        // Langkah 4: Mengembalikan hasil dalam bentuk teks dengan awalan "Rp "
        return "Rp ${decimalFormat.format(budget)}"
    }
