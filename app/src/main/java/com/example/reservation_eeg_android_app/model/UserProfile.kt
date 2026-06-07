package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class UserProfile(
    val id: String? = null, // Maps to auth.users.id
    val name: String = "",
    val sex: String = "",
    @SerialName("resident_id")
    val residentId: String = "",
    val address: String = "",
    @SerialName("phone_number")
    val phoneNumber: String = "",
    val email: String = ""
)
