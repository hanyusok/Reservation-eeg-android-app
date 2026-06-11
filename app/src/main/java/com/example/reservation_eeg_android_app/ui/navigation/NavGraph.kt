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
import com.example.reservation_eeg_android_app.ui.reservation.PaymentScreen
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.auth.ProfileScreen
import com.example.reservation_eeg_android_app.ui.auth.FamilyMembersScreen
import com.example.reservation_eeg_android_app.ui.auth.viewmodel.AuthViewModel
import com.example.reservation_eeg_android_app.ui.clinic.ClinicScreen
import com.example.reservation_eeg_android_app.ui.clinic.DoctorDetailScreen
import com.example.reservation_eeg_android_app.ui.community.CommunityScreen
import com.example.reservation_eeg_android_app.model.mockDoctors
import com.example.reservation_eeg_android_app.model.UserRole
import com.example.reservation_eeg_android_app.ui.admin.AdminDashboardScreen
import com.example.reservation_eeg_android_app.ui.notification.NotificationScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.runtime.LaunchedEffect

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
    object AdminDashboard : Screen("admin_dashboard")
    object Payment : Screen("payment/{reservationId}") {
        fun createRoute(reservationId: Int) = "payment/$reservationId"
    }
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
    val userProfile by authViewModel.userProfile.collectAsState()

    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            val currentRoute = navController.currentDestination?.route
            if (profile.role == UserRole.ADMIN && currentRoute != Screen.AdminDashboard.route) {
                navController.navigate(Screen.AdminDashboard.route) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

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
            val sessionStatus by authViewModel.sessionStatus.collectAsState()
            ReservationScreen(
                viewModel = viewModel,
                sessionStatus = sessionStatus,
                onNext = { navController.navigate(Screen.Symptoms.route) },
                onBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.navigate(Screen.Profile.route) }
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
            val newReservationId by viewModel.newReservationId.collectAsState()
            SlotSelectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onComplete = { 
                    val id = newReservationId
                    if (id != null) {
                        navController.navigate(Screen.Payment.createRoute(id)) {
                            popUpTo(Screen.Reservation.route) { inclusive = true }
                        }
                        viewModel.clearNewReservationId()
                    } else {
                        navController.navigate(Screen.MyReservations.route) {
                            popUpTo(Screen.Reservation.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("reservationId") { type = NavType.IntType })
        ) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getInt("reservationId") ?: 0
            PaymentScreen(
                reservationId = reservationId,
                viewModel = viewModel,
                onPaymentSuccess = {
                    navController.navigate(Screen.MyReservations.route) {
                        popUpTo(Screen.Clinic.route) { inclusive = false }
                    }
                },
                onBack = {
                    navController.navigate(Screen.MyReservations.route) {
                        popUpTo(Screen.Clinic.route) { inclusive = false }
                    }
                }
            )
        }
        composable(Screen.MyReservations.route) {
            val sessionStatus by authViewModel.sessionStatus.collectAsState()
            MyReservationsScreen(
                viewModel = viewModel,
                sessionStatus = sessionStatus,
                onEdit = { reservation ->
                    viewModel.startEditing(reservation)
                    navController.navigate(Screen.Reservation.route)
                },
                onNewReservation = {
                    viewModel.clearEditing()
                    navController.navigate(Screen.Reservation.route)
                },
                onNavigateToLogin = { navController.navigate(Screen.Profile.route) },
                onPayClick = { id ->
                    navController.navigate(Screen.Payment.createRoute(id))
                }
            )
        }
        composable(Screen.Notification.route) {
            NotificationScreen()
        }
        composable(Screen.Community.route) {
            val sessionStatus by authViewModel.sessionStatus.collectAsState()
            CommunityScreen(
                isAuthenticated = sessionStatus is io.github.jan.supabase.auth.status.SessionStatus.Authenticated,
                onNavigateToLogin = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                viewModel = authViewModel,
                onNavigateToFamilyMembers = { navController.navigate(Screen.FamilyMembers.route) },
                onNavigateToAdmin = { navController.navigate(Screen.AdminDashboard.route) }
            )
        }
        composable(Screen.FamilyMembers.route) {
            FamilyMembersScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onSignOut = { 
                    authViewModel.signOut()
                    navController.navigate(Screen.Clinic.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
