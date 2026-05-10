package com.kelompok.duidtracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kelompok.duidtracker.viewmodel.AuthViewModel

sealed class BottomNavScreen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Personal : BottomNavScreen("personal", "Pribadi", Icons.Default.AccountBalanceWallet)
    object Groups : BottomNavScreen("groups", "Grup", Icons.Default.Group)
    object Profile : BottomNavScreen("profile", "Profil", Icons.Default.Person)
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onNavigateToGroupDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedScreen by remember { mutableStateOf<BottomNavScreen>(BottomNavScreen.Personal) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    BottomNavScreen.Personal,
                    BottomNavScreen.Groups,
                    BottomNavScreen.Profile
                )
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = selectedScreen == screen,
                        onClick = { selectedScreen = screen }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (selectedScreen) {
                is BottomNavScreen.Personal -> Text("Layar Pribadi: Segera Hadir")
                is BottomNavScreen.Groups -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Layar Grup: Segera Hadir")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onNavigateToGroupDetail("sample_group_id") }) {
                            Text("Ke Detail Grup Contoh")
                        }
                    }
                }
                is BottomNavScreen.Profile -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Layar Profil: Segera Hadir")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onLogout) {
                            Text("Keluar")
                        }
                    }
                }
            }
        }
    }
}
