package com.example.reservation_eeg_android_app.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.UserProfile
import com.example.reservation_eeg_android_app.model.Reservation
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminUserViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedUserHistory = MutableStateFlow<List<Reservation>>(emptyList())
    val selectedUserHistory: StateFlow<List<Reservation>> = _selectedUserHistory.asStateFlow()

    private val _selectedUserNotifications = MutableStateFlow<List<com.example.reservation_eeg_android_app.model.Notification>>(emptyList())
    val selectedUserNotifications: StateFlow<List<com.example.reservation_eeg_android_app.model.Notification>> = _selectedUserNotifications.asStateFlow()

    val filteredUsers: StateFlow<List<UserProfile>> = combine(
        _users,
        _searchQuery
    ) { users, query ->
        if (query.isBlank()) users
        else users.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.email.contains(query, ignoreCase = true) ||
            it.phoneNumber.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        fetchAllUsers()
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = supabaseClient.postgrest["profiles"]
                    .select()
                    .decodeList<UserProfile>()
                _users.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun fetchUserHistory(userId: String) {
        viewModelScope.launch {
            try {
                // 1. Fetch Reservations
                val history = supabaseClient.postgrest["bookings"]
                    .select {
                        filter { eq("user_id", userId) }
                        order("reserved_at", order = Order.DESCENDING)
                    }
                    .decodeList<Reservation>()
                _selectedUserHistory.value = history

                // 2. Fetch Notifications sent to this user
                val notifications = supabaseClient.postgrest["notifications"]
                    .select {
                        filter { eq("user_id", userId) }
                        order("created_at", order = Order.DESCENDING)
                    }
                    .decodeList<com.example.reservation_eeg_android_app.model.Notification>()
                _selectedUserNotifications.value = notifications
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun sendNotification(userId: String, title: String, message: String) {
        viewModelScope.launch {
            try {
                val notification = com.example.reservation_eeg_android_app.model.Notification(
                    userId = userId,
                    title = title,
                    message = message
                )
                supabaseClient.postgrest["notifications"].insert(notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
