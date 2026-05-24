package com.kelompok.duidtracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.kelompok.duidtracker.ui.group.GroupListScreen
import com.kelompok.duidtracker.ui.personal.PersonalScreen
import com.kelompok.duidtracker.ui.profile.ProfileScreen
import com.kelompok.duidtracker.viewmodel.AuthViewModel
import com.kelompok.duidtracker.viewmodel.GroupViewModel
import com.kelompok.duidtracker.viewmodel.TransactionViewModel

/**
 * Layar Utama dengan Bottom Navigation yang sudah terintegrasi.
 */
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    groupViewModel: GroupViewModel,
    onNavigateToGroupDetail: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Pribadi", "Grup", "Profil")
    val icons = listOf(
        Icons.Default.AccountBalanceWallet,
        Icons.Default.Group,
        Icons.Default.Person
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title) },
                        icon = { Icon(icons[index], contentDescription = title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> PersonalScreen(viewModel = transactionViewModel)
                1 -> GroupListScreen(
                    viewModel = groupViewModel,
                    onGroupClick = onNavigateToGroupDetail
                )
                2 -> ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = onLogout
                )
            }
        }
    }
}
