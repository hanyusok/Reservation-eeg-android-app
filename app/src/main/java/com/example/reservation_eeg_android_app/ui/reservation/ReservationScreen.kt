package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel

@Composable
fun ReservationScreen(
    viewModel: ReservationViewModel = viewModel(),
    onNext: () -> Unit
) {
    val selectedType by viewModel.selectedType.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "EEG 예약 요청",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "검사 유형을 선택해주세요",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(modifier = Modifier.weight(1f)) {
            EegTypeList(
                selectedType = selectedType,
                onTypeSelected = { viewModel.selectType(it) }
            )
        }

        if (selectedType != null) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "다음 단계로")
            }
        }
    }
}

@Composable
fun EegTypeList(
    selectedType: EegType?,
    onTypeSelected: (EegType) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(EegType.values()) { eegType ->
            EegTypeItem(
                eegType = eegType,
                isSelected = eegType == selectedType,
                onClick = { onTypeSelected(eegType) }
            )
        }
    }
}

@Composable
fun EegTypeItem(
    eegType: EegType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = eegType.displayName, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "기본 소요 시간: ${eegType.baseDurationMin}분",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReservationScreenPreview() {
    MaterialTheme {
        ReservationScreen(onNext = {})
    }
}
