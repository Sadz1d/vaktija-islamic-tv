package com.islamictv.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun AdminApp() {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            // Show loading screen
            LoadingScreen()
        }
        is AuthState.Unauthenticated -> {
            // Show login screen
            LoginScreen(
                onLoginSuccess = { /* Will trigger recomposition */ }
            )
        }
        is AuthState.Authenticated -> {
            // Show main app
            AuthenticatedApp(
                onSignOut = { authViewModel.signOut() }
            )
        }
        is AuthState.Error -> {
            // Show login screen (error is handled there)
            LoginScreen(
                onLoginSuccess = { /* Will trigger recomposition */ }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    androidx.compose.foundation.layout.Box(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}

@Composable
fun AuthenticatedApp(onSignOut: () -> Unit) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                onAddContent = { navController.navigate("add") },
                onEditContent = { itemId -> navController.navigate("edit/$itemId") },
                onSignOut = onSignOut
            )
        }

        composable("add") {
            AddEditScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "edit/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            AddEditScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}