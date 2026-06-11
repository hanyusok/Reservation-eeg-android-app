package com.example.reservation_eeg_android_app.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.repository.UserRepository
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Kakao
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val userProfile: StateFlow<UserProfile?> = UserRepository.userProfile
    val familyMembers: StateFlow<List<FamilyMember>> = UserRepository.familyMembers

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    val userId = status.session.user?.id ?: ""
                    UserRepository.fetchProfile(userId)
                    UserRepository.fetchFamilyMembers()
                } else {
                    UserRepository.clearData()
                }
            }
        }
    }

    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            UserRepository.fetchProfile(userId)
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            if (UserRepository.updateProfile(profile)) {
                _updateSuccess.value = true
            } else {
                _error.value = "프로필 저장에 실패했습니다."
            }
            
            _isLoading.value = false
        }
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                supabaseClient.auth.signUpWith(Email) {
                    this.email = email.trim()
                    this.password = password.trim()
                }
            } catch (e: Exception) {
                _error.value = "Sign up failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                supabaseClient.auth.signInWith(Email) {
                    this.email = email.trim()
                    this.password = password.trim()
                }
            } catch (e: Exception) {
                _error.value = "Sign in failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                supabaseClient.auth.signInWith(Google, redirectUrl = "supabase://callback")
            } catch (e: Exception) {
                _error.value = "Google Sign in failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signInWithKakao() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                supabaseClient.auth.signInWith(Kakao, redirectUrl = "supabase://callback")
            } catch (e: Exception) {
                _error.value = "Kakao Sign in failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                supabaseClient.auth.signOut()
            } catch (e: Exception) {
                _error.value = "Sign out failed: ${e.localizedMessage}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun fetchFamilyMembers() {
        viewModelScope.launch {
            UserRepository.fetchFamilyMembers()
        }
    }

    fun addFamilyMember(member: FamilyMember) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!UserRepository.addFamilyMember(member)) {
                _error.value = "Failed to add family member"
            }
            _isLoading.value = false
        }
    }

    fun updateFamilyMember(member: FamilyMember) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!UserRepository.updateFamilyMember(member)) {
                _error.value = "Failed to update family member"
            }
            _isLoading.value = false
        }
    }

    fun deleteFamilyMember(memberId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            if (!UserRepository.deleteFamilyMember(memberId)) {
                _error.value = "Failed to delete family member"
            }
            _isLoading.value = false
        }
    }
}
