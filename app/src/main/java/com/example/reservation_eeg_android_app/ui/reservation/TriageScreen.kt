package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageScreen(
    viewModel: ReservationViewModel,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    val selectedType by viewModel.selectedType.collectAsState()
    
    // In a real app, this would be determined by an algorithm.
    // For now, we'll just use the one selected or a hardcoded suggestion.
    val suggestedType = EegType.SLEEP_DEPRIVED 

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("AI Triage 결과") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "추천 검사 유형",
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = suggestedType.displayName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.typography.headlineLarge.color
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "이유:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "입력하신 증상(경련, 의식 소실)을 정밀하게 분석하기 위해 수면 유도 후 뇌파를 측정하는 것이 권장됩니다.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("다시 입력")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        viewModel.selectType(suggestedType)
                        onConfirm()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("추천대로 예약")
                }
            }
        }
    }
}
