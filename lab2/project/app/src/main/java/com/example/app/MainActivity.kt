package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app.lab2.PasswordInputScreen
import com.example.app.lab2.PasswordResultScreen
import com.example.app.lab2.PasswordViewModel
import com.example.app.ui.theme.AppTheme

object NavRoutes {
    const val INPUT = "input"
    const val RESULT = "result"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PasswordNavHost(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun PasswordNavHost(
    modifier: Modifier = Modifier,
    viewModel: PasswordViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.INPUT,
        modifier = modifier
    ) {
        composable(NavRoutes.INPUT) {
            PasswordInputScreen(
                viewModel = viewModel,
                onNavigateToResult = {
                    navController.navigate(NavRoutes.RESULT) {
                        popUpTo(NavRoutes.INPUT) { inclusive = false }
                    }
                }
            )
        }
        composable(NavRoutes.RESULT) {
            PasswordResultScreen(
                viewModel = viewModel,
                onCancel = {
                    navController.popBackStack()
                }
            )
        }
    }
}
