package com.example.reservation_eeg_android_app.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.reservation_eeg_android_app.model.ReservationStatus

@Composable
fun StatusBadge(status: ReservationStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        ReservationStatus.PENDING -> Color(0xFFFFA000)
        ReservationStatus.CONFIRMED -> Color(0xFF4CAF50)
        ReservationStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        ReservationStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        modifier = modifier
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
