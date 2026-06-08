package com.example.reservation_eeg_android_app.ui.notification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.Notification
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = supabaseClient.postgrest["notifications"]
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<Notification>()
                _notifications.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "알림을 불러오지 못했습니다."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["notifications"].update({
                    Notification::isRead setTo true
                }) {
                    filter { eq("id", notificationId) }
                }
                fetchNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["notifications"].delete {
                    filter { eq("id", notificationId) }
                }
                fetchNotifications()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
