package com.kelompok.duidtracker.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kelompok.duidtracker.data.local.dao.GroupDao
import com.kelompok.duidtracker.data.local.dao.TransactionDao
import com.kelompok.duidtracker.data.local.entity.GroupEntity
import com.kelompok.duidtracker.data.local.entity.TransactionEntity

/**
 * Pusat Database Room untuk aplikasi DuidTracker.
 * Sesuai dengan persyaratan Step F1.5:
 * - Singleton pattern dengan @Volatile INSTANCE
 * - Mendukung fallbackToDestructiveMigration
 * - Memiliki TypeConverters untuk List<String>
 */
@Database(
    entities = [TransactionEntity::class, GroupEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun groupDao(): GroupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "duittracker_db"
                )
                    .fallbackToDestructiveMigration() // Menghapus database jika skema berubah saat pengembangan
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/**
 * Kelas konverter untuk menangani tipe data kompleks yang tidak didukung Room secara langsung.
 * Digunakan untuk List<String> sebagai pengaman tambahan.
 */
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return Gson().toJson(list)
    }
}
