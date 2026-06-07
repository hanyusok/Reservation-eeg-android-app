package com.example.reservation_eeg_android_app.ui.clinic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reservation_eeg_android_app.model.Doctor
import com.example.reservation_eeg_android_app.model.mockDoctors
import com.example.reservation_eeg_android_app.ui.theme.ReservationeegandroidappTheme

@Composable
fun DoctorDetailScreen(
    doctor: Doctor,
    onBack: () -> Unit,
    onBookClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Custom Top Bar (Since we removed Scaffold)
        Surface(
            color = Color.Transparent, // We'll let the gradient handle it
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("의료진 상세 정보", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header with Profile Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp,
                    shadowElevation = 4.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = doctor.name.take(1),
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .offset(y = (-20).dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = doctor.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = doctor.specialty,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                // Bio Section
                DetailSection(title = "약력 소개", icon = Icons.Default.Description) {
                    Text(
                        text = doctor.fullBio,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Education & Experience
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        DetailSection(title = "학력", icon = Icons.Default.School) {
                            doctor.education.forEach { edu ->
                                BulletItem(text = edu)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                DetailSection(title = "주요 경력", icon = Icons.Default.Work) {
                    doctor.experience.forEach { exp ->
                        BulletItem(text = exp)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Call to action
                Button(
                    onClick = onBookClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${doctor.name} 원장님께 예약하기",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                // Extra spacing to ensure content isn't hidden by the bottom navbar
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

@Composable
fun BulletItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DoctorDetailScreenPreview() {
    val doctor = Doctor(
        id = "1",
        name = "김철수 원장",
        specialty = "신경과 전문의 / 의학박사",
        description = "서울대학교 병원 신경과 외래교수 역임\n20년 경력의 뇌파 진단 전문가",
        fullBio = "김철수 원장은 20년 이상의 임상 경험을 가진 뇌파 진단 분야의 권위자입니다. 수많은 난치성 뇌질환 환자들을 진료해왔으며, 정밀한 뇌파 분석을 통한 맞춤형 치료를 지향합니다.",
        education = listOf(
            "서울대학교 의과대학 졸업",
            "서울대학교 의학박사 취득",
            "미국 메이요 클리닉 연수"
        ),
        experience = listOf(
            "서울대학교 병원 신경과 외래교수",
            "대한뇌파학회 이사",
            "전) 한국뇌연구원 선임연구원"
        )
    )
    ReservationeegandroidappTheme {
        DoctorDetailScreen(doctor = doctor, onBack = {}, onBookClick = {})
    }
}
