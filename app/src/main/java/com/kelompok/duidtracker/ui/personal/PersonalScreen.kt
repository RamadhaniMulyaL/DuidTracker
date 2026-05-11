package com.kelompok.duidtracker.ui.personal

import androidx.compose.foundation.background // Mengatur warna latar belakang komponen
import androidx.compose.foundation.layout.* // Mengatur tata letak (layout) seperti Box, Column, Row
import androidx.compose.foundation.lazy.LazyColumn // List efisien yang hanya merender item yang terlihat di layar
import androidx.compose.foundation.lazy.items // Membantu memasukkan daftar data ke dalam LazyColumn
import androidx.compose.foundation.shape.CircleShape // Memberikan bentuk lingkaran pada komponen
import androidx.compose.foundation.shape.RoundedCornerShape // Memberikan sudut membulat pada komponen
import androidx.compose.material.icons.Icons // Koleksi ikon standar Material
import androidx.compose.material.icons.filled.Add // Ikon tanda tambah
import androidx.compose.material.icons.filled.ArrowDownward // Ikon panah bawah (untuk pengeluaran)
import androidx.compose.material.icons.filled.ArrowUpward // Ikon panah atas (untuk pemasukan)
import androidx.compose.material.icons.filled.Delete // Ikon tempat sampah untuk hapus
import androidx.compose.material3.* // Komponen UI Material Design 3
import androidx.compose.runtime.* // Mengelola state dan variabel yang bisa berubah di Compose
import androidx.compose.ui.Alignment // Mengatur posisi elemen (tengah, kiri, kanan)
import androidx.compose.ui.Modifier // Alat pemodifikasi tampilan (ukuran, warna, klik, dll)
import androidx.compose.ui.graphics.Color // Definisi warna
import androidx.compose.ui.graphics.vector.ImageVector // Tipe data untuk gambar ikon vektor
import androidx.compose.ui.text.font.FontWeight // Mengatur ketebalan huruf (bold, dll)
import androidx.compose.ui.unit.dp // Satuan ukuran piksel (Density-independent Pixels)
import androidx.compose.ui.unit.sp // Satuan ukuran teks (Scale-independent Pixels)
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Mengamati data dari ViewModel secara aman terhadap lifecycle
import com.kelompok.duidtracker.data.local.entity.TransactionEntity // Formulir data transaksi
import com.kelompok.duidtracker.data.local.entity.toFormattedNominal // Fungsi pengubah angka ke Rupiah
import com.kelompok.duidtracker.viewmodel.TransactionViewModel // Otak pengelola data transaksi

/**
 * Layar Utama untuk mengelola Transaksi Pribadi.
 * Analogi: Ini adalah "Buku Kas Pribadi" digital Anda.
 */
@Composable
fun PersonalScreen(
    viewModel: TransactionViewModel // Menerima otak (ViewModel) untuk mengolah data
) {
    // 1. Mengamati status data dari ViewModel (List transaksi, total saldo, dll)
    // Alur: Jika ada data baru di database, variabel uiState ini akan otomatis ter-update.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 2. Variabel untuk menentukan apakah jendela tambah transaksi muncul atau tidak
    var showAddDialog by remember { mutableStateOf(false) }

    // 3. Struktur dasar halaman Material3
    Scaffold(
        floatingActionButton = {
            // Tombol Melayang (FAB) di pojok bawah untuk menambah data
            FloatingActionButton(
                onClick = { showAddDialog = true }, // Munculkan dialog saat diklik
                containerColor = Color(0xFF1565C0), // Warna biru utama
                contentColor = Color.White // Warna ikon putih
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi")
            }
        }
    ) { padding ->
        // 4. Layout kolom utama untuk menyusun konten dari atas ke bawah
        Column(
            modifier = Modifier
                .fillMaxSize() // Penuhi layar
                .background(Color(0xFFF5F5F5)) // Background abu-abu muda (Neutral 60%)
                .padding(padding) // Hindari konten tertutup oleh Bar Navigasi/Sistem
        ) {
            // 5. Bagian Ringkasan Saldo (Analogi: Dashboard dompet Anda)
            SummarySection(
                income = uiState.totalIncome, // Ambil total pemasukan dari state
                expense = uiState.totalExpense // Ambil total pengeluaran dari state
            )

            // 6. Judul untuk daftar riwayat
            Text(
                text = "Transaksi Terakhir",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(16.dp)
            )

            // 7. Logika penampilan daftar transaksi
            if (uiState.transactions.isEmpty()) {
                // Tampilan jika belum pernah mencatat apapun
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi. Yuk catat sekarang!", color = Color.Gray)
                }
            } else {
                // List efisien untuk menampilkan banyak data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Beri spasi di bawah agar tidak tertutup tombol melayang
                ) {
                    // Loop setiap data transaksi di dalam list
                    items(uiState.transactions) { transaction ->
                        // Tampilkan komponen item untuk masing-masing transaksi
                        TransactionItem(
                            transaction = transaction,
                            onDelete = { viewModel.deleteTransaction(transaction) } // Fungsi hapus
                        )
                    }
                }
            }
        }
    }

    // 8. Logika penayangan Jendela Tambah Data (Dialog)
    if (showAddDialog) {
        AddTransactionDialog(
            onDismiss = { showAddDialog = false }, // Tutup dialog jika batal
            onConfirm = { nama, nominal, kategori, tipe ->
                // Kirim data baru ke ViewModel untuk disimpan
                viewModel.addTransaction(nama, nominal, kategori, tipe)
                showAddDialog = false // Tutup dialog setelah simpan
            }
        )
    }
}

