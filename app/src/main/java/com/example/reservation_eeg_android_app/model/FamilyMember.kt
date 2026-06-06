package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FamilyMember(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String = "",
    val name: String = "",
    val relationship: String = "",
    @SerialName("resident_id")
    val residentId: String = "",
    @SerialName("phone_number")
    val phoneNumber: String = ""
)
