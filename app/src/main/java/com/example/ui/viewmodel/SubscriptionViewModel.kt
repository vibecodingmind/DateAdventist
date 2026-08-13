package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.remote.AdventHeartsApiClient
import com.example.data.remote.ApiResponse
import com.example.data.remote.AuthTokenManager
import com.example.data.repository.AdventHeartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)
    private val apiClient = AdventHeartsApiClient()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _userTier = MutableStateFlow("FREE")
    val userTier: StateFlow<String> = _userTier.asStateFlow()

    private val _selectedPlan = MutableStateFlow("GOLD")
    val selectedPlan: StateFlow<String> = _selectedPlan.asStateFlow()

    private val _paymentProvider = MutableStateFlow("stripe")
    val paymentProvider: StateFlow<String> = _paymentProvider.asStateFlow()

    private val _checkoutUrl = MutableStateFlow<String?>(null)
    val checkoutUrl: StateFlow<String?> = _checkoutUrl.asStateFlow()

    private val _subSuccessMessage = MutableStateFlow<String?>(null)
    val subSuccessMessage: StateFlow<String?> = _subSuccessMessage.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        refreshSubscription()
    }

    fun selectPlan(plan: String) {
        _selectedPlan.value = plan
    }

    fun selectPaymentProvider(provider: String) {
        _paymentProvider.value = provider
    }

    fun refreshSubscription() {
        viewModelScope.launch {
            when (val remote = apiClient.fetchCurrentSubscription()) {
                is ApiResponse.Success -> {
                    _isPremium.value = remote.data.active
                    _userTier.value = remote.data.tier
                }
                else -> {
                    val userId = AuthTokenManager.currentUserId
                    val prof = if (userId != null) repo.getProfileSync(userId) else null
                    if (prof != null) {
                        _isPremium.value = prof.isPremium
                        _userTier.value = if (prof.isPremium) "GOLD" else "FREE"
                    }
                }
            }
        }
    }

    fun initiateBackendCheckout() {
        viewModelScope.launch {
            _isProcessing.value = true
            _error.value = null
            val provider = _paymentProvider.value
            val plan = _selectedPlan.value
            val planId = "plan_${plan.lowercase()}_monthly"
            when (val remote = apiClient.initiateCheckout(planId, provider)) {
                is ApiResponse.Success -> {
                    _checkoutUrl.value = remote.data.checkoutUrl
                    _subSuccessMessage.value =
                        "Complete checkout in your browser, then tap “I’ve finished paying” to refresh your membership."
                }
                is ApiResponse.Error -> {
                    _error.value = remote.message
                    _subSuccessMessage.value = "Could not start checkout: ${remote.message}"
                }
            }
            _isProcessing.value = false
        }
    }

    fun onCheckoutReturned() {
        refreshSubscription()
        _subSuccessMessage.value = "Checking your membership status…"
    }

    fun dismissMessage() {
        _subSuccessMessage.value = null
        _checkoutUrl.value = null
        _error.value = null
    }
}
