package com.example.reservation_eeg_android_app.ui.community.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.CommunityPost
import com.example.reservation_eeg_android_app.model.PostComment
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.JsonNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isPostSuccess = MutableStateFlow(false)
    val isPostSuccess: StateFlow<Boolean> = _isPostSuccess.asStateFlow()

    private val _comments = MutableStateFlow<Map<Int, List<PostComment>>>(emptyMap())
    val comments: StateFlow<Map<Int, List<PostComment>>> = _comments.asStateFlow()

    init {
        fetchPosts()
    }

    fun fetchPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = supabaseClient.postgrest["community_posts"]
                    .select {
                        order("created_at", order = Order.DESCENDING)
                        range(0, 50)
                    }
                    .decodeList<CommunityPost>()
                
                // Check liked status for each post
                val userId = supabaseClient.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val userLikes = supabaseClient.postgrest["post_likes"]
                        .select {
                            filter { eq("user_id", userId) }
                        }
                        .decodeList<Map<String, Int>>() // Just need the post_ids
                    
                    val likedPostIds = userLikes.mapNotNull { it["post_id"] }.toSet()
                    results.forEach { it.isLiked = likedPostIds.contains(it.id) }
                }

                _posts.value = results
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

    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            try {
                val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                val currentPost = _posts.value.find { it.id == postId } ?: return@launch
                
                if (currentPost.isLiked) {
                    // Unlike
                    supabaseClient.postgrest["post_likes"].delete {
                        filter {
                            eq("post_id", postId)
                            eq("user_id", userId)
                        }
                    }
                } else {
                    // Like
                    val likeData = buildJsonObject {
                        put("post_id", postId)
                        put("user_id", userId)
                    }
                    supabaseClient.postgrest["post_likes"].insert(likeData)
                }
                
                // Note: likes_count in community_posts should be updated via Trigger in DB for accuracy.
                // If no trigger, we should manually update it here too.
                
                fetchPosts() // Refresh state
            } catch (e: Exception) {
                Log.e("CommunityDebug", "ToggleLike Error: ${e.localizedMessage}", e)
                _error.value = "좋아요 처리에 실패했습니다."
            }
        }
    }

    fun fetchComments(postId: Int) {
        viewModelScope.launch {
            try {
                val results = supabaseClient.postgrest["post_comments"]
                    .select {
                        filter { eq("post_id", postId) }
                        order("created_at", order = Order.ASCENDING)
                    }
                    .decodeList<PostComment>()
                
                val currentMap = _comments.value.toMutableMap()
                currentMap[postId] = results
                _comments.value = currentMap
            } catch (e: Exception) {
                Log.e("CommunityDebug", "FetchComments Error: ${e.localizedMessage}", e)
            }
        }
    }

    fun addComment(postId: Int, content: String) {
        viewModelScope.launch {
            try {
                val user = supabaseClient.auth.currentUserOrNull() ?: throw Exception("로그인이 필요합니다.")
                
                // Fetch user name for the comment
                val profile = supabaseClient.postgrest["profiles"]
                    .select { filter { eq("id", user.id) } }
                    .decodeSingleOrNull<UserProfile>()
                val userName = profile?.name ?: user.email ?: "익명 사용자"

                val commentData = buildJsonObject {
                    put("post_id", postId)
                    put("user_id", user.id)
                    put("user_name", userName)
                    put("content", content)
                }

                supabaseClient.postgrest["post_comments"].insert(commentData)
                
                fetchComments(postId)
                fetchPosts() // To update comments_count if tracked
            } catch (e: Exception) {
                Log.e("CommunityDebug", "AddComment Error: ${e.localizedMessage}", e)
                _error.value = "댓글 작성에 실패했습니다."
            }
        }
    }

    fun deleteComment(postId: Int, commentId: Long) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["post_comments"].delete {
                    filter { eq("id", commentId) }
                }
                fetchComments(postId)
                fetchPosts()
            } catch (e: Exception) {
                Log.e("CommunityDebug", "DeleteComment Error: ${e.localizedMessage}", e)
                _error.value = "댓글 삭제에 실패했습니다."
            }
        }
    }

    fun updatePost(postId: Int, category: String, title: String, content: String, imageBytes: ByteArray? = null, isImageRemoved: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("CommunityDebug", "UpdatePost: Starting update for ID $postId. Category: $category, Title: $title")
            try {
                var imageUrl: String? = null
                
                // 1. Upload new image if provided
                if (imageBytes != null) {
                    Log.d("CommunityDebug", "UpdatePost: Uploading new image (${imageBytes.size} bytes)...")
                    val user = supabaseClient.auth.currentUserOrNull() ?: throw Exception("로그인이 필요합니다.")
                    val fileName = "${user.id}_${System.currentTimeMillis()}.jpg"
                    val bucket = supabaseClient.storage["post-images"]
                    bucket.upload(fileName, imageBytes)
                    imageUrl = bucket.publicUrl(fileName)
                    Log.d("CommunityDebug", "UpdatePost: Image uploaded: $imageUrl")
                }

                // 2. Prepare update data as JSON object
                val updateData = buildJsonObject {
                    put("category", category)
                    put("title", title)
                    put("content", content)
                    
                    if (isImageRemoved) {
                        put("image_url", JsonNull)
                        Log.d("CommunityDebug", "UpdatePost: Removing image")
                    } else if (imageUrl != null) {
                        put("image_url", imageUrl)
                        Log.d("CommunityDebug", "UpdatePost: Updating image to $imageUrl")
                    }
                }

                Log.d("CommunityDebug", "UpdatePost: updateData JSON: $updateData")

                // 3. Update database
                Log.d("CommunityDebug", "UpdatePost: Executing database update...")
                supabaseClient.postgrest["community_posts"].update(updateData) {
                    filter { eq("id", postId) }
                }
                
                Log.d("CommunityDebug", "UpdatePost: Update successful")
                _isPostSuccess.value = true
                fetchPosts()
            } catch (e: Exception) {
                Log.e("CommunityDebug", "UpdatePost: Error: ${e.localizedMessage}", e)
                _error.value = "포스트 수정에 실패했습니다: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                supabaseClient.postgrest["community_posts"].delete {
                    filter { eq("id", postId) }
                }
                fetchPosts()
            } catch (e: Exception) {
                Log.e("CommunityDebug", "DeletePost Error: ${e.localizedMessage}", e)
                _error.value = "포스트 삭제에 실패했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
