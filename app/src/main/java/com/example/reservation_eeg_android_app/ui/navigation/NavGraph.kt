package com.example.reservation_eeg_android_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.reservation_eeg_android_app.ui.reservation.MyReservationsScreen
import com.example.reservation_eeg_android_app.ui.reservation.ReservationScreen
import com.example.reservation_eeg_android_app.ui.reservation.SlotSelectionScreen
import com.example.reservation_eeg_android_app.ui.reservation.SymptomScreen
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.auth.ProfileScreen
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import com.example.reservation_eeg_android_app.ui.clinic.ClinicScreen

sealed class Screen(val route: String) {
    object Clinic : Screen("clinic")
    object Reservation : Screen("reservation")
    object Symptoms : Screen("symptoms")
    object SlotSelection : Screen("slot_selection")
    object MyReservations : Screen("my_reservations")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ReservationViewModel,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Clinic.route
    ) {
        composable(Screen.Clinic.route) {
            ClinicScreen()
        }
        composable(Screen.Reservation.route) {
            val isEditing by viewModel.isEditing.collectAsState()
            ReservationScreen(
                viewModel = viewModel,
                onNext = { navController.navigate(Screen.Symptoms.route) },
                onBack = if (isEditing) { { navController.popBackStack() } } else null
            )
        }
        composable(Screen.Symptoms.route) {
            SymptomScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.SlotSelection.route) }
            )
        }
        composable(Screen.SlotSelection.route) {
            SlotSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = { 
                    navController.navigate(Screen.MyReservations.route) {
                        popUpTo(Screen.Reservation.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.MyReservations.route) {
            MyReservationsScreen(
                viewModel = viewModel,
                onEdit = { reservation ->
                    viewModel.startEditing(reservation)
                    navController.navigate(Screen.Reservation.route)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = authViewModel)
        }
    }
}
