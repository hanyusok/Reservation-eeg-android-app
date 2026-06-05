package com.example.reservation_eeg_android_app.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Kakao
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    val sessionStatus: StateFlow<SessionStatus> = supabaseClient.auth.sessionStatus

    init {
        viewModelScope.launch {
            sessionStatus.collect { status ->
                if (status is SessionStatus.Authenticated) {
                    fetchProfile(status.session.user?.id ?: "")
                } else {
                    _userProfile.value = null
                }
            }
        }
    }

    fun fetchProfile(userId: String) {
        viewModelScope.launch {
            try {
                val profile = supabaseClient.postgrest["profiles"]
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingleOrNull<UserProfile>()
                _userProfile.value = profile ?: UserProfile(id = userId, email = supabaseClient.auth.currentUserOrNull()?.email ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _updateSuccess.value = false
            try {
                // If this is a new profile and the user entered an email for the first time
                // We should ensure the profile has the current user ID
                val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: profile.id
                val profileToUpdate = profile.copy(id = currentUserId)
                
                supabaseClient.postgrest["profiles"].upsert(profileToUpdate)
                _userProfile.value = profileToUpdate
                _updateSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Profile update failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
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
}
