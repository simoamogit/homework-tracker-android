package com.simo3000.imieicompiti.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.simo3000.imieicompiti.data.local.TokenStore
import com.simo3000.imieicompiti.ui.screens.archive.ArchiveScreen
import com.simo3000.imieicompiti.ui.screens.auth.LoginScreen
import com.simo3000.imieicompiti.ui.screens.auth.RegisterScreen
import com.simo3000.imieicompiti.ui.screens.dashboard.DashboardScreen
import com.simo3000.imieicompiti.ui.screens.settings.SettingsScreen

object Routes {
    const val LOGIN     = "login"
    const val REGISTER  = "register"
    const val DASHBOARD = "dashboard"
    const val ARCHIVE   = "archive"
    const val SETTINGS  = "settings"
}

@Composable
fun NavGraph() {
    val navController    = rememberNavController()
    val context          = LocalContext.current
    val tokenStore       = TokenStore(context)
    val startDestination = if (tokenStore.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess       = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onLogout = {
                    TokenStore(context).clear()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateToArchive   = { navController.navigate(Routes.ARCHIVE) },
                onNavigateToSettings  = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.ARCHIVE) {
            ArchiveScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}