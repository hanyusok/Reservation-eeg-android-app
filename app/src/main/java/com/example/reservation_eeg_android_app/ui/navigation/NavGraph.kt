package com.example.reservation_eeg_android_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.reservation_eeg_android_app.ui.reservation.MyReservationsScreen
import com.example.reservation_eeg_android_app.ui.reservation.ReservationScreen
import com.example.reservation_eeg_android_app.ui.reservation.SlotSelectionScreen
import com.example.reservation_eeg_android_app.ui.reservation.SymptomScreen
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.auth.ProfileScreen
import com.example.reservation_eeg_android_app.ui.auth.FamilyMembersScreen
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import com.example.reservation_eeg_android_app.ui.clinic.ClinicScreen
import com.example.reservation_eeg_android_app.ui.clinic.DoctorDetailScreen
import com.example.reservation_eeg_android_app.model.mockDoctors
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(val route: String) {
    object Clinic : Screen("clinic")
    object Reservation : Screen("reservation")
    object Symptoms : Screen("symptoms")
    object SlotSelection : Screen("slot_selection")
    object MyReservations : Screen("my_reservations")
    object Notification : Screen("notification")
    object Community : Screen("community")
    object Profile : Screen("profile")
    object FamilyMembers : Screen("family_members")
    object DoctorDetail : Screen("doctor_detail/{doctorId}") {
        fun createRoute(doctorId: String) = "doctor_detail/$doctorId"
    }
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
            ClinicScreen(
                onNavigateToReservation = { navController.navigate(Screen.Reservation.route) },
                onNavigateToDoctorDetail = { doctorId -> 
                    navController.navigate(Screen.DoctorDetail.createRoute(doctorId)) 
                }
            )
        }
        composable(
            route = Screen.DoctorDetail.route,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            val doctor = mockDoctors.find { it.id == doctorId }
            if (doctor != null) {
                DoctorDetailScreen(
                    doctor = doctor,
                    onBack = { navController.popBackStack() },
                    onBookClick = { navController.navigate(Screen.Reservation.route) }
                )
            }
        }
        composable(Screen.Reservation.route) {
            ReservationScreen(
                viewModel = viewModel,
                onNext = { navController.navigate(Screen.Symptoms.route) },
                onBack = { navController.popBackStack() }
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
                },
                onNewReservation = {
                    viewModel.clearEditing()
                    navController.navigate(Screen.Reservation.route)
                }
            )
        }
        composable(Screen.Notification.route) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("알림 화면 (준비 중)")
            }
        }
        composable(Screen.Community.route) {
            Box(modifier = androidx.compose.ui.Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("커뮤니티 화면 (준비 중)")
            }
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onNavigateToFamilyMembers = { navController.navigate(Screen.FamilyMembers.route) }
            )
        }
        composable(Screen.FamilyMembers.route) {
            FamilyMembersScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
