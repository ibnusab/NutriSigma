package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainContainer
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.NutriSigmaTheme
import com.example.viewmodel.NutriViewModel
import com.example.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get lazy repository instance from Application
        val appRepository = (application as NutriApplication).repository

        // Create ViewModel with Custom Factory
        val factory = ViewModelFactory(application, appRepository)
        val viewModel = ViewModelProvider(this, factory)[NutriViewModel::class.java]

        setContent {
            // Observe user preferences to switch themes globally and reactively
            val userState by viewModel.currentUser.collectAsState()
            val isDarkMode = userState?.isDarkMode ?: false

            NutriSigmaTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // 1. Splash Screen
                        composable("splash") {
                            val userEmail by viewModel.currentUserEmail.collectAsState()
                            SplashScreen(
                                isUserLoggedIn = userEmail != null,
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                },
                                onNavigateToHome = {
                                    navController.navigate("main") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 2. Login Screen
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToRegister = {
                                    navController.navigate("register")
                                },
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 3. Register Screen
                        composable("register") {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateToLogin = {
                                    navController.navigate("login") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                },
                                onRegisterSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("register") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // 4. Main App Container (Bottom Bar Frame)
                        composable("main") {
                            MainContainer(
                                viewModel = viewModel,
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
