package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo // Digunakan untuk penamaan kolom kustom di database Room
import androidx.room.Entity // Menandai class ini sebagai tabel database
import androidx.room.PrimaryKey // Menentukan kunci utama unik untuk setiap baris
import com.google.gson.Gson // Library untuk mengubah objek menjadi teks JSON dan sebaliknya
import com.google.gson.reflect.TypeToken // Digunakan untuk mempertahankan informasi tipe data saat parsing JSON

/**
 * Representasi tabel "groups" di database lokal.
 * Analogi: Ini adalah "Buku Induk Kelompok" yang mencatat siapa saja anggota kelompok patungan.
 */
@Entity(tableName = "groups") // Memberi nama tabel "groups" di SQL
data class GroupEntity(
    @PrimaryKey // Setiap grup harus punya ID unik (misal: ID dari Firestore)
    @ColumnInfo(name = "id")
    val id: String, // Identitas unik grup

    @ColumnInfo(name = "name")
    val name: String, // Nama grup (misal: "Kontrakan Ceria")

    @ColumnInfo(name = "description")
    val description: String, // Penjelasan singkat tentang grup

    @ColumnInfo(name = "owner_id")
    val ownerId: String, // ID pengguna yang membuat grup (ketua)

    @ColumnInfo(name = "members_json")
    val membersJson: String, // Daftar ID anggota yang disimpan sebagai teks panjang (format JSON)

    @ColumnInfo(name = "budget")
    val budget: Double, // Batas maksimal pengeluaran grup (anggaran)

    @ColumnInfo(name = "icon")
    val icon: String, // Nama atau URL ikon untuk visual grup

    @ColumnInfo(name = "invite_code")
    val inviteCode: String, // Kode unik untuk mengajak orang lain bergabung

    @ColumnInfo(name = "created_at")
    val createdAt: Long // Waktu kapan grup ini didaftarkan
) {
    /**
     * Mengubah teks JSON di database kembali menjadi daftar (List) ID pengguna.
     * Analogi: Seperti membongkar kardus (JSON) untuk mengambil isinya (Daftar Nama).
     */
    fun getMembersList(): List<String> {
        return try {
            // Memberitahu Gson bahwa kita ingin hasil berupa List berisi String
            val type = object : TypeToken<List<String>>() {}.type
            // Melakukan konversi teks ke List, jika gagal kembalikan list kosong
            Gson().fromJson(membersJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList() // Jika terjadi error format, anggap tidak ada anggota
        }
    }

    /**
     * Mengecek apakah seorang pengguna adalah bagian dari grup ini.
     * Analogi: Satpam yang mengecek apakah nama Anda ada di daftar tamu sebelum boleh masuk.
     */
    fun isMember(userId: String): Boolean {
        // Memeriksa apakah ID user yang dicari ada di dalam daftar anggota hasil bongkar JSON
        return getMembersList().contains(userId)
    }
}
