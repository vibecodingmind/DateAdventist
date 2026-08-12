package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProfileEntity
import com.example.data.local.UserAccountEntity
import com.example.data.repository.AdventHeartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _onboardingStep = MutableStateFlow(0) // 0 = Completed/LoggedIn, 1..11 = Onboarding steps
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    val currentProfile: StateFlow<ProfileEntity?> = _currentUserId.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repo.getProfileFlow(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    init {
        com.example.data.remote.AuthTokenManager.init(application)
        val savedUserId = com.example.data.remote.AuthTokenManager.currentUserId ?: "usr_me"
        _currentUserId.value = savedUserId

        com.example.data.remote.AuthTokenManager.setOnUnauthorizedListener {
            logout()
        }

        viewModelScope.launch {
            repo.seedDatabaseIfEmpty()
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _authError.value = null
            if (email.isBlank() || pass.isBlank()) {
                _authError.value = "Please enter both email and password."
                return@launch
            }
            val account = repo.getUserAccountByEmail(email.trim())
            if (account != null) {
                val dummyJwtToken = "jwt_token_${account.userId}_${System.currentTimeMillis()}"
                com.example.data.remote.AuthTokenManager.saveTokens(dummyJwtToken, "refresh_$dummyJwtToken", account.userId)
                _currentUserId.value = account.userId
                _onboardingStep.value = 0
            } else {
                _authError.value = "Invalid credentials. Please check your email or password."
            }
        }
    }

    fun register(
        fullName: String,
        email: String,
        pass: String,
        age: Int,
        gender: String,
        country: String,
        city: String,
        intention: String
    ) {
        viewModelScope.launch {
            _authError.value = null
            if (age < 18) {
                _authError.value = "AdventHearts is strictly for members 18 years of age or older."
                return@launch
            }
            val existing = repo.getUserAccountByEmail(email.trim())
            if (existing != null) {
                _authError.value = "An account with this email already exists."
                return@launch
            }

            val newId = "usr_${System.currentTimeMillis()}"
            val userAcc = UserAccountEntity(
                userId = newId,
                email = email.trim(),
                passwordHash = pass,
                role = "USER",
                isEmailVerified = true
            )

            val newProfile = ProfileEntity(
                userId = newId,
                fullName = fullName,
                age = age,
                gender = gender,
                country = country,
                city = city,
                occupation = "Adventist Professional",
                education = "University",
                bio = "Faithful Adventist looking for a Christian partner to share Sabbath, life, and ministry.",
                relationshipIntention = intention,
                primaryPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                photoUrls = listOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80"),
                isVerified = false,
                verificationStatus = "NOT_VERIFIED",
                isPremium = false,
                adventistAffiliation = "Seventh-day Adventist Member",
                yearsAsAdventist = age - 5,
                localChurch = "Local SDA Church",
                isBaptized = true,
                faithImportance = "Central to everything I do",
                churchInvolvement = "Active",
                sabbathObservance = listOf("Church Service", "Sunset to Sunset Rest", "Nature Walks"),
                ministryInterests = listOf("Youth", "Bible Study"),
                personalBibleStudy = "Daily",
                favoriteVerse = "John 3:16",
                diet = "Vegetarian",
                alcohol = "None / Abstain",
                smoking = "Never",
                wantsChildren = "Yes, definitely",
                hasChildren = false,
                interests = listOf("Sabbath Nature Walks", "Bible Study", "A cappella Music")
            )

            repo.registerUser(userAcc, newProfile)
            val dummyJwtToken = "jwt_token_${newId}_${System.currentTimeMillis()}"
            com.example.data.remote.AuthTokenManager.saveTokens(dummyJwtToken, "refresh_$dummyJwtToken", newId)
            _currentUserId.value = newId
            _onboardingStep.value = 1 // Proceed to profile completion steps
        }
    }

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun updateProfileData(updated: ProfileEntity) {
        viewModelScope.launch {
            repo.updateProfile(updated)
        }
    }

    fun switchAccount(userId: String) {
        val dummyJwtToken = "jwt_token_${userId}_${System.currentTimeMillis()}"
        com.example.data.remote.AuthTokenManager.saveTokens(dummyJwtToken, "refresh_$dummyJwtToken", userId)
        _currentUserId.value = userId
        _onboardingStep.value = 0
    }

    fun logout() {
        com.example.data.remote.AuthTokenManager.clearTokens()
        _currentUserId.value = null
        _onboardingStep.value = 0
    }

    fun clearError() {
        _authError.value = null
    }
}
