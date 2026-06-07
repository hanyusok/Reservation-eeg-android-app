package com.example.reservation_eeg_android_app.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class CommunityPost(
    val id: Int? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("user_name")
    val userName: String = "",
    val category: String,
    val title: String,
    val content: String,
    @SerialName("image_url")
    val imageUrl: String? = null,
    @SerialName("likes_count")
    val likesCount: Int = 0,
    @SerialName("comments_count")
    val commentsCount: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null,
    // Client-side transient fields
    var isLiked: Boolean = false
)

val communityCategories = listOf(
    "질환정보", "케어팁", "사례검토", "경험공유", "추천정보"
)
