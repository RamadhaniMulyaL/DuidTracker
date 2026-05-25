package com.kelompok.duidtracker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kelompok.duidtracker.viewmodel.AuthViewModel

/**
 * Layar Login untuk autentikasi pengguna ke aplikasi DuitTracker.
 * Redesigned with professional 60/30/10 color rule.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel, // Menghubungkan UI dengan logika bisnis di ViewModel
    onLoginSuccess: () -> Unit, // Callback saat login berhasil
    onNavigateToRegister: () -> Unit // Callback untuk pindah ke layar pendaftaran
) {
    // Definisi sistem warna 60/30/10 (Neutral/Primary/Accent)
    val baseColor = Color(0xFFF5F5F5)    // 60% - Neutral Background
    val primaryColor = Color(0xFF1565C0) // 30% - Deep Blue Primary
    val accentColor = Color(0xFF00BFA5)  // 10% - Teal Accent

    // Mengambil state UI dari ViewModel secara reaktif
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Variabel untuk menyimpan teks input (Logika tetap sama)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Efek samping untuk navigasi sukses
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    // Layout utama dengan background solid baseColor
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // --- TOP SECTION (Header Biru 30%) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    color = primaryColor,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo Inisial Bulat
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(color = accentColor, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DT",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Nama Aplikasi
                Text(
                    text = "DuitTracker",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                
                // Slogan aplikasi
                Text(
                    text = "Kelola keuanganmu dengan cerdas",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        // --- MAIN CARD (Form Input) ---
        Card(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-32).dp) // Efek menumpuk di atas header
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Judul di dalam Card
                Text(
                    text = "Masuk ke DuitTracker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Field Input Email
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    placeholder = { Text("Masukkan email Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field Input Kata Sandi
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Kata Sandi") },
                    placeholder = { Text("Masukkan kata sandi Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Color.Gray)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                // Area Pesan Kesalahan
                uiState.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Masuk
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Masuk",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Penyeimbang agar footer berada di bawah
        Spacer(modifier = Modifier.weight(1f))

        // --- FOOTER (Navigasi ke Daftar) ---
        TextButton(
            onClick = onNavigateToRegister,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Belum punya akun? ")
                    withStyle(style = SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                        append("Daftar")
                    }
                },
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
