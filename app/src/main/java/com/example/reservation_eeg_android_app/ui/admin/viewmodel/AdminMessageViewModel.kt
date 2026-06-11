package com.example.reservation_eeg_android_app.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.Notification
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map

class AdminMessageViewModel : ViewModel() {
    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList())
    
    // Group identical notifications so the Admin only sees ONE entry for a broadcast
    val allNotifications: StateFlow<List<Notification>> = _allNotifications.map { list ->
        list.distinctBy { it.title + it.message + (it.createdAt?.take(16) ?: "") }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchAllNotifications()
    }

    fun fetchAllNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Admins fetch all notifications
                val results = supabaseClient.postgrest["notifications"]
                    .select {
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<Notification>()
                _allNotifications.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendBroadcast(title: String, message: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Fetch all users
                val users = supabaseClient.postgrest["profiles"]
                    .select()
                    .decodeList<UserProfile>()
                
                // 2. Create notification for each user
                val notifications = users.mapNotNull { user ->
                    user.id?.let { uid ->
                        Notification(
                            userId = uid,
                            title = title,
                            message = message
                        )
                    }
                }
                
                if (notifications.isNotEmpty()) {
                    supabaseClient.postgrest["notifications"].insert(notifications)
                }
                
                fetchAllNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["notifications"].delete {
                    filter { eq("id", id) }
                }
                fetchAllNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
