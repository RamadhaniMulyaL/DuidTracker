package com.kelompok.duidtracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kelompok.duidtracker.ui.auth.LoginScreen
import com.kelompok.duidtracker.ui.auth.RegisterScreen
import com.kelompok.duidtracker.ui.group.GroupDetailScreen
import com.kelompok.duidtracker.viewmodel.AuthViewModel
import com.kelompok.duidtracker.viewmodel.GroupViewModel
import com.kelompok.duidtracker.viewmodel.TransactionViewModel

/**
 * Definisi rute navigasi aplikasi.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }
}

/**
 * Komponen Navigasi Utama.
 * Menghubungkan layar-layar dengan ViewModel yang sesuai.
 */
@Composable
fun AppNavigation(
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    groupViewModel: GroupViewModel
) {
    val navController = rememberNavController()
    
    // Tentukan layar awal berdasarkan status login
    val startDestination = if (authViewModel.uiState.value.isLoggedIn) {
        Screen.Main.route
    } else {
        Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Layar Login
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Layar Register
        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Layar Utama (dengan Bottom Nav)
        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                groupViewModel = groupViewModel,
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Screen.GroupDetail.createRoute(groupId))
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }

        // Layar Detail Grup
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupDetailScreen(
                groupId = groupId,
                viewModel = groupViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
