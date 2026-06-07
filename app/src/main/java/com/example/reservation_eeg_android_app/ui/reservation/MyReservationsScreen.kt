package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.model.Reservation
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyReservationsScreen(
    viewModel: ReservationViewModel,
    sessionStatus: SessionStatus,
    onEdit: (Reservation) -> Unit,
    onNewReservation: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val reservations by viewModel.userReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val isAuthenticated = sessionStatus is SessionStatus.Authenticated
    
    LaunchedEffect(sessionStatus) {
        if (!isAuthenticated) {
            val result = snackbarHostState.showSnackbar(
                message = "로그인이 필요한 서비스입니다. 예약을 위해 먼저 로그인해주세요.",
                actionLabel = "로그인",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onNavigateToLogin()
            }
        }
    }
    
    var reservationToDelete by remember { mutableStateOf<Reservation?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchBookedSlots()
    }

    if (reservationToDelete != null) {
        AlertDialog(
            onDismissRequest = { reservationToDelete = null },
            title = { Text("예약 삭제") },
            text = { Text("이 예약을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reservationToDelete?.id?.let { id ->
                            scope.launch {
                                viewModel.deleteReservation(id)
                            }
                        }
                        reservationToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToDelete = null }) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("내 예약 목록") },
                actions = {
                    IconButton(onClick = { viewModel.fetchBookedSlots() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isAuthenticated) {
                        onNewReservation()
                    } else {
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "로그인이 필요한 서비스입니다.",
                                actionLabel = "로그인"
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                onNavigateToLogin()
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Reservation")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (reservations.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("예약된 내역이 없습니다.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reservations) { reservation ->
                        ReservationItem(
                            reservation = reservation,
                            onDelete = { reservationToDelete = reservation },
                            onEdit = { onEdit(reservation) }
                        )
                    }
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ReservationItem(
    reservation: Reservation,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    val displayTime = remember(reservation.reservedAt) {
                        try {
                            val dt = OffsetDateTime.parse(reservation.reservedAt)
                            // Convert to KST explicitly in case Supabase returned UTC
                            val kstDt = dt.atZoneSameInstant(SupabaseConfig.KST_ZONE_ID)
                            kstDt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                        } catch (e: Exception) {
                            reservation.reservedAt
                        }
                    }

                    Text(
                        text = "${reservation.eegType.displayName} (${reservation.patientName})",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "시간: $displayTime (KST)",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (reservation.symptoms.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "증상: ${reservation.symptoms}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
