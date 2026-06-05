package com.example.reservation_eeg_android_app.ui.reservation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.EegType
import com.example.reservation_eeg_android_app.model.Reservation
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationViewModel : ViewModel() {
    private val _selectedType = MutableStateFlow<EegType?>(null)
    val selectedType: StateFlow<EegType?> = _selectedType.asStateFlow()

    private val _symptoms = MutableStateFlow("")
    val symptoms: StateFlow<String> = _symptoms.asStateFlow()

    private val _bookedSlots = MutableStateFlow<List<String>>(emptyList())
    val bookedSlots: StateFlow<List<String>> = _bookedSlots.asStateFlow()

    private val _userReservations = MutableStateFlow<List<Reservation>>(emptyList())
    val userReservations: StateFlow<List<Reservation>> = _userReservations.asStateFlow()

    private val _isReservationSuccess = MutableStateFlow(false)
    val isReservationSuccess: StateFlow<Boolean> = _isReservationSuccess.asStateFlow()

    private var editingReservationId: Int? = null

    init {
        fetchBookedSlots()
    }

    fun startEditing(reservation: Reservation) {
        editingReservationId = reservation.id
        _selectedType.value = reservation.eegType
        _symptoms.value = reservation.symptoms
        _isReservationSuccess.value = false
    }

    fun clearEditing() {
        editingReservationId = null
        _selectedType.value = null
        _symptoms.value = ""
        _isReservationSuccess.value = false
    }

    fun fetchBookedSlots() {
        viewModelScope.launch {
            try {
                val results = supabaseClient.postgrest["bookings"]
                    .select()
                    .decodeList<Reservation>()
                _bookedSlots.value = results.map { it.reservedAt }
                // For now, assume all bookings belong to the current "Test Patient"
                _userReservations.value = results.filter { it.patientName == "Test Patient" }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteReservation(id: Int) {
        try {
            supabaseClient.postgrest["bookings"].delete {
                filter {
                    eq("id", id)
                }
            }
            fetchBookedSlots() // Refresh lists
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun updateReservation(id: Int, newSlot: String) {
        try {
            val updatedReservedAt = "2026-06-10T$newSlot"
            supabaseClient.postgrest["bookings"].update({
                Reservation::reservedAt setTo updatedReservedAt
            }) {
                filter {
                    eq("id", id)
                }
            }
            fetchBookedSlots()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectType(type: EegType) {
        _selectedType.value = type
    }

    fun updateSymptoms(text: String) {
        _symptoms.value = text
    }

    fun completeReservation(slot: String) {
        val type = _selectedType.value ?: return
        val symptomText = _symptoms.value
        val reservedAt = "2026-06-10T$slot"
        
        viewModelScope.launch {
            try {
                if (editingReservationId != null) {
                    updateReservation(editingReservationId!!, slot)
                    _isReservationSuccess.value = true
                    editingReservationId = null
                } else {
                    // Check locally first for NEW reservations
                    if (_bookedSlots.value.contains(reservedAt)) {
                        println("Slot already booked")
                        return@launch
                    }

                    val reservation = Reservation(
                        patientName = "Test Patient", // Replace with real patient name
                        eegType = type,
                        symptoms = symptomText,
                        reservedAt = reservedAt
                    )
                    
                    supabaseClient.postgrest["bookings"].insert(reservation)
                    _isReservationSuccess.value = true
                    fetchBookedSlots() // Refresh booked slots
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }
}
