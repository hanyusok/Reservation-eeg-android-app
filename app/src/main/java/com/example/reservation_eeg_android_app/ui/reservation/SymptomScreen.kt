package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.util.AppTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomScreen(
    viewModel: ReservationViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val symptoms by viewModel.symptoms.collectAsState()
    val patientName by viewModel.patientName.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val hasSeizure by viewModel.hasSeizure.collectAsState()
    val hasMedications by viewModel.hasMedications.collectAsState()
    val hasSleepDisorder by viewModel.hasSleepDisorder.collectAsState()

    val displayName = if (patientName == userName) "본인" else patientName
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("증상 체크리스트") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
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
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "${displayName} 님이 겪고 계신 증상을 입력해주세요.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "현재 겪고 계신 증상을 자유롭게 입력해주세요.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            AppTextField(
                value = symptoms,
                onValueChange = { viewModel.updateSymptoms(it) },
                label = "증상 입력",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = "예: 최근 갑작스러운 의식 소실, 경련 등"
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "추가 체크 항목",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ChecklistItem(
                label = "최근 1주일 내 경련 발생 여부",
                checked = hasSeizure,
                onCheckedChange = { viewModel.updateHasSeizure(it) }
            )
            ChecklistItem(
                label = "현재 복용 중인 약물 있음",
                checked = hasMedications,
                onCheckedChange = { viewModel.updateHasMedications(it) }
            )
            ChecklistItem(
                label = "수면 장애 여부",
                checked = hasSleepDisorder,
                onCheckedChange = { viewModel.updateHasSleepDisorder(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = symptoms.isNotBlank()
                ) {
                    Text(
                        text = "다음 단계", 
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ChecklistItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}
