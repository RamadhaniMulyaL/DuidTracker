package com.kelompok.duidtracker.data.local.entity

import androidx.room.ColumnInfo // Digunakan untuk memberikan nama kolom kustom di database
import androidx.room.Entity // Menandai class ini sebagai tabel di database Room
import androidx.room.PrimaryKey // Menandai variabel sebagai kunci utama unik
import com.google.gson.Gson // Library untuk mengubah list menjadi teks JSON
import com.google.gson.reflect.TypeToken // Membantu Gson mengenali tipe data List
import java.text.DecimalFormat // Alat untuk merapikan angka menjadi format mata uang
import java.text.DecimalFormatSymbols // Mengatur simbol ribuan (titik)
import java.util.Locale // Mengatur wilayah (Indonesia)

/**
 * Representasi tabel "groups" di database lokal Room.
 * Sesuai dengan persyaratan Step F1.2:
 * - Menggunakan @ColumnInfo snake_case
 * - Memiliki helper methods untuk parsing JSON anggota
 * - Menggunakan Gson untuk konversi data
 */
@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "owner_id")
    val ownerId: String,

    @ColumnInfo(name = "members_json")
    val membersJson: String, // Disimpan sebagai String karena Room tidak dukung List secara langsung

    @ColumnInfo(name = "budget")
    val budget: Double,

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "invite_code")
    val inviteCode: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    /**
     * Membongkar teks JSON members_json menjadi List of User ID.
     */
    fun getMembersList(): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(membersJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Mengecek apakah seorang user adalah anggota grup ini.
     */
    fun isMember(userId: String): Boolean {
        return getMembersList().contains(userId)
    }
}

/**
 * Properti tambahan untuk format anggaran ke Rupiah.
 */
val GroupEntity.toFormattedBudget: String
    get() {
        val localeID = Locale("id", "ID")
        val symbols = DecimalFormatSymbols(localeID).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        return "Rp ${decimalFormat.format(budget)}"
    }
