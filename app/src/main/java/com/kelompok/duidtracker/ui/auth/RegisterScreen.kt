package com.kelompok.duidtracker.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Layar Pendaftaran untuk membuat akun baru di DuitTracker.
 * Redesigned with professional 60/30/10 color rule.
 */
@Composable
fun RegisterScreen(
    viewModel: AuthViewModel, // Menghubungkan UI dengan logika pendaftaran di ViewModel
    onRegisterSuccess: () -> Unit, // Aksi yang dipanggil saat pendaftaran berhasil
    onNavigateToLogin: () -> Unit // Aksi untuk kembali ke layar masuk
) {
    // Definisi sistem warna 60/30/10 (Neutral/Primary/Accent)
    val baseColor = Color(0xFFF5F5F5)    // 60% - Neutral Background
    val primaryColor = Color(0xFF1565C0) // 30% - Deep Blue Primary
    val accentColor = Color(0xFF00BFA5)  // 10% - Teal Accent

    // Mengamati perubahan status pendaftaran dari ViewModel secara real-time
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Variabel state untuk menyimpan input teks pengguna
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    // Efek otomatis: Navigasi saat pendaftaran berhasil
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onRegisterSuccess()
        }
    }

    // Layout utama kolom yang mendukung scroll (Penting karena banyak field input)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(baseColor)
            .verticalScroll(rememberScrollState()),
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

                Text(
                    text = "DuitTracker",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )

                Text(
                    text = "Mulai perjalanan finansialmu di sini",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        // --- MAIN CARD (Form Pendaftaran) ---
        Card(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = (-32).dp) // Efek menumpuk di atas header biru
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Buat Akun Baru",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = primaryColor
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input Nama Lengkap
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    placeholder = { Text("Masukkan nama lengkap Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
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

                // Input Kata Sandi
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it 
                        localError = null
                    },
                    label = { Text("Kata Sandi") },
                    placeholder = { Text("Buat kata sandi Anda") },
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
                        localError = null
                    },
                    label = { Text("Konfirmasi Kata Sandi") },
                    placeholder = { Text("Ulangi kata sandi Anda") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )

                // Area Pesan Error
                val errorMessage = localError ?: uiState.errorMessage
                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Daftar
                Button(
                    onClick = {
                        localError = null
                        if (password.length < 6) {
                            localError = "Password minimal 6 karakter"
                        } else if (password != confirmPassword) {
                            localError = "Password tidak cocok"
                        } else {
                            viewModel.register(name, email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    enabled = !uiState.isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Daftar",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // --- FOOTER (Navigasi ke Login) ---
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    append("Sudah punya akun? ")
                    withStyle(style = SpanStyle(color = accentColor, fontWeight = FontWeight.Bold)) {
                        append("Masuk")
                    }
                },
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
