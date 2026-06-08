package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSelectionScreen(
    viewModel: ReservationViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val patientName by viewModel.patientName.collectAsState()
    val userName by viewModel.userName.collectAsState()
    
    val bookedSlots by viewModel.bookedSlots.collectAsState()
    val originalReservedAt by viewModel.originalReservedAt.collectAsState()
    val isSuccess by viewModel.isReservationSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    SlotSelectionContent(
        selectedDate = selectedDate,
        patientName = patientName,
        userName = userName,
        bookedSlots = bookedSlots,
        originalReservedAt = originalReservedAt,
        isSuccess = isSuccess,
        isLoading = isLoading,
        onDateSelected = viewModel::selectDate,
        onConfirm = viewModel::completeReservation,
        onBack = onBack,
        onComplete = onComplete,
        onResetSuccess = viewModel::resetSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSelectionContent(
    selectedDate: LocalDate,
    patientName: String,
    userName: String,
    bookedSlots: List<Instant>,
    originalReservedAt: Instant?,
    isSuccess: Boolean,
    isLoading: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onConfirm: (String) -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onResetSuccess: () -> Unit
) {
    val displayName = if (patientName == userName) "본인" else patientName
    val mockSlots = listOf("09:00", "10:30", "13:00", "14:30", "16:00")
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onComplete()
            onResetSuccess()
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface
        )
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                        onDateSelected(date)
                    }
                    showDatePicker = false
                }) {
                    Text("확인", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            },
            shape = RoundedCornerShape(28.dp)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("날짜 및 시간 선택", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "${displayName} 님의 예약",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.DateRange, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(text = "선택된 날짜", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "가능한 시간대",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(mockSlots) { slot ->
                        val fullSlotTime = "${selectedDate}T$slot:00${SupabaseConfig.KST_OFFSET}"
                        val slotInstant = try {
                            OffsetDateTime.parse(fullSlotTime).toInstant()
                        } catch (_: Exception) {
                            null
                        }
                        
                        val isBooked = slotInstant != null && bookedSlots.contains(slotInstant)
                        val isOriginalSlot = slotInstant != null && slotInstant == originalReservedAt
                        val isPast = slotInstant != null && slotInstant.isBefore(Instant.now())
                        
                        val isAvailable = (!isBooked || isOriginalSlot) && !isPast
                        
                        SlotItem(
                            slot = slot,
                            isSelected = slot == selectedSlot,
                            isBooked = !isAvailable,
                            onClick = { if (isAvailable) selectedSlot = slot }
                        )
                    }
                }
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "취소",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            selectedSlot?.let {
                                onConfirm(it)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = selectedSlot != null && !isLoading,
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(text = "예약 확정하기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
fun SlotItem(slot: String, isSelected: Boolean, isBooked: Boolean, onClick: () -> Unit) {
    if (isBooked) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            SlotItemContent(slot = slot, isBooked = true, isSelected = false)
        }
    } else {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 4.dp else 1.dp
            ),
            border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
        ) {
            SlotItemContent(slot = slot, isBooked = false, isSelected = isSelected)
        }
    }
}

@Composable
fun SlotItemContent(slot: String, isBooked: Boolean, isSelected: Boolean) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isBooked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = slot, 
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isBooked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
        }
        when {
            isBooked -> {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "예약 불가",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            isSelected -> Icon(
                Icons.Default.CheckCircle, 
                contentDescription = null, 
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SlotSelectionScreenPreview() {
    ReservationeegandroidappTheme {
        SlotSelectionContent(
            selectedDate = LocalDate.now(),
            patientName = "홍길동",
            userName = "홍길동",
            bookedSlots = emptyList(),
            originalReservedAt = null,
            isSuccess = false,
            isLoading = false,
            onDateSelected = {},
            onConfirm = {},
            onBack = {},
            onComplete = {},
            onResetSuccess = {}
        )
    }
}
