package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
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
    val mockSlots = listOf("09:00", "10:30", "13:00", "14:30", "16:00")
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    
    val bookedSlots by viewModel.bookedSlots.collectAsState()
    val isSuccess by viewModel.isReservationSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onComplete()
        }
    }

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
                        viewModel.selectDate(date)
                    }
                    showDatePicker = false
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("예약 날짜 및 시간 선택") })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "선택된 날짜", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                Text(
                    text = "가능한 시간대",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mockSlots) { slot ->
                        val fullSlotTime = "${selectedDate}T$slot:00${SupabaseConfig.KST_OFFSET}"
                        val slotInstant = try {
                            OffsetDateTime.parse(fullSlotTime).toInstant()
                        } catch (e: Exception) {
                            null
                        }
                        
                        val isBooked = slotInstant != null && bookedSlots.contains(slotInstant)
                        val isPast = slotInstant != null && slotInstant.isBefore(Instant.now())
                        val isAvailable = !isBooked && !isPast
                        
                        SlotItem(
                            slot = slot,
                            isSelected = slot == selectedSlot,
                            isBooked = !isAvailable, // Disable if booked OR past
                            onClick = { if (isAvailable) selectedSlot = slot }
                        )
                    }
                }
                
                Button(
                    onClick = {
                        selectedSlot?.let {
                            viewModel.completeReservation(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedSlot != null && !isLoading
                ) {
                    Text("예약 확정")
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isBooked, onClick = onClick),
        colors = when {
            isBooked -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            isSelected -> CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            else -> CardDefaults.cardColors()
        }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = slot, 
                style = MaterialTheme.typography.bodyLarge,
                color = if (isBooked) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
            )
            when {
                isBooked -> Text(text = "예약 불가", color = MaterialTheme.colorScheme.error)
                isSelected -> Text(text = "선택됨", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
