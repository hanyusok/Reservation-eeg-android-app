package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.LazyRow
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationScreen(
    viewModel: ReservationViewModel = viewModel(),
    sessionStatus: SessionStatus,
    onNext: () -> Unit,
    onBack: (() -> Unit)? = null,
    onNavigateToLogin: () -> Unit
) {
    val selectedType by viewModel.selectedType.collectAsState()
    val patientName by viewModel.patientName.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val familyMembers by viewModel.familyMembers.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val isAuthenticated = sessionStatus is SessionStatus.Authenticated

    LaunchedEffect(Unit) {
        if (isAuthenticated) {
            viewModel.fetchFamilyMembers()
        }
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("EEG 예약 요청") },
                navigationIcon = {
                    onBack?.let {
                        IconButton(onClick = it) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                onPatientSelected = viewModel::selectPatient
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
                    onTypeSelected = viewModel::selectType
                )
            }

            if ((selectedType != null && patientName.isNotBlank())) {
                Button(
                    onClick = {
                        if (isAuthenticated) {
                            onNext()
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = isAuthenticated
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        item {
            val isMeSelected = selectedName == userName || (selectedName.isEmpty() && userName.isEmpty())
            FilterChip(
                selected = isMeSelected,
                onClick = { onPatientSelected(userName) },
                label = { Text("본인") },
                leadingIcon = if (isMeSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        items(familyMembers) { member ->
            val isSelected = selectedName == member.name
            FilterChip(
                selected = isSelected,
                onClick = { onPatientSelected(member.name) },
                label = { Text(member.name) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null,
                shape = RoundedCornerShape(12.dp)
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
        ReservationScreen(
            onNext = {},
            sessionStatus = SessionStatus.NotAuthenticated(),
            onNavigateToLogin = {}
        )
    }
}
