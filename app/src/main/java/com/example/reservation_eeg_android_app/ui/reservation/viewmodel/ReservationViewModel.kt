package com.example.reservation_eeg_android_app.ui.reservation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.Reservation
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val _selectedType = MutableStateFlow<EegType?>(null)
    val selectedType: StateFlow<EegType?> = _selectedType.asStateFlow()

    private val _symptoms = MutableStateFlow("")
    val symptoms: StateFlow<String> = _symptoms.asStateFlow()

    private val _bookedSlots = MutableStateFlow<List<Instant>>(emptyList())
    val bookedSlots: StateFlow<List<Instant>> = _bookedSlots.asStateFlow()

    private val _userReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val userReservations: StateFlow<List<Reservation>> = _userReservations.asStateFlow()

    private val _isReservationSuccess = MutableStateFlow(false)
    val isReservationSuccess: StateFlow<Boolean> = _isReservationSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now(SupabaseConfig.KST_ZONE_ID))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private var editingReservationId: Int? = null

    init {
        fetchBookedSlots()
    }

    fun startEditing(reservation: Reservation) {
        editingReservationId = reservation.id
        _selectedType.value = reservation.eegType
        _symptoms.value = reservation.symptoms
        // Extract date from ISO string: "2026-06-10T..."
        try {
            val datePart = reservation.reservedAt.split("T")[0]
            _selectedDate.value = LocalDate.parse(datePart)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isReservationSuccess.value = false
        _isEditing.value = true
    }

    fun clearEditing() {
        editingReservationId = null
        _selectedType.value = null
        _symptoms.value = ""
        _selectedDate.value = LocalDate.now(SupabaseConfig.KST_ZONE_ID)
        _isReservationSuccess.value = false
        _isEditing.value = false
    }

    fun fetchBookedSlots() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val results = supabaseClient.postgrest["bookings"]
                    .select()
                    .decodeList<Reservation>()
                println("Fetched ${results.size} bookings from Supabase")
                _bookedSlots.value = results.mapNotNull { 
                    try { OffsetDateTime.parse(it.reservedAt).toInstant() } catch (e: Exception) { null } 
                }
                
                val currentUserId = supabaseClient.auth.currentSessionOrNull()?.user?.id
                _userReservations.value = if (currentUserId != null) {
                    results.filter { it.userId == currentUserId }
                } else {
                    results.filter { it.patientName == TEST_PATIENT }
                }.sortedByDescending { it.reservedAt }
                
                println("Filtered and sorted to ${_userReservations.value.size} reservations for user")
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = "목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun deleteReservation(id: Int) {
        _isLoading.value = true
        _error.value = null
        try {
            println("Deleting reservation with id: $id")
            supabaseClient.postgrest["bookings"].delete {
                filter {
                    eq("id", id)
                }
            }
            println("Delete request sent successfully for id: $id")
            fetchBookedSlots()
        } catch (e: Exception) {
            e.printStackTrace()
            _error.value = "삭제에 실패했습니다: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun updateReservation(id: Int, newSlot: String) {
        val type = _selectedType.value ?: return
        val symptomText = _symptoms.value
        val date = _selectedDate.value
        _isLoading.value = true
        try {
            val updatedReservedAt = "${date}T$newSlot:00${SupabaseConfig.KST_OFFSET}"
            supabaseClient.postgrest["bookings"].update({
                Reservation::reservedAt setTo updatedReservedAt
                Reservation::eegType setTo type
                Reservation::symptoms setTo symptomText
            }) {
                filter {
                    eq("id", id)
                }
            }
            fetchBookedSlots()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun selectType(type: EegType) {
        _selectedType.value = type
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun resetSuccess() {
        _isReservationSuccess.value = false
    }

    fun updateSymptoms(text: String) {
        _symptoms.value = text
    }

    fun completeReservation(slot: String) {
        val type = _selectedType.value ?: return
        val symptomText = _symptoms.value
        val date = _selectedDate.value
        val reservedAt = "${date}T$slot:00${SupabaseConfig.KST_OFFSET}"
        
        viewModelScope.launch {
            try {
                if (editingReservationId != null) {
                    updateReservation(editingReservationId!!, slot)
                    _isReservationSuccess.value = true
                    editingReservationId = null
                } else {
                    val reservedInstant = OffsetDateTime.parse(reservedAt).toInstant()
                    
                    // Check locally first for NEW reservations
                    if (_bookedSlots.value.contains(reservedInstant)) {
                        println("Slot already booked")
                        return@launch
                    }

                    val currentUserId = supabaseClient.auth.currentSessionOrNull()?.user?.id
                    val currentUserEmail = supabaseClient.auth.currentSessionOrNull()?.user?.email

                    val reservation = Reservation(
                        userId = currentUserId,
                        patientName = currentUserEmail ?: TEST_PATIENT,
                        eegType = type,
                        symptoms = symptomText,
                        reservedAt = reservedAt
                    )
                    
                    try {
                        supabaseClient.postgrest["bookings"].insert(reservation)
                        _isReservationSuccess.value = true
                        fetchBookedSlots() // Refresh booked slots
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // This handles the "duplicate key" error if slot was taken between fetch and insert
                        _isLoading.value = false
                        // You might want to update a state to show a "Slot already taken" message
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }

    companion object {
        const val TEST_PATIENT = "Test Patient"
    }
}
