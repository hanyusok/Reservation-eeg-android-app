package com.example.reservation_eeg_android_app.ui.clinic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("EEG 클리닉 정보") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Clinic Introduction
            SectionTitle("클리닉 소개")
            Text(
                "전문적인 뇌파(EEG) 분석과 정밀 진단을 통해 여러분의 뇌 건강을 책임지는 EEG 전문 클리닉입니다.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Speciality
            SectionTitle("전문 분야")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("수면 장애", "간질/경련", "인지 기능", "두통", "어지럼증").forEach { speciality ->
                    SuggestionChip(onClick = {}, label = { Text(speciality) })
                }
            }

            // Doctor Profiles
            SectionTitle("의료진 소개")
            DoctorCard(
                name = "김철수 원장",
                specialty = "신경과 전문의 / 의학박사",
                experience = "서울대학교 병원 신경과 외래교수"
            )
            DoctorCard(
                name = "이영희 과장",
                specialty = "신경과 전문의",
                experience = "EEG 분석 및 판독 전문가"
            )

            // Address
            SectionTitle("찾아오시는 길")
            ContactRow(Icons.Default.LocationOn, "서울특별시 강남구 테헤란로 123, EEG 빌딩 5층")

            // Contact
            SectionTitle("연락처 및 운영 시간")
            ContactRow(Icons.Default.Phone, "02-123-4567")
            ContactRow(Icons.Default.Email, "info@eegclinic.com")
            ContactRow(Icons.Default.Info, "평일: 09:00 - 18:00\n토요일: 09:00 - 13:00\n일요일/공휴일 휴무")
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun DoctorCard(name: String, specialty: String, experience: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(specialty, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(experience, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ContactRow(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}
