package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Reservation(
    val id: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("patient_name")
    val patientName: String,
    @SerialName("eeg_type")
    val eegType: EegType,
    val symptoms: String,
    @SerialName("reserved_at")
    val reservedAt: String
)
