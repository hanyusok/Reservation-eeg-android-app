package com.example.reservation_eeg_android_app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reservation_eeg_android_app.model.Reservation
import com.example.reservation_eeg_android_app.model.ReservationStatus
import com.example.reservation_eeg_android_app.model.UserProfile
import com.example.reservation_eeg_android_app.model.BlockedSlot
import com.example.reservation_eeg_android_app.model.Notification
import com.example.reservation_eeg_android_app.ui.admin.viewmodel.AdminReservationViewModel
import com.example.reservation_eeg_android_app.ui.admin.viewmodel.AdminUserViewModel
import com.example.reservation_eeg_android_app.ui.admin.viewmodel.AdminScheduleViewModel
import com.example.reservation_eeg_android_app.ui.admin.viewmodel.AdminMessageViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AdminDashboardScreen(
    onSignOut: () -> Unit,
    adminViewModel: AdminReservationViewModel = viewModel(),
    userViewModel: AdminUserViewModel = viewModel(),
    scheduleViewModel: AdminScheduleViewModel = viewModel(),
    messageViewModel: AdminMessageViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            header = {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).padding(vertical = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            NavigationRailItem(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                label = { Text("대시보드") }
            )
            NavigationRailItem(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                icon = { Icon(Icons.Default.EventNote, contentDescription = "Reservations") },
                label = { Text("예약관리") }
            )
            NavigationRailItem(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                label = { Text("회원관리") }
            )
            NavigationRailItem(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                icon = { Icon(Icons.Default.Schedule, contentDescription = "Schedule") },
                label = { Text("일정관리") }
            )
            NavigationRailItem(
                selected = selectedTab == 4,
                onClick = { selectedTab = 4 },
                icon = { Icon(Icons.Default.Message, contentDescription = "Messages") },
                label = { Text("메시지센터") }
            )
            Spacer(modifier = Modifier.weight(1f))
            NavigationRailItem(
                selected = false,
                onClick = onSignOut,
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout") },
                label = { Text("로그아웃") }
            )
        }

        // Main Content Area - Fixed with weight to prevent pushing Rail out
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            when (selectedTab) {
                0 -> AdminSummaryView(adminViewModel)
                1 -> MasterReservationList(adminViewModel)
                2 -> UserManagementView(userViewModel)
                3 -> ScheduleManagementView(scheduleViewModel)
                4 -> MessageManagementView(messageViewModel)
            }
        }
    }
}

