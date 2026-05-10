package com.kelompok.duidtracker.ui

import androidx.compose.foundation.layout.Box // Wadah untuk menumpuk elemen atau mengatur posisi tunggal
import androidx.compose.foundation.layout.Column // Wadah untuk menyusun elemen secara vertikal (atas ke bawah)
import androidx.compose.foundation.layout.Spacer // Elemen kosong untuk memberikan jarak antar komponen
import androidx.compose.foundation.layout.fillMaxSize // Perintah agar komponen mengisi seluruh ukuran layar yang tersedia
import androidx.compose.foundation.layout.height // Mengatur tinggi spesifik sebuah komponen
import androidx.compose.foundation.layout.padding // Memberikan jarak dalam (margin internal) agar konten tidak mepet ke pinggir
import androidx.compose.material.icons.Icons // Koleksi ikon standar Material Design
import androidx.compose.material.icons.filled.AccountBalanceWallet // Ikon dompet untuk menu Pribadi
import androidx.compose.material.icons.filled.Group // Ikon grup untuk menu Kelompok
import androidx.compose.material.icons.filled.Person // Ikon orang untuk menu Profil
import androidx.compose.material3.* // Komponen UI Material Design 3 (Scaffold, Button, Text, dll)
import androidx.compose.runtime.* // Library inti untuk mengelola state/data yang bisa berubah di layar
import androidx.compose.ui.Alignment // Mengatur posisi perataan (tengah, kiri, kanan)
import androidx.compose.ui.Modifier // Alat untuk memodifikasi tampilan, ukuran, dan perilaku komponen
import androidx.compose.ui.unit.dp // Satuan ukuran standar Android (Density-independent Pixels)
import com.kelompok.duidtracker.viewmodel.AuthViewModel // Otak yang mengelola status autentikasi

/**
 * Kelas untuk mendefinisikan item-item di bilah navigasi bawah.
 * Analogi: Seperti label pada rak buku agar kita tahu bagian mana yang sedang kita buka.
 */
sealed class BottomNavScreen(
    val route: String, // Alamat teknis untuk sistem navigasi
    val title: String, // Nama yang muncul di bawah ikon (Bahasa Indonesia)
    val icon: androidx.compose.ui.graphics.vector.ImageVector // Gambar ikon menunya
) {
    object Personal : BottomNavScreen("personal", "Pribadi", Icons.Default.AccountBalanceWallet)
    object Groups : BottomNavScreen("groups", "Grup", Icons.Default.Group)
    object Profile : BottomNavScreen("profile", "Profil", Icons.Default.Person)
}

/**
 * Layar Utama yang menampung navigasi bawah dan konten dinamis.
 * Analogi: Seperti "Dashboard Mobil" yang punya beberapa tab menu di layarnya.
 */
@Composable
fun MainScreen(
    authViewModel: AuthViewModel, // Digunakan untuk aksi logout di tab profil
    onNavigateToGroupDetail: (String) -> Unit, // Fungsi untuk berpindah ke detail grup
    onLogout: () -> Unit // Fungsi untuk menendang user kembali ke layar login
) {
    // Menyimpan status menu mana yang sedang dipilih oleh pengguna (Default: Pribadi)
    // Analogi: Seperti mengingat posisi gigi persneling mobil.
    var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Personal) }

    // Struktur dasar halaman Material3 yang menyediakan slot untuk Bar Atas, Bar Bawah, dll.
    Scaffold(
        bottomBar = {
            // Bilah navigasi yang menempel di bawah layar
            NavigationBar {
                // Daftar menu yang ingin ditampilkan
                val items = listOf(
                    BottomNavScreen.Personal,
                    BottomNavScreen.Groups,
                    BottomNavScreen.Profile
                )
                // Melakukan perulangan untuk membuat setiap item menu secara otomatis
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) }, // Menampilkan ikon
                        label = { Text(screen.title) }, // Menampilkan teks di bawah ikon
                        selected = selectedScreen == screen, // Menandai menu jika sedang aktif (berubah warna)
                        onClick = { selectedScreen = screen } // Mengubah menu yang aktif saat diklik
                    )
                }
            }
        }
    ) { innerPadding ->
        // Konten utama aplikasi yang berubah-ubah sesuai menu yang dipilih
        // padding(innerPadding) memastikan konten tidak tertutup oleh Navigasi Bawah
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center // Selalu letakkan konten di tengah layar
        ) {
            // Logika "Switch": Menampilkan layar yang berbeda berdasarkan menu yang diklik
            when (selectedScreen) {
                is BottomNavScreen.Personal -> {
                    // Layar untuk mencatat pengeluaran pribadi
                    Text("Layar Pribadi: Segera Hadir")
                }
                is BottomNavScreen.Groups -> {
                    // Layar untuk manajemen keuangan kelompok
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Layar Grup: Segera Hadir")
                        Spacer(modifier = Modifier.height(16.dp))
                        // Tombol simulasi untuk mencoba navigasi ke detail grup
                        Button(onClick = { onNavigateToGroupDetail("sample_group_id") }) {
                            Text("Ke Detail Grup Contoh")
                        }
                    }
                }
                is BottomNavScreen.Profile -> {
                    // Layar untuk pengaturan akun dan logout
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Layar Profil: Segera Hadir")
                        Spacer(modifier = Modifier.height(16.dp))
                        // Tombol untuk keluar dari aplikasi
                        Button(onClick = {
                            authViewModel.logout() // Hapus sesi di Firebase
                            onLogout() // Pindah ke layar Login
                        }) {
                            Text("Keluar")
                        }
                    }
                }
            }
        }
    }
}
