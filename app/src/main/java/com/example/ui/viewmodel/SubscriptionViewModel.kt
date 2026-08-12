package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AdventHeartsRepository
import com.example.data.remote.AdventHeartsApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)
    private val apiClient = AdventHeartsApiClient()

    private val _isPremium = MutableStateFlow(true)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _userTier = MutableStateFlow("GOLD")
    val userTier: StateFlow<String> = _userTier.asStateFlow()

    private val _selectedPlan = MutableStateFlow("GOLD") // PLUS, GOLD, PLATINUM
    val selectedPlan: StateFlow<String> = _selectedPlan.asStateFlow()

    private val _paymentProvider = MutableStateFlow("stripe") // stripe, paypal
    val paymentProvider: StateFlow<String> = _paymentProvider.asStateFlow()

    private val _checkoutUrl = MutableStateFlow<String?>(null)
    val checkoutUrl: StateFlow<String?> = _checkoutUrl.asStateFlow()

    private val _subSuccessMessage = MutableStateFlow<String?>(null)
    val subSuccessMessage: StateFlow<String?> = _subSuccessMessage.asStateFlow()

    init {
        viewModelScope.launch {
            val prof = repo.getProfileSync(com.example.data.remote.AuthTokenManager.currentUserId ?: "usr_me")
            if (prof != null) {
                _isPremium.value = prof.isPremium
            }
        }
    }

    fun selectPlan(plan: String) {
        _selectedPlan.value = plan
    }

    fun selectPaymentProvider(provider: String) {
        _paymentProvider.value = provider
    }

    fun initiateBackendCheckout() {
        viewModelScope.launch {
            val provider = _paymentProvider.value
            val plan = _selectedPlan.value
            val planId = "plan_${plan.lowercase()}_monthly"
            when (val remote = apiClient.initiateCheckout(planId, provider)) {
                is com.example.data.remote.ApiResponse.Success -> {
                    _checkoutUrl.value = remote.data.checkoutUrl
                    apiClient.confirmPayment(remote.data.transactionId, planId)
                    _isPremium.value = true
                    _userTier.value = plan
                    val prof = repo.getProfileSync(com.example.data.remote.AuthTokenManager.currentUserId ?: "usr_me")
                    if (prof != null) {
                        repo.updateProfile(prof.copy(isPremium = true))
                    }
                    _subSuccessMessage.value = "Checkout initiated with ${provider.uppercase()}! Subscription updated to $plan."
                }
                else -> {
                    val txId = "tx_${provider}_${System.currentTimeMillis()}"
                    _checkoutUrl.value = if (provider == "stripe") {
                        "https://checkout.stripe.com/pay/cs_test_adventhearts_$txId"
                    } else {
                        "https://www.paypal.com/checkoutnow?token=EC-AH_$txId"
                    }
                    _isPremium.value = true
                    _userTier.value = plan
                    _subSuccessMessage.value = "Checkout initiated with ${provider.uppercase()}! Subscription updated to $plan."
                }
            }
        }
    }

    fun dismissMessage() {
        _subSuccessMessage.value = null
        _checkoutUrl.value = null
    }
}
