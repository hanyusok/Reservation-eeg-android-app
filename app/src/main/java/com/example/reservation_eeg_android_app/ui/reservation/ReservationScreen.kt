package com.example.reservation_eeg_android_app.ui.reservation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.ui.reservation.viewmodel.ReservationViewModel
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme
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
    
    ReservationContent(
        selectedType = selectedType,
        patientName = patientName,
        userName = userName,
        familyMembers = familyMembers,
        sessionStatus = sessionStatus,
        onNext = onNext,
        onBack = onBack,
        onNavigateToLogin = onNavigateToLogin,
        onTypeSelected = viewModel::selectType,
        onPatientSelected = viewModel::selectPatient,
        onFetchFamilyMembers = viewModel::fetchFamilyMembers
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationContent(
    selectedType: EegType?,
    patientName: String,
    userName: String,
    familyMembers: List<FamilyMember>,
    sessionStatus: SessionStatus,
    onNext: () -> Unit,
    onBack: (() -> Unit)?,
    onNavigateToLogin: () -> Unit,
    onTypeSelected: (EegType) -> Unit,
    onPatientSelected: (String) -> Unit,
    onFetchFamilyMembers: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isAuthenticated = sessionStatus is SessionStatus.Authenticated

    LaunchedEffect(Unit) {
        if (isAuthenticated) {
            onFetchFamilyMembers()
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
                title = { Text("EEG 예약 요청", fontWeight = FontWeight.Bold) },
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
        if (isAuthenticated) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "누구를 위한 예약인가요?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                PatientSelectionList(
                    selectedName = patientName,
                    userName = userName,
                    familyMembers = familyMembers,
                    onPatientSelected = onPatientSelected
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "검사 유형을 선택해주세요",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box(modifier = Modifier.weight(1f)) {
                    EegTypeList(
                        selectedType = selectedType,
                        onTypeSelected = onTypeSelected
                    )
                }

                if (selectedType != null && patientName.isNotBlank()) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = "다음 단계로", 
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("로그인이 필요합니다.", color = MaterialTheme.colorScheme.outline)
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
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
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading Icon with soft background
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when(eegType) {
                            EegType.ROUTINE_ADULT -> Icons.Default.Person
                            EegType.ROUTINE_PEDIATRIC -> Icons.Default.ChildCare
                            EegType.SLEEP_DEPRIVED -> Icons.Default.Bedtime
                            EegType.VIDEO_EEG -> Icons.Default.Videocam
                            EegType.AMBULATORY -> Icons.AutoMirrored.Filled.DirectionsRun
                        },
                        contentDescription = null,
                        modifier = Modifier.size(26.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = eegType.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "기본 소요 시간: ${eegType.baseDurationMin}분",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Preview(showBackground = true)
@Composable
fun ReservationScreenPreview() {
    ReservationeegandroidappTheme {
        ReservationContent(
            selectedType = EegType.ROUTINE_ADULT,
            patientName = "홍길동",
            userName = "홍길동",
            familyMembers = listOf(
                FamilyMember(id = 1, name = "김철수", relationship = "부")
            ),
            sessionStatus = SessionStatus.Authenticated(
                session = io.github.jan.supabase.auth.user.UserSession(
                    accessToken = "", refreshToken = "", expiresIn = 3600, tokenType = "",
                    user = io.github.jan.supabase.auth.user.UserInfo(id = "", aud = "", email = "", createdAt = null, updatedAt = null)
                )
            ),
            onNext = {},
            onBack = {},
            onNavigateToLogin = {},
            onTypeSelected = {},
            onPatientSelected = {},
            onFetchFamilyMembers = {}
        )
    }
}
