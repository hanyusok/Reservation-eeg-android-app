package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable

@Serializable
enum class EegType(
    val displayName: String,
    val baseDurationMin: Int,
    val prepBufferMin: Int,
    val weight: Double
) {
    ROUTINE_ADULT("성인 일반 EEG", 45, 15, 1.0),
    ROUTINE_PEDIATRIC("소아 일반 EEG", 60, 20, 1.2),
    SLEEP_DEPRIVED("수면 박탈 EEG", 120, 30, 2.5),
    VIDEO_EEG("비디오 EEG 모니터링", 180, 45, 3.0),
    AMBULATORY("24시간 휴대용 EEG", 1440, 60, 4.0);

    val price: Int
        get() = when (this) {
            ROUTINE_ADULT -> 50000
            ROUTINE_PEDIATRIC -> 70000
            SLEEP_DEPRIVED -> 120000
            VIDEO_EEG -> 180000
            AMBULATORY -> 250000
        }

    val formattedPrice: String
        get() = java.text.DecimalFormat("#,###").format(price) + "원"
}

