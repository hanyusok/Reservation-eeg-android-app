package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomScreen(
    viewModel: ReservationViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val symptoms by viewModel.symptoms.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("증상 체크리스트") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
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
                text = "현재 겪고 계신 증상을 자유롭게 입력해주세요.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = symptoms,
                onValueChange = { viewModel.updateSymptoms(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("예: 최근 갑작스러운 의식 소실, 경련 등") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "추가 체크 항목",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            ChecklistItem(label = "최근 1주일 내 경련 발생 여부")
            ChecklistItem(label = "현재 복용 중인 약물 있음")
            ChecklistItem(label = "수면 장애 여부")
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = symptoms.isNotBlank()
            ) {
                Text("AI Triage 분석 요청")
            }
        }
    }
}

@Composable
fun ChecklistItem(label: String) {
    var checked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { checked = it })
        Text(text = label, modifier = Modifier.padding(start = 8.dp))
    }
}
