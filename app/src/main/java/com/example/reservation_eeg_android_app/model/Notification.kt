package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Notification(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String,
    val title: String,
    val message: String,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("related_id")
    val relatedId: Int? = null // e.g., the reservation ID
)
