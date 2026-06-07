package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PostComment(
    val id: Long? = null,
    @SerialName("post_id")
    val postId: Int,
    @SerialName("user_id")
    val userId: String,
    @SerialName("user_name")
    val userName: String,
    val content: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
