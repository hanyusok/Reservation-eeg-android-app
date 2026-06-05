package com.example.reservation_eeg_android_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.reservation_eeg_android_app.ui.reservation.ReservationScreen
import com.example.reservation_eeg_android_app.ui.reservation.SlotSelectionScreen
import com.example.reservation_eeg_android_app.ui.reservation.SymptomScreen
import com.example.reservation_eeg_android_app.ui.reservation.TriageScreen
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel

sealed class Screen(val route: String) {
    object Reservation : Screen("reservation")
    object Symptoms : Screen("symptoms")
    object Triage : Screen("triage")
    object SlotSelection : Screen("slot_selection")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: ReservationViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Reservation.route
    ) {
        composable(Screen.Reservation.route) {
            ReservationScreen(
                viewModel = viewModel,
                onNext = { navController.navigate(Screen.Symptoms.route) }
            )
        }
        composable(Screen.Symptoms.route) {
            SymptomScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(Screen.Triage.route) }
            )
        }
        composable(Screen.Triage.route) {
            TriageScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onConfirm = { navController.navigate(Screen.SlotSelection.route) }
            )
        }
        composable(Screen.SlotSelection.route) {
            SlotSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = { /* Finalize reservation */ }
            )
        }
    }
}
