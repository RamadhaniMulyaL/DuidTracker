package com.kelompok.duidtracker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok.duidtracker.viewmodel.AuthViewModel

/**
 * Layar Pendaftaran untuk membuat akun baru di DuitTracker.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel, // Menghubungkan UI dengan logika pendaftaran di ViewModel
    onRegisterSuccess: () -> Unit, // Aksi yang dipanggil saat pendaftaran berhasil
    onNavigateToLogin: () -> Unit // Aksi untuk kembali ke layar masuk
) {
    // Mengamati perubahan status pendaftaran dari ViewModel secara real-time
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Variabel state untuk menyimpan input teks pengguna di layar
    var name by remember { mutableStateOf("") } // Menyimpan nama lengkap
    var email by remember { mutableStateOf("") } // Menyimpan alamat email
    var password by remember { mutableStateOf("") } // Menyimpan kata sandi
    var confirmPassword by remember { mutableStateOf("") } // Menyimpan konfirmasi kata sandi
    
    // Status untuk menampilkan atau menyembunyikan teks kata sandi
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Variabel untuk menyimpan pesan kesalahan validasi lokal (di HP)
    var localError by remember { mutableStateOf<String?>(null) }

    // Efek otomatis: Jika status sukses di ViewModel berubah jadi true, langsung pindah layar
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess() // Navigasi ke Home
        }
    }

    // Layout utama kolom yang bisa digulung (scrollable) jika layar penuh
    Column(
        modifier = Modifier
            .fillMaxSize() // Memenuhi layar
            .padding(24.dp) // Jarak tepi 24dp
            .verticalScroll(rememberScrollState()), // Mengaktifkan fungsi gulung (scroll)
        horizontalAlignment = Alignment.CenterHorizontally, // Konten di tengah secara horizontal
        verticalArrangement = Arrangement.Center // Konten di tengah secara vertikal
    ) {
        // Logo Aplikasi (Kotak Biru dengan Teks Putih)
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Duit\nTracker",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Judul Layar
        Text(
            text = "Buat Akun Baru",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Nama Lengkap
        OutlinedTextField(
            value = name,
            onValueChange = { name = it }, // Update variabel name saat diketik
            label = { Text("Nama Lengkap") },
            placeholder = { Text("Masukkan nama lengkap Anda") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text, // Keyboard teks biasa
                imeAction = ImeAction.Next // Tombol enter jadi 'Lanjut'
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("Masukkan email Anda") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, // Keyboard dengan tombol @
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Kata Sandi
        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it 
                localError = null // Hapus pesan error saat user mulai mengetik ulang
            },
            label = { Text("Kata Sandi") },
            placeholder = { Text("Buat kata sandi Anda") },
            modifier = Modifier.fillMaxWidth(),
            // Masking teks jika passwordVisible false
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { // Ikon mata di kanan
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Konfirmasi Kata Sandi
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { 
                confirmPassword = it 
                localError = null // Hapus pesan error saat user mulai mengetik ulang
            },
            label = { Text("Konfirmasi Kata Sandi") },
            placeholder = { Text("Ulangi kata sandi Anda") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done // Tombol enter jadi 'Selesai'
            ),
            singleLine = true
        )

        // Logika Penampilan Pesan Error (Gabungan error lokal HP dan error server Firebase)
        val errorMessage = localError ?: uiState.errorMessage
        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Daftar
        Button(
            onClick = {
                localError = null // Reset error sebelum cek validasi
                if (password.length < 6) {
                    localError = "Password minimal 6 karakter" // Cek panjang pass (Analogi: Syarat kekuatan kunci)
                } else if (password != confirmPassword) {
                    localError = "Password tidak cocok" // Cek kesamaan pass (Analogi: Konfirmasi kunci)
                } else {
                    viewModel.register(name, email, password) // Kirim data ke ViewModel untuk diproses
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            // Tombol aktif hanya jika semua field sudah diisi
            enabled = !uiState.isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
        ) {
            if (uiState.isLoading) {
                // Tampilkan loading spinner jika sedang mendaftar ke server
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Daftar")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigasi kembali ke Login
        TextButton(onClick = onNavigateToLogin) {
            Text("Sudah punya akun? Masuk")
        }
    }
}
