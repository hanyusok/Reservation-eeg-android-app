package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyRow
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    viewModel: ReservationViewModel = viewModel(),
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val patientName by viewModel.patientName.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EEG 예약 요청") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "누구를 위한 예약인가요?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PatientSelectionList(
                selectedName = patientName,
                userName = userName,
                familyMembers = familyMembers,
                onPatientSelected = { viewModel.selectPatient(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            if (selectedType != null && patientName.isNotBlank()) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSelectionList(
    selectedName: String,
    userName: String,
    familyMembers: List<FamilyMember>,
    onPatientSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = selectedName == userName,
                onClick = { onPatientSelected(userName) },
                label = { Text("본인") }
            )
        }
        
        items(familyMembers) { member ->
            FilterChip(
                selected = selectedName == member.name,
                onClick = { onPatientSelected(member.name) },
                label = { Text(member.name) }
            )
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
        items(EegType.entries) { eegType ->
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