@Composable
fun AdminSummaryView(viewModel: AdminReservationViewModel) {
    val filteredReservations by viewModel.filteredReservations.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val weeklyStats by viewModel.weeklyStats.collectAsState()
    val typeStats by viewModel.typeStats.collectAsState()
    
    Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
        Text(
            text = "${selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))} 현황", 
            style = MaterialTheme.typography.headlineMedium, 
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard("선택일 예약", filteredReservations.size.toString(), Icons.Default.People, MaterialTheme.colorScheme.primary)
            SummaryCard("대기중", filteredReservations.count { it.status == ReservationStatus.PENDING }.toString(), Icons.Default.HourglassEmpty, Color(0xFFFFA000))
            SummaryCard("확정됨", filteredReservations.count { it.status == ReservationStatus.CONFIRMED }.toString(), Icons.Default.CheckCircle, Color(0xFF4CAF50))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Weekly Chart
            ElevatedCard(
                modifier = Modifier.weight(1.5f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("최근 7일 예약 추이", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    WeeklyReservationChart(stats = weeklyStats)
                }
            }

            // Type Distribution
            ElevatedCard(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("검사 유형별 분포", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(24.dp))
                    TypeDistributionChart(stats = typeStats)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun WeeklyReservationChart(stats: Map<LocalDate, Int>) {
    val maxVal = (stats.values.maxOrNull() ?: 1).coerceAtLeast(5)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        stats.forEach { (date, count) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(count.toString(), style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(count.toFloat() / maxVal)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                            )
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MM/dd")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun TypeDistributionChart(stats: Map<String, Int>) {
    val total = stats.values.sum().coerceAtLeast(1)
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        stats.forEach { (name, count) ->
            val percentage = (count.toFloat() / total)
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(name, style = MaterialTheme.typography.labelMedium)
                    Text("${(percentage * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, icon: ImageVector, color: Color) {
    ElevatedCard(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            Text(value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterReservationList(viewModel: AdminReservationViewModel) {
    val filteredReservations by viewModel.filteredReservations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var selectedReservationForStatus by remember { mutableStateOf<Pair<Reservation, ReservationStatus>?>(null) }

    if (selectedReservationForStatus != null) {
        val (res, nextStatus) = selectedReservationForStatus!!
        SendNotificationDialog(
            userName = res.patientName,
            onDismiss = { selectedReservationForStatus = null },
            onSend = { title, message ->
                viewModel.updateReservationStatus(res.id!!, nextStatus, title, message)
                selectedReservationForStatus = null
            }
        )
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

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("전체 예약 관리", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = { viewModel.fetchAllReservations() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("환자 이름 또는 검사 유형 검색") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = if (searchQuery.isNotEmpty()) {
                {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            } else null,
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        if (filteredReservations.isEmpty() && !isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("검색 결과가 없습니다.", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredReservations) { reservation ->
                    AdminReservationItem(reservation) { newStatus ->
                        if (newStatus == ReservationStatus.CONFIRMED) {
                            selectedReservationForStatus = reservation to newStatus
                        } else {
                            viewModel.updateReservationStatus(reservation.id!!, newStatus)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserManagementView(viewModel: AdminUserViewModel) {
    val users by viewModel.filteredUsers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val history by viewModel.selectedUserHistory.collectAsState()
    val userNotifications by viewModel.selectedUserNotifications.collectAsState()
    
    var selectedUser by remember { mutableStateOf<UserProfile?>(null) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    if (showNotificationDialog && selectedUser != null) {
        SendNotificationDialog(
            userName = selectedUser!!.name,
            onDismiss = { showNotificationDialog = false },
            onSend = { title, message ->
                viewModel.sendNotification(selectedUser!!.id!!, title, message)
                showNotificationDialog = false
            }
        )
    }

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text("회원 관리", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("이름, 이메일, 전화번호 검색") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(users) { user ->
                    val isSelected = selectedUser?.id == user.id
                    OutlinedCard(
                        onClick = { 
                            selectedUser = user
                            viewModel.fetchUserHistory(user.id!!)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(user.name.take(1), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(user.name, fontWeight = FontWeight.Bold)
                                Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.weight(1.2f)) {
            if (selectedUser != null) {
                UserDetailPane(
                    user = selectedUser!!, 
                    history = history,
                    notifications = userNotifications,
                    onSendMessageClick = { showNotificationDialog = true }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("회원을 선택하면 상세 정보와 예약 이력을 볼 수 있습니다.", color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}

@Composable
fun SendNotificationDialog(
    userName: String,
    onDismiss: () -> Unit,
    onSend: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("알림") }
    var message by remember { mutableStateOf("") }

    val templates = listOf(
        "일반 EEG" to "안녕하세요, $userName 님. Routine EEG 검사 안내입니다. 검사 당일 머리를 깨끗이 감고 오시되, 헤어 젤이나 스프레이는 사용하지 마시기 바랍니다. 검사 10분 전까지 내원해 주세요.",
        "수면 EEG" to "안녕하세요, $userName 님. 수면 박탈 EEG 안내입니다. 정확한 검사를 위해 전날 밤 수면 시간을 4시간 이내로 제한해 주시기 바랍니다. 검사 당일 졸음이 올 수 있으니 보호자 동반을 권장합니다.",
        "비디오 모니터링" to "안녕하세요, $userName 님. 비디오 EEG 모니터링 안내입니다. 장시간 검사가 진행되므로 편안한 복장으로 오시기 바랍니다. 현재 복용 중인 약 처방전을 지참해 주세요.",
        "예약 확정" to "안녕하세요, $userName 님. 요청하신 예약이 확정되었습니다. 예약된 시간에 늦지 않게 방문해 주시기 바랍니다. 감사합니다."
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("$userName 님에게 알림 전송", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("템플릿 선택", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    templates.forEach { (label, content) ->
                        SuggestionChip(
                            onClick = { 
                                title = label
                                message = content 
                            },
                            label = { Text(label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("메시지 내용") },
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(title, message) },
                enabled = message.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("전송하기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleManagementView(viewModel: AdminScheduleViewModel) {
    val blockedSlots by viewModel.blockedSlots.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val timeSlots = listOf("09:00", "10:30", "13:00", "14:30", "16:00")
    var showDatePicker by remember { mutableStateOf(false) }

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
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("취소") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("진료 일정 관리", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            OutlinedButton(
                onClick = { showDatePicker = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("해당 날짜의 시간별 예약 차단/해제", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(timeSlots) { slot ->
                val fullSlotTime = "${selectedDate}T$slot:00+09:00"
                val blockedInfo = blockedSlots.find { it.blockedAt == fullSlotTime }
                val isBlocked = blockedInfo != null

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isBlocked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.05f) else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = if (isBlocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(slot, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        
                        if (isBlocked) {
                            Button(
                                onClick = { viewModel.unblockSlot(blockedInfo!!.id!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("예약 차단 해제") }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.blockSlot(slot, "내부 사정으로 인한 차단") },
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("예약 차단하기") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageManagementView(viewModel: AdminMessageViewModel) {
    val notifications by viewModel.allNotifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showBroadcastDialog by remember { mutableStateOf(false) }

    if (showBroadcastDialog) {
        SendNotificationDialog(
            userName = "전체 회원",
            onDismiss = { showBroadcastDialog = false },
            onSend = { title, message ->
                viewModel.sendBroadcast(title, message)
                showBroadcastDialog = false
            }
        )
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("메시지 센터", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showBroadcastDialog = true },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("전체 공지 발송")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("최근 발송 이력", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notifications) { notification ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notification.title, fontWeight = FontWeight.Bold)
                            Text(notification.message, style = MaterialTheme.typography.bodyMedium)
                            val date = try {
                                OffsetDateTime.parse(notification.createdAt).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                            } catch (e: Exception) { notification.createdAt ?: "" }
                            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                        IconButton(onClick = { viewModel.deleteNotification(notification.id!!) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailPane(
    user: UserProfile, 
    history: List<Reservation>, 
    notifications: List<Notification>,
    onSendMessageClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("회원 상세 정보", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onSendMessageClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("알림 보내기")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            DetailRow(label = "이름", value = user.name)
            DetailRow(label = "연락처", value = user.phoneNumber)
            DetailRow(label = "이메일", value = user.email)
            DetailRow(label = "주소", value = user.address.ifBlank { "등록되지 않음" })
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("최근 발송 메시지 (${notifications.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            notifications.take(3).forEach { note ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(note.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text(note.message, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("예약 이력 (${history.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            history.forEach { res ->
                val date = try {
                    OffsetDateTime.parse(res.reservedAt).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                } catch (e: Exception) { res.reservedAt }
                
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(res.eegType.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text(date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                        StatusBadge(res.status)
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AdminReservationItem(reservation: Reservation, onStatusChange: (ReservationStatus) -> Unit) {
    val formattedTime = try {
        val dt = OffsetDateTime.parse(reservation.reservedAt)
        dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        reservation.reservedAt
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reservation.patientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(reservation.status)
                }
                Text(
                    text = "${reservation.eegType.displayName} | $formattedTime",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                if (reservation.symptoms.isNotBlank()) {
                    Text(
                        text = "증상: ${reservation.symptoms}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (reservation.status == ReservationStatus.PENDING) {
                    Button(
                        onClick = { onStatusChange(ReservationStatus.CONFIRMED) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("확정")
                    }
                }
                if (reservation.status == ReservationStatus.CONFIRMED) {
                    Button(
                        onClick = { onStatusChange(ReservationStatus.COMPLETED) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("완료")
                    }
                }
                OutlinedButton(
                    onClick = { onStatusChange(ReservationStatus.CANCELLED) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("취소")
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ReservationStatus) {
    val color = when (status) {
        ReservationStatus.PENDING -> Color(0xFFFFA000)
        ReservationStatus.CONFIRMED -> Color(0xFF4CAF50)
        ReservationStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = when(status) {
                ReservationStatus.PENDING -> "대기중"
                ReservationStatus.CONFIRMED -> "확정됨"
                ReservationStatus.COMPLETED -> "진료완료"
                ReservationStatus.CANCELLED -> "취소됨"
            },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
