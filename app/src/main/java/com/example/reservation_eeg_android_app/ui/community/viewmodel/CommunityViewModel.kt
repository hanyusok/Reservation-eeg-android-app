package com.example.reservation_eeg_android_app.ui.community.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.CommunityPost
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

class CommunityViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isPostSuccess = MutableStateFlow(false)
    val isPostSuccess: StateFlow<Boolean> = _isPostSuccess.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = supabaseClient.postgrest["community_posts"]
                    .select()
                    .decodeList<CommunityPost>()
                _posts.value = results.sortedByDescending { it.createdAt }
            } catch (e: Exception) {
                _error.value = "포스트를 불러오는데 실패했습니다: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createPost(category: String, title: String, content: String, imageBytes: ByteArray? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val user = supabaseClient.auth.currentUserOrNull() ?: throw Exception("로그인이 필요합니다.")
                
                // 1. Fetch user name for the post
                val profile = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<UserProfile>()
                val userName = profile?.name ?: user.email ?: "익명 사용자"

                var imageUrl: String? = null
                
                // 2. Upload image if exists
                if (imageBytes != null) {
                    val fileName = "${user.id}_${System.currentTimeMillis()}.jpg"
                    val bucket = supabaseClient.storage["post-images"]
                    bucket.upload(fileName, imageBytes)
                    imageUrl = bucket.publicUrl(fileName)
                }

                // 3. Create post entry
                val post = CommunityPost(
                    userId = user.id,
                    userName = userName,
                    category = category,
                    title = title,
                    content = content,
                    imageUrl = imageUrl
                )

                supabaseClient.postgrest["community_posts"].insert(post)
                _isPostSuccess.value = true
                fetchPosts()
            } catch (e: Exception) {
                _error.value = "포스트 작성에 실패했습니다: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _isPostSuccess.value = false
    }

    fun likePost(postId: Int) {
        // Implementation for likes would go here
        // Usually involves a separate 'post_likes' table to prevent double-likes
    }
}