/**
 * Komponen Kartu Ringkasan (Kartu Saldo Biru).
 * Analogi: Seperti melihat layar ATM untuk tahu sisa saldo Anda.
 */
@Composable
fun SummarySection(income: Double, expense: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp), // Sudut membulat modern
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)) // Warna biru (Primary 30%)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Judul kecil
            Text("Total Saldo", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            // Hasil pengurangan Pemasukan - Pengeluaran
            Text(
                text = "Rp ${formatNominal(income - expense)}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Baris untuk menampilkan rincian Masuk & Keluar secara berdampingan
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryItem(
                    label = "Pemasukan",
                    amount = income,
                    icon = Icons.Default.ArrowUpward,
                    iconColor = Color(0xFF00BFA5), // Aksen Hijau/Teal (Accent 10%)
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Pengeluaran",
                    amount = expense,
                    icon = Icons.Default.ArrowDownward,
                    iconColor = Color(0xFFFF5252), // Merah peringatan
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Komponen kecil untuk menampilkan angka Pemasukan atau Pengeluaran.
 */
@Composable
fun SummaryItem(label: String, amount: Double, icon: ImageVector, iconColor: Color, modifier: Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        // Ikon kecil dengan latar belakang transparan
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text("Rp ${formatNominal(amount)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Baris tunggal untuk satu catatan transaksi.
 * Analogi: Satu lembar struk belanja dalam daftar riwayat.
 */
@Composable
fun TransactionItem(transaction: TransactionEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp) // Efek bayangan tipis
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Ikon Penanda: Panah Atas (Ijo) atau Panah Bawah (Merah)
            val isIncome = transaction.type == TransactionEntity.TYPE_INCOME
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isIncome) Color(0xFFE0F2F1) else Color(0xFFFFEBEE),
                        shape = CircleShape // Bentuk bulat sempurna
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isIncome) Color(0xFF00BFA5) else Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 2. Keterangan Nama & Kategori
            Column(modifier = Modifier.weight(1f)) {
                Text(transaction.nama, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(transaction.kategori, color = Color.Gray, fontSize = 12.sp)
            }

            // 3. Nominal Uang & Tombol Hapus
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+ " else "- ") + transaction.toFormattedNominal,
                    color = if (isIncome) Color(0xFF00BFA5) else Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.LightGray)
                }
            }
        }
    }
}

/**
 * Dialog Input Transaksi Baru.
 * Analogi: Mesin Kasir di mana Anda memasukkan Nama Barang, Harga, dan Tipe Uang.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String, String) -> Unit) {
    // State lokal untuk menampung ketikan sementara di dalam dialog
    var nama by remember { mutableStateOf("") }
    var nominal by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Umum") }
    var type by remember { mutableStateOf(TransactionEntity.TYPE_EXPENSE) }

    AlertDialog(
        onDismissRequest = onDismiss, // Jika user klik di luar kotak, dialog tutup
        title = { Text("Tambah Transaksi") },
        text = {
            Column {
                // Pilihan Tipe: Pengeluaran atau Pemasukan
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = type == TransactionEntity.TYPE_EXPENSE,
                        onClick = { type = TransactionEntity.TYPE_EXPENSE },
                        label = { Text("Pengeluaran") },
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                    FilterChip(
                        selected = type == TransactionEntity.TYPE_INCOME,
                        onClick = { type = TransactionEntity.TYPE_INCOME },
                        label = { Text("Pemasukan") },
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                }
                // Kotak Input Keterangan (misal: Beli Kopi)
                OutlinedTextField(
                    value = nama, 
                    onValueChange = { nama = it }, 
                    label = { Text("Keterangan") },
                    modifier = Modifier.fillMaxWidth()
                )
                // Kotak Input Nominal Angka
                OutlinedTextField(
                    value = nominal, 
                    onValueChange = { nominal = it }, 
                    label = { Text("Nominal") }, 
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                // Kotak Input Nama Kategori
                OutlinedTextField(
                    value = kategori, 
                    onValueChange = { kategori = it }, 
                    label = { Text("Kategori") }, 
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            // Tombol Simpan
            Button(onClick = { 
                val amount = nominal.toDoubleOrNull() ?: 0.0 // Ubah teks ke angka, jika bukan angka jadi 0.0
                if (nama.isNotBlank() && amount > 0) {
                    onConfirm(nama, amount, kategori, type) // Panggil aksi simpan
                }
            }) { Text("Simpan") }
        },
        dismissButton = { 
            // Tombol Batal
            TextButton(onClick = onDismiss) { Text("Batal") } 
        }
    )
}

/**
 * Fungsi pembantu (Helper) untuk memformat angka saldo menjadi format Indonesia secara manual.
 */
private fun formatNominal(amount: Double): String {
    // Mengatur locale dan simbol pemisah ribuan
    val localeID = java.util.Locale.forLanguageTag("id-ID")
    val symbols = java.text.DecimalFormatSymbols(localeID).apply {
        groupingSeparator = '.' // Pakai titik untuk ribuan
    }
    // Format angka tanpa desimal di belakang
    return java.text.DecimalFormat("#,###", symbols).format(amount)
}
