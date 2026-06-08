package com.example.reservation_eeg_android_app.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.BlockedSlot
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime

class AdminScheduleViewModel : ViewModel() {
    private val _blockedSlots = MutableStateFlow<List<BlockedSlot>>(emptyList())
    val blockedSlots: StateFlow<List<BlockedSlot>> = _blockedSlots.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    init {
        fetchBlockedSlots()
    }

    fun fetchBlockedSlots() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = supabaseClient.postgrest["blocked_slots"]
                    .select {
                        order("blocked_at", order = Order.DESCENDING)
                    }
                    .decodeList<BlockedSlot>()
                _blockedSlots.value = results
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun blockSlot(slot: String, reason: String) {
        val date = _selectedDate.value
        val blockedAt = "${date}T$slot:00${SupabaseConfig.KST_OFFSET}"
        
        viewModelScope.launch {
            try {
                val newBlock = BlockedSlot(blockedAt = blockedAt, reason = reason)
                supabaseClient.postgrest["blocked_slots"].insert(newBlock)
                fetchBlockedSlots()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun unblockSlot(id: Int) {
        viewModelScope.launch {
            try {
                supabaseClient.postgrest["blocked_slots"].delete {
                    filter { eq("id", id) }
                }
                fetchBlockedSlots()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }
}
