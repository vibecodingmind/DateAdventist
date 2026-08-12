package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ReportEntity
import com.example.data.remote.AuthTokenManager
import com.example.data.repository.AdventHeartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)

    val blockedUserIds: StateFlow<List<String>> = repo.getBlockedUserIds(AuthTokenManager.currentUserId ?: "usr_me")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allReports: StateFlow<List<ReportEntity>> = repo.getAllReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _safetyMessage = MutableStateFlow<String?>(null)
    val safetyMessage: StateFlow<String?> = _safetyMessage.asStateFlow()

    fun reportUser(reportedUserId: String, reason: String, details: String) {
        viewModelScope.launch {
            repo.reportUser(AuthTokenManager.currentUserId ?: "usr_me", reportedUserId, reason, details)
            _safetyMessage.value = "Thank you. Your report has been submitted to AdventHearts Moderation for immediate review."
        }
    }

    fun blockUser(blockedUserId: String) {
        viewModelScope.launch {
            repo.blockUser(AuthTokenManager.currentUserId ?: "usr_me", blockedUserId)
            _safetyMessage.value = "User blocked. You will no longer see or receive messages from this profile."
        }
    }

    fun submitSelfieVerification(selfieUri: String) {
        viewModelScope.launch {
            val prof = repo.getProfileSync(AuthTokenManager.currentUserId ?: "usr_me")
            if (prof != null) {
                val updated = prof.copy(
                    verificationStatus = "PENDING",
                    verificationSelfieUri = selfieUri
                )
                repo.updateProfile(updated)
                _safetyMessage.value = "Selfie verification submitted! Our moderation team will verify your identity within 24 hours."
            }
        }
    }

    fun dismissMessage() {
        _safetyMessage.value = null
    }
}
