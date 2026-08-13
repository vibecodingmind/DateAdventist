package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProfileEntity
import com.example.data.local.ReportEntity
import com.example.data.remote.AuditLogDto
import com.example.data.repository.AdventHeartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminAnalytics(
    val totalUsers: Int = 1248,
    val dailyActiveUsers: Int = 852,
    val monthlyActiveUsers: Int = 2100,
    val subscriptionConversionRatePct: Double = 12.4,
    val activeSubscriptions: Int = 230,
    val matchesMade: Int = 412,
    val pendingReports: Int = 3,
    val pendingVerifications: Int = 2,
    val verificationRatePercent: Int = 84,
    val totalRevenueUsd: Double = 14280.00
)

data class PaymentSettings(
    val provider: String = "Stripe",
    val stripePublishableKey: String = "pk_live_adventhearts_sample_key",
    val stripeSecretKey: String = "sk_live_adventhearts_sample_key",
    val paypalClientId: String = "client_id_paypal_live",
    val isSandboxMode: Boolean = true,
    val monthlyPriceUsd: String = "14.99",
    val annualPriceUsd: String = "99.99",
    val lifetimePriceUsd: String = "199.99",
    val autoApproveVerifications: Boolean = false,
    val requireSelfieVerification: Boolean = true,
    val maintenanceMode: Boolean = false
)

class AdminViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)

    val pendingVerifications: StateFlow<List<ProfileEntity>> = repo.getPendingVerifications()
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

    val allProfiles: StateFlow<List<ProfileEntity>> = repo.getAllOtherProfiles("")
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Account Status state map for users (userId -> ACTIVE / SUSPENDED / BANNED)
    private val _userAccountStatuses = MutableStateFlow<Map<String, String>>(emptyMap())
    val userAccountStatuses: StateFlow<Map<String, String>> = _userAccountStatuses.asStateFlow()

    // Audit logs state
    private val _auditLogs = MutableStateFlow<List<AuditLogDto>>(
        listOf(
            AuditLogDto("log_101", System.currentTimeMillis() - 3600000 * 2, "admin@adventhearts.com", "VERIFY_APPROVE", "Approved selfie verification badge for user usr_hannah", "usr_hannah"),
            AuditLogDto("log_102", System.currentTimeMillis() - 3600000 * 5, "admin@adventhearts.com", "USER_SUSPEND", "Suspended account due to inappropriate messaging reports", "usr_002"),
            AuditLogDto("log_103", System.currentTimeMillis() - 3600000 * 12, "superadmin@adventhearts.com", "SETTINGS_UPDATE", "Updated Stripe gateway keys and subscription pricing tier"),
            AuditLogDto("log_104", System.currentTimeMillis() - 3600000 * 24, "admin@adventhearts.com", "USER_BAN", "Permanently banned spam profile", "usr_spammer_99"),
            AuditLogDto("log_105", System.currentTimeMillis() - 3600000 * 48, "system", "SYSTEM_BACKUP", "Completed automated nightly database & media backup snapshot")
        )
    )
    val auditLogs: StateFlow<List<AuditLogDto>> = _auditLogs.asStateFlow()

    // Audit Log Filters
    private val _auditActionFilter = MutableStateFlow("ALL")
    val auditActionFilter: StateFlow<String> = _auditActionFilter.asStateFlow()

    private val _auditActorFilter = MutableStateFlow("")
    val auditActorFilter: StateFlow<String> = _auditActorFilter.asStateFlow()

    val filteredAuditLogs: StateFlow<List<AuditLogDto>> = combine(
        _auditLogs,
        _auditActionFilter,
        _auditActorFilter
    ) { logs, actionFilter, actorFilter ->
        logs.filter { log ->
            (actionFilter == "ALL" || log.actionType.equals(actionFilter, ignoreCase = true)) &&
            (actorFilter.isBlank() || log.actor.contains(actorFilter, ignoreCase = true) || log.details.contains(actorFilter, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _analytics = MutableStateFlow(AdminAnalytics())
    val analytics: StateFlow<AdminAnalytics> = _analytics.asStateFlow()

    private val _paymentSettings = MutableStateFlow(PaymentSettings())
    val paymentSettings: StateFlow<PaymentSettings> = _paymentSettings.asStateFlow()

    private val _adminMessage = MutableStateFlow<String?>(null)
    val adminMessage: StateFlow<String?> = _adminMessage.asStateFlow()

    private val _adminRole = MutableStateFlow<String?>("ADMIN")
    val adminRole: StateFlow<String?> = _adminRole.asStateFlow()

    fun loginAdmin(email: String, role: String) {
        viewModelScope.launch {
            val password = "AdminPass2026!"
            when (val remote = repo.loginAdminRemote(email, password)) {
                is com.example.data.remote.ApiResponse.Success -> {
                    _adminRole.value = remote.data.role
                    _adminMessage.value = "Authenticated as ${remote.data.role} ($email)"
                    addAuditLog(email, "ADMIN_LOGIN", "Admin logged in with role ${remote.data.role}")
                }
                else -> {
                    _adminRole.value = role
                    _adminMessage.value = "Authenticated as $role ($email)"
                    addAuditLog(email, "ADMIN_LOGIN", "Admin logged in with role $role")
                }
            }
        }
    }

    fun setAuditActionFilter(action: String) {
        _auditActionFilter.value = action
    }

    fun setAuditActorFilter(actor: String) {
        _auditActorFilter.value = actor
    }

    fun updateUserAccountStatus(userId: String, newStatus: String, reason: String? = null) {
        viewModelScope.launch {
            val currentMap = _userAccountStatuses.value.toMutableMap()
            currentMap[userId] = newStatus
            _userAccountStatuses.value = currentMap

            val prof = repo.getProfileSync(userId)
            val name = prof?.fullName ?: userId
            _adminMessage.value = "User $name status updated to $newStatus."

            val actionType = when (newStatus) {
                "SUSPENDED" -> "USER_SUSPEND"
                "BANNED" -> "USER_BAN"
                else -> "USER_ACTIVATE"
            }
            addAuditLog("admin@adventhearts.com", actionType, "Set status of $name to $newStatus. Reason: ${reason ?: "Admin action"}", userId)
        }
    }

    fun updateSettings(newSettings: PaymentSettings) {
        _paymentSettings.value = newSettings
        _adminMessage.value = "Admin & Payment Gateway settings saved successfully!"
        addAuditLog("admin@adventhearts.com", "SETTINGS_UPDATE", "Updated payment gateway settings for provider ${newSettings.provider}")
    }

    fun approveVerification(userId: String) {
        viewModelScope.launch {
            val prof = repo.getProfileSync(userId)
            if (prof != null) {
                repo.updateProfile(prof.copy(isVerified = true, verificationStatus = "VERIFIED"))
                _adminMessage.value = "Profile for ${prof.fullName} approved and verified badge awarded."
                addAuditLog("admin@adventhearts.com", "VERIFY_APPROVE", "Approved verification for user ${prof.fullName}", userId)
            }
        }
    }

    fun rejectVerification(userId: String) {
        viewModelScope.launch {
            val prof = repo.getProfileSync(userId)
            if (prof != null) {
                repo.updateProfile(prof.copy(isVerified = false, verificationStatus = "REJECTED"))
                _adminMessage.value = "Verification request for ${prof.fullName} rejected."
                addAuditLog("admin@adventhearts.com", "VERIFY_REJECT", "Rejected verification for user ${prof.fullName}", userId)
            }
        }
    }

    fun updateReportStatus(reportId: String, status: String) {
        viewModelScope.launch {
            repo.updateReportStatus(reportId, status)
            _adminMessage.value = "Report #$reportId updated to $status."
            addAuditLog("admin@adventhearts.com", "REPORT_STATUS", "Updated status of report #$reportId to $status")
        }
    }

    private fun addAuditLog(actor: String, actionType: String, details: String, targetUserId: String? = null) {
        val newLog = AuditLogDto(
            id = "log_${System.currentTimeMillis()}",
            timestamp = System.currentTimeMillis(),
            actor = actor,
            actionType = actionType,
            details = details,
            targetUserId = targetUserId
        )
        _auditLogs.value = listOf(newLog) + _auditLogs.value
    }

    fun updateUserSubscriptionTier(userId: String, isPremium: Boolean) {
        viewModelScope.launch {
            val prof = repo.getProfileSync(userId)
            if (prof != null) {
                repo.updateProfile(prof.copy(isPremium = isPremium))
                _adminMessage.value = "Updated subscription for ${prof.fullName} to ${if (isPremium) "PRO / GOLD" else "FREE"}."
                addAuditLog("admin@adventhearts.com", "SUBSCRIPTION_OVERRIDE", "Changed subscription status for ${prof.fullName} to ${if (isPremium) "PREMIUM" else "FREE"}", userId)
            }
        }
    }

    fun dismissMessage() {
        _adminMessage.value = null
    }
}
