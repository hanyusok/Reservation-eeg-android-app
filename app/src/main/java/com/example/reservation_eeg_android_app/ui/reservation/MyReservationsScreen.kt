package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.Reservation
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme
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
    
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface
        )
    )

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
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("내 예약 목록", fontWeight = FontWeight.Bold) },
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
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Reservation")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(innerPadding)
        ) {
            if (reservations.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("예약된 내역이 없습니다.", color = MaterialTheme.colorScheme.outline)
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
    val displayTime = remember(reservation.reservedAt) {
        try {
            val dt = OffsetDateTime.parse(reservation.reservedAt)
            val kstDt = dt.atZoneSameInstant(SupabaseConfig.KST_ZONE_ID)
            kstDt.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"))
        } catch (_: Exception) {
            reservation.reservedAt
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading Icon Container
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.EventAvailable,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reservation.eegType.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "피검자",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reservation.patientName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Action Buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // Sub-items Display
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ReservationDetailRow(
                    icon = Icons.Default.AccessTime,
                    label = "예약 시간",
                    value = displayTime,
                    accentColor = MaterialTheme.colorScheme.tertiary
                )

                if (reservation.symptoms.isNotEmpty()) {
                    ReservationDetailRow(
                        icon = Icons.AutoMirrored.Filled.Note,
                        label = "증상 메모",
                        value = reservation.symptoms,
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(accentColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = accentColor
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyReservationsScreenPreview() {
    val sampleReservation = Reservation(
        id = 1,
        patientName = "홍길동",
        eegType = EegType.ROUTINE_ADULT,
        symptoms = "최근 갑작스러운 어지럼증과 두통이 심함",
        reservedAt = "2026-06-10T14:30:00+09:00"
    )
    
    ReservationeegandroidappTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ReservationItem(
                reservation = sampleReservation,
                onDelete = {},
                onEdit = {}
            )
        }
    }
}
