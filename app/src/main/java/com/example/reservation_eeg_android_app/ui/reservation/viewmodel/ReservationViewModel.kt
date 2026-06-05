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

    fun selectType(type: EegType) {
        _selectedType.value = type
    }

    fun updateSymptoms(text: String) {
        _symptoms.value = text
    }

    fun completeReservation(slot: String) {
        val type = _selectedType.value ?: return
        val symptomText = _symptoms.value
        
        viewModelScope.launch {
            try {
                val reservation = Reservation(
                    patientName = "Test Patient", // Replace with real patient name
                    eegType = type,
                    symptoms = symptomText,
                    reservedAt = "2026-06-10T$slot"
                )
                
                supabaseClient.postgrest["bookings"].insert(reservation)
                // Handle success (e.g., navigate to confirmation)
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle error
            }
        }
    }
}
