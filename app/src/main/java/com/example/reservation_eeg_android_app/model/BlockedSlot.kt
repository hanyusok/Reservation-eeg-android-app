package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class BlockedSlot(
    val id: Int? = null,
    @SerialName("blocked_at")
    val blockedAt: String, // ISO String
    val reason: String = "정기 휴무 또는 내부 사정",
    @SerialName("created_at")
    val createdAt: String? = null
)
