package com.kelompok.duidtracker.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.kelompok.duidtracker.viewmodel.AuthViewModel

/**
 * Layar Profil Pengguna.
 * Sesuai persyaratan Step F6.1:
 * - Avatar dengan inisial nama.
 * - Info nama dan email.
 * - Pengaturan (Notifikasi, Tentang, Privasi).
 * - Tombol Logout dengan konfirmasi.
 */
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    // Mengambil data user langsung dari Firebase Auth untuk sementara
    // Idealnya data ini ada di AuthUiState dari Firestore
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userName = currentUser?.displayName ?: "User"
    val userEmail = currentUser?.email ?: "email@example.com"
    val initial = userName.take(1).uppercase()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Avatar Circle
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color(0xFF1565C0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Info
        Text(
            text = userName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = userEmail,
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider()

        // Settings Menu
        ListItem(
            headlineContent = { Text("Notifikasi") },
            leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        )

        ListItem(
            headlineContent = { Text("Tentang Aplikasi") },
            leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                TextButton(onClick = { showAboutDialog = true }) {
                    Text("Lihat")
                }
            }
        )

        ListItem(
            headlineContent = { Text("Privasi & Keamanan") },
            leadingContent = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            trailingContent = {
                TextButton(onClick = { showPrivacyDialog = true }) {
                    Text("Detail")
                }
            }
        )

        HorizontalDivider()
        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
        ) {
            Text("Keluar dari Akun")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Dialogs
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Keluar") },
            text = { Text("Apakah Anda yakin ingin keluar dari akun ini?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("Tentang DuidTracker") },
            text = { Text("DuidTracker v1.0.0\nAplikasi pencatat keuangan cerdas untuk pribadi dan grup.\n\nDikembangkan oleh Kelompok DuidTracker.") },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) { Text("Tutup") }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privasi & Keamanan") },
            text = { Text("Data Anda disimpan dengan aman di Google Firebase. Kami tidak membagikan data keuangan Anda kepada pihak ketiga manapun.") },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text("Mengerti") }
            }
        )
    }
}
