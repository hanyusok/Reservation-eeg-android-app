package com.example.reservation_eeg_android_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.ui.navigation.NavGraph
import com.example.reservation_eeg_android_app.ui.navigation.Screen
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabaseClient.handleDeeplinks(intent)
        enableEdgeToEdge()
        setContent {
            ReservationeegandroidappTheme {
                val navController = rememberNavController()
                val viewModel: ReservationViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                
                val items = listOf(
                    Screen.Clinic to "클리닉",
                    Screen.Community to "커뮤니티",
                    Screen.MyReservations to "내 예약",
                    Screen.Notification to "알림",
                    Screen.Profile to "프로필"
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { (screen, label) ->
                                NavigationBarItem(
                                    icon = { 
                                        Icon(
                                            when(screen) {
                                                Screen.Clinic -> Icons.Default.Home
                                                Screen.MyReservations -> Icons.Default.List
                                                Screen.Notification -> Icons.Default.Notifications
                                                Screen.Community -> Icons.Default.Groups
                                                else -> Icons.Default.AccountCircle
                                            },
                                            contentDescription = null
                                        )
                                    },
                                    label = { Text(label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        NavGraph(
                            navController = navController,
                            viewModel = viewModel,
                            authViewModel = authViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}
