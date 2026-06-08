package com.example.reservation_eeg_android_app.ui.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.Reservation
import com.example.reservation_eeg_android_app.model.ReservationStatus
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.OffsetDateTime

class AdminReservationViewModel : ViewModel() {
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredReservations: StateFlow<List<Reservation>> = combine(
        _reservations,
        _selectedDate,
        _searchQuery
    ) { reservations, date, query ->
        reservations.filter { res ->
            val resDate = try {
                OffsetDateTime.parse(res.reservedAt).toLocalDate()
            } catch (e: Exception) {
                null
            }
            val matchesDate = resDate == date
            val matchesQuery = res.patientName.contains(query, ignoreCase = true) || 
                             res.eegType.displayName.contains(query, ignoreCase = true)
            
            matchesDate && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Analytics: Reservations by day for the last 7 days
    val weeklyStats: StateFlow<Map<LocalDate, Int>> = _reservations.map { list ->
        val today = LocalDate.now()
        val last7Days = (0..6).map { today.minusDays(it.toLong()) }.reversed()
        
        last7Days.associateWith { date ->
            list.count { res ->
                try {
                    OffsetDateTime.parse(res.reservedAt).toLocalDate() == date
                } catch (e: Exception) {
                    false
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Analytics: Popular EEG Types
    val typeStats: StateFlow<Map<String, Int>> = _reservations.map { list ->
        list.groupBy { it.eegType.displayName }
            .mapValues { it.value.size }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        fetchAllReservations()
    }

    fun fetchAllReservations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val results = supabaseClient.postgrest["bookings"]
                    .select {
                        order("reserved_at", order = Order.DESCENDING)
                    }
                    .decodeList<Reservation>()
                
                _reservations.value = results
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to fetch reservations: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReservationStatus(reservationId: Int, newStatus: ReservationStatus) {
        viewModelScope.launch {
            try {
                val reservation = _reservations.value.find { it.id == reservationId }
                
                supabaseClient.postgrest["bookings"].update({
                    Reservation::status setTo newStatus
                }) {
                    filter { eq("id", reservationId) }
                }

                reservation?.userId?.let { uid ->
                    val statusText = when(newStatus) {
                        ReservationStatus.CONFIRMED -> "확정되었습니다"
                        ReservationStatus.COMPLETED -> "진료가 완료되었습니다"
                        ReservationStatus.CANCELLED -> "취소되었습니다"
                        else -> "상태가 변경되었습니다"
                    }
                    val notification = com.example.reservation_eeg_android_app.model.Notification(
                        userId = uid,
                        title = "예약 상태 변경",
                        message = "환자 ${reservation.patientName}님의 예약이 $statusText.",
                        relatedId = reservationId
                    )
                    supabaseClient.postgrest["notifications"].insert(notification)
                }

                fetchAllReservations()
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
