package com.example.reservation_eeg_android_app.ui.reservation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.SupabaseConfig
import com.example.reservation_eeg_android_app.data.repository.UserRepository
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.model.Reservation
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val _selectedType = MutableStateFlow<EegType?>(null)
    val selectedType: StateFlow<EegType?> = _selectedType.asStateFlow()

    private val _patientName = MutableStateFlow("")
    val patientName: StateFlow<String> = _patientName.asStateFlow()

    val userName: StateFlow<String> = UserRepository.userProfile.map { it?.name ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val familyMembers: StateFlow<List<FamilyMember>> = UserRepository.familyMembers

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

    private val _selectedDate = MutableStateFlow(LocalDate.now(SupabaseConfig.KST_ZONE_ID))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _originalReservedAt = MutableStateFlow<Instant?>(null)
    val originalReservedAt: StateFlow<Instant?> = _originalReservedAt.asStateFlow()

    private var editingReservationId: Int? = null

    init {
        fetchBookedSlots()
        
        // Initialize patient name from profile
        viewModelScope.launch {
            UserRepository.userProfile.collect { profile ->
                if (profile != null && _patientName.value.isEmpty()) {
                    _patientName.value = profile.name
                }
            }
        }
    }

    fun fetchFamilyMembers() {
        viewModelScope.launch {
            UserRepository.fetchFamilyMembers()
        }
    }

    fun selectPatient(name: String) {
        _patientName.value = name
    }

    fun startEditing(reservation: Reservation) {
        editingReservationId = reservation.id
        _selectedType.value = reservation.eegType
        _patientName.value = reservation.patientName
        _symptoms.value = reservation.symptoms
        // Extract date from ISO string: "2026-06-10T..."
        try {
            val datePart = reservation.reservedAt.split("T")[0]
            _selectedDate.value = LocalDate.parse(datePart)
            _originalReservedAt.value = OffsetDateTime.parse(reservation.reservedAt).toInstant()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _isReservationSuccess.value = false
    }

    fun clearEditing() {
        editingReservationId = null
        _selectedType.value = null
        _symptoms.value = ""
        // but maybe we should reset it to the user's name.
        _patientName.value = UserRepository.userProfile.value?.name ?: ""
        _selectedDate.value = LocalDate.now(SupabaseConfig.KST_ZONE_ID)
        _originalReservedAt.value = null
        _isReservationSuccess.value = false
    }

    fun fetchBookedSlots() {
        val currentUserId = supabaseClient.auth.currentSessionOrNull()?.user?.id
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch ALL bookings for slot checking
                val allBookings = supabaseClient.postgrest["bookings"]
                    .select()
                    .decodeList<Reservation>()

                // Fetch admin-blocked slots
                val blockedResults = try {
                    supabaseClient.postgrest["blocked_slots"]
                        .select()
                        .decodeList<com.example.reservation_eeg_android_app.model.BlockedSlot>()
                } catch (e: Exception) { emptyList() }

                val bookingInstants = allBookings.mapNotNull { 
                    try { OffsetDateTime.parse(it.reservedAt).toInstant() } catch (_: Exception) { null } 
                }
                val blockedInstants = blockedResults.mapNotNull {
                    try { OffsetDateTime.parse(it.blockedAt).toInstant() } catch (_: Exception) { null }
                }

                _bookedSlots.value = bookingInstants + blockedInstants
                
                // Fetch only USER'S reservations using server-side filtering and ordering
                if (currentUserId != null) {
                    val userResults = supabaseClient.postgrest["bookings"]
                        .select {
                            filter {
                                eq("user_id", currentUserId)
                            }
                            order("reserved_at", order = Order.DESCENDING)
                        }
                        .decodeList<Reservation>()
                    _userReservations.value = userResults
                } else {
                    _userReservations.value = emptyList()
                }
                
                println("Fetched and filtered reservations successfully")
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
        val patient = _patientName.value
        val symptomText = _symptoms.value
        val date = _selectedDate.value
        _isLoading.value = true
        try {
            val updatedReservedAt = "${date}T$newSlot:00${SupabaseConfig.KST_OFFSET}"
            supabaseClient.postgrest["bookings"].update({
                Reservation::reservedAt setTo updatedReservedAt
                Reservation::eegType setTo type
                Reservation::patientName setTo patient
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
        val patient = _patientName.value
        val symptomText = _symptoms.value
        val date = _selectedDate.value
        val reservedAt = "${date}T$slot:00${SupabaseConfig.KST_OFFSET}"
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (editingReservationId != null) {
                    updateReservation(editingReservationId!!, slot)
                    _isReservationSuccess.value = true
                    editingReservationId = null
                } else {
                    val reservedInstant = OffsetDateTime.parse(reservedAt).toInstant()
                    
                    if (_bookedSlots.value.contains(reservedInstant)) {
                        _error.value = "이미 예약된 시간대입니다."
                        _isLoading.value = false
                        return@launch
                    }

                    val currentUserId = supabaseClient.auth.currentSessionOrNull()?.user?.id ?: return@launch
                    val finalPatientName = patient.ifBlank { supabaseClient.auth.currentSessionOrNull()?.user?.email ?: "" }

                    val reservation = Reservation(
                        userId = currentUserId,
                        patientName = finalPatientName,
                        eegType = type,
                        symptoms = symptomText,
                        reservedAt = reservedAt
                    )
                    
                    supabaseClient.postgrest["bookings"].insert(reservation)
                    _isReservationSuccess.value = true
                    fetchBookedSlots() 
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMessage = e.message ?: ""
                if (errorMessage.contains("23505") || errorMessage.contains("duplicate", ignoreCase = true)) {
                    _error.value = "이미 예약된 시간대입니다. 다른 시간을 선택해주세요."
                } else {
                    _error.value = "예약 처리 중 오류가 발생했습니다: ${e.localizedMessage}"
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

}
