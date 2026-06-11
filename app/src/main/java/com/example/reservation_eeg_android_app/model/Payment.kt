package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Payment(
    val id: Int? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("reservation_id")
    val reservationId: Int,
    @SerialName("user_id")
    val userId: String,
    val amount: Int,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("card_issuer")
    val cardIssuer: String,
    @SerialName("transaction_id")
    val transactionId: String,
    val status: String = "success"
)
