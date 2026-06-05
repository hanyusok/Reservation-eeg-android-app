package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotSelectionScreen(
    viewModel: ReservationViewModel,
    onBack: () -> Unit,
    onComplete: () -> Unit
) {
    val selectedDate = "2026-06-10"
    val mockSlots = listOf("09:00", "10:30", "13:00", "14:30", "16:00")
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    
    val bookedSlots by viewModel.bookedSlots.collectAsState()
    val isSuccess by viewModel.isReservationSuccess.collectAsState()

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("예약 시간 선택") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "날짜: $selectedDate",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
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
                    val fullSlotTime = "${selectedDate}T$slot"
                    val isBooked = bookedSlots.contains(fullSlotTime)
                    
                    SlotItem(
                        slot = slot,
                        isSelected = slot == selectedSlot,
                        isBooked = isBooked,
                        onClick = { if (!isBooked) selectedSlot = slot }
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
                enabled = selectedSlot != null
            ) {
                Text("예약 확정")
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
