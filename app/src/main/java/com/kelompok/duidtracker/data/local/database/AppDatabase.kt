package com.kelompok.duidtracker.data.local.database

import android.content.Context // Digunakan untuk mengakses sistem Android (seperti file storage)
import androidx.room.Database // Menandai class ini sebagai database utama Room
import androidx.room.Room // Alat untuk membangun instance database
import androidx.room.RoomDatabase // Class induk yang harus di-extend oleh database Room
import androidx.room.TypeConverter // Menandai fungsi sebagai pengubah tipe data
import androidx.room.TypeConverters // Menghubungkan database dengan class pengubah tipe data
import com.google.gson.Gson // Library untuk memproses teks JSON
import com.google.gson.reflect.TypeToken // Digunakan untuk menjaga informasi tipe data saat konversi
import com.kelompok.duidtracker.data.local.dao.GroupDao // Menghubungkan pelayan database Grup
import com.kelompok.duidtracker.data.local.dao.TransactionDao // Menghubungkan pelayan database Transaksi
import com.kelompok.duidtracker.data.local.entity.GroupEntity // Menghubungkan tabel Grup
import com.kelompok.duidtracker.data.local.entity.TransactionEntity // Menghubungkan tabel Transaksi

/**
 * Pusat Kendali Database Room untuk aplikasi DuitTracker.
 * Analogi: Ini adalah "Gedung Pusat Arsip" yang mengelola semua lemari data (Tabel).
 */
@Database(
    entities = [TransactionEntity::class, GroupEntity::class], // Mendaftarkan tabel yang ada di dalam gedung ini
    version = 1 // Versi database, harus naik jika ada perubahan struktur tabel
)
@TypeConverters(Converters::class) // Memberitahu database untuk menggunakan class penerjemah data kompleks
abstract class AppDatabase : RoomDatabase() {

    // Mendefinisikan akses ke pelayan database (DAO)
    abstract fun transactionDao(): TransactionDao
    abstract fun groupDao(): GroupDao

    companion object {
        @Volatile // Memastikan perubahan pada variabel ini langsung terlihat di seluruh bagian aplikasi
        private var INSTANCE: AppDatabase? = null // Tempat menyimpan satu-satunya instance gedung database (Singleton)

        /**
         * Fungsi untuk mendapatkan akses ke gedung database.
         * Analogi: Seperti meminta kunci gedung; jika gedung belum dibangun, kita bangun dulu.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Jika instance sudah ada, langsung kembalikan. Jika belum, masuk ke blok pembangunan (synchronized).
            return INSTANCE ?: synchronized(this) {
                // Membangun instance database menggunakan Room builder
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Menggunakan context aplikasi agar tidak memory leak
                    AppDatabase::class.java, // Menunjuk class database ini
                    "duittracker_db" // Nama file database yang disimpan di memori HP
                )
                    .fallbackToDestructiveMigration() // Hapus & buat ulang database jika versi berubah (berguna saat dev)
                    .build() // Selesaikan pembangunan gedung
                INSTANCE = instance // Simpan instance agar bisa dipakai lagi nanti
                instance // Kembalikan hasil pembangunan
            }
        }
    }
}

/**
 * Class Penerjemah (Converters).
 * Analogi: Mesin penghancur dokumen (Object -> JSON) dan mesin penyusun kembali (JSON -> Object).
 */
class Converters {
    /**
     * Mengubah teks JSON dari database kembali menjadi daftar List asli.
     */
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        // Mendefinisikan tipe data tujuan: List dari String
        val listType = object : TypeToken<List<String>>() {}.type
        // Mengubah teks (String) menjadi objek List menggunakan Gson
        return Gson().fromJson(value, listType)
    }

    /**
     * Mengubah daftar List menjadi teks JSON agar bisa disimpan di database.
     */
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        // Mengubah objek List menjadi satu baris teks panjang (String) menggunakan Gson
        return Gson().toJson(list)
    }
}
