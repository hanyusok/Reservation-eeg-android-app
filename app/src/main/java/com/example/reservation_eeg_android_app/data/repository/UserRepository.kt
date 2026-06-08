package com.example.reservation_eeg_android_app.data.repository

import com.example.reservation_eeg_android_app.data.supabaseClient
import com.example.reservation_eeg_android_app.model.FamilyMember
import com.example.reservation_eeg_android_app.model.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserRepository {
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()

    suspend fun fetchProfile(userId: String): UserProfile? {
        return try {
            val profile = supabaseClient.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<UserProfile>()
            _userProfile.value = profile ?: UserProfile(id = userId, email = supabaseClient.auth.currentUserOrNull()?.email ?: "")
            _userProfile.value
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateProfile(profile: UserProfile): Boolean {
        return try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id ?: profile.id ?: ""
            val profileToUpdate = profile.copy(id = currentUserId)
            supabaseClient.postgrest["profiles"].upsert(profileToUpdate)
            _userProfile.value = profileToUpdate
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun fetchFamilyMembers(): List<FamilyMember> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return emptyList()
        return try {
            val members = supabaseClient.postgrest["family_members"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<FamilyMember>()
            _familyMembers.value = members
            members
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun addFamilyMember(member: FamilyMember): Boolean {
        val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return false
        return try {
            val memberWithUserId = member.copy(userId = userId)
            supabaseClient.postgrest["family_members"].insert(memberWithUserId)
            fetchFamilyMembers()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateFamilyMember(member: FamilyMember): Boolean {
        return try {
            supabaseClient.postgrest["family_members"].update(member) {
                filter { eq("id", member.id ?: 0) }
            }
            fetchFamilyMembers()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteFamilyMember(memberId: Int): Boolean {
        return try {
            supabaseClient.postgrest["family_members"].delete {
                filter { eq("id", memberId) }
            }
            fetchFamilyMembers()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearData() {
        _userProfile.value = null
        _familyMembers.value = emptyList()
    }
}
