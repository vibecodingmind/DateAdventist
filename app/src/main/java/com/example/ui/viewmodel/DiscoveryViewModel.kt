package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ProfileEntity
import com.example.data.repository.AdventHeartsRepository
import com.example.service.LocationMatchingService
import com.example.service.UserLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DiscoveryFilterState(
    val minAge: Int = 18,
    val maxAge: Int = 60,
    val maxDistanceKm: Int = 500,
    val genderPreference: String = "All", // All, Female, Male
    val adventistAffiliation: String = "All",
    val churchInvolvement: String = "All",
    val dietPreference: String = "All",
    val relationshipIntention: String = "All",
    val verifiedOnly: Boolean = false,
    val searchQuery: String = ""
)

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)
    val locationMatchingService = LocationMatchingService(application)

    private val _userLocation = MutableStateFlow<UserLocation?>(null)
    val userLocation: StateFlow<UserLocation?> = _userLocation.asStateFlow()

    fun refreshUserLocation() {
        viewModelScope.launch {
            val loc = locationMatchingService.fetchCurrentLocation()
            if (loc != null) {
                _userLocation.value = loc
            }
        }
    }

    private val _currentUserId = MutableStateFlow("usr_me")
    fun setCurrentUserId(id: String) { _currentUserId.value = id }

    private val _selectedTab = MutableStateFlow("Recommended") // Recommended, Nearby, New, Most Compatible, Verified
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _filterState = MutableStateFlow(DiscoveryFilterState())
    val filterState: StateFlow<DiscoveryFilterState> = _filterState.asStateFlow()

    private val _matchAlertProfile = MutableStateFlow<ProfileEntity?>(null)
    val matchAlertProfile: StateFlow<ProfileEntity?> = _matchAlertProfile.asStateFlow()

    val discoveryProfiles: StateFlow<List<ProfileEntity>> = combine(
        repo.getAllOtherProfiles("usr_me"),
        _filterState,
        _selectedTab
    ) { allProfiles, filter, tab ->
        val currentMyProfile = repo.getProfileSync("usr_me")

        allProfiles.filter { p ->
            // Age
            if (p.age !in filter.minAge..filter.maxAge) return@filter false
            // Distance
            if (p.distanceKm > filter.maxDistanceKm) return@filter false
            // Gender
            if (filter.genderPreference != "All" && p.gender != filter.genderPreference) return@filter false
            // Adventist status
            if (filter.adventistAffiliation != "All" && p.adventistAffiliation != filter.adventistAffiliation) return@filter false
            // Church involvement
            if (filter.churchInvolvement != "All" && p.churchInvolvement != filter.churchInvolvement) return@filter false
            // Diet
            if (filter.dietPreference != "All" && p.diet != filter.dietPreference) return@filter false
            // Intention
            if (filter.relationshipIntention != "All" && p.relationshipIntention != filter.relationshipIntention) return@filter false
            // Verified
            if (filter.verifiedOnly && !p.isVerified) return@filter false
            // Search query
            if (filter.searchQuery.isNotBlank()) {
                val q = filter.searchQuery.lowercase()
                val matchesName = p.fullName.lowercase().contains(q)
                val matchesCity = p.city.lowercase().contains(q)
                val matchesBio = p.bio.lowercase().contains(q)
                val matchesChurch = p.localChurch.lowercase().contains(q)
                if (!matchesName && !matchesCity && !matchesBio && !matchesChurch) return@filter false
            }
            true
        }.sortedByDescending { p ->
            when (tab) {
                "Nearby" -> -p.distanceKm
                "Most Compatible" -> repo.calculateCompatibility(currentMyProfile, p)
                "Verified" -> if (p.isVerified) 1 else 0
                else -> repo.calculateCompatibility(currentMyProfile, p)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setTab(tab: String) {
        _selectedTab.value = tab
    }

    fun updateFilter(update: (DiscoveryFilterState) -> DiscoveryFilterState) {
        _filterState.value = update(_filterState.value)
    }

    fun onLike(targetProfile: ProfileEntity, isSuperLike: Boolean = false) {
        viewModelScope.launch {
            val isMatch = repo.sendLike("usr_me", targetProfile.userId, isSuperLike)
            if (isMatch) {
                _matchAlertProfile.value = targetProfile
            }
        }
    }

    fun onPass(targetProfile: ProfileEntity) {
        viewModelScope.launch {
            repo.sendPass("usr_me", targetProfile.userId)
        }
    }

    fun dismissMatchAlert() {
        _matchAlertProfile.value = null
    }

    fun getCompatibilityScore(targetProfile: ProfileEntity): Int {
        val myProfile = ProfileEntity(
            userId = "usr_me", fullName = "Joshua Miller", age = 28, gender = "Male",
            country = "United States", city = "Berrien Springs", occupation = "Engineer",
            education = "BS", bio = "", relationshipIntention = "Marriage", primaryPhoto = "",
            photoUrls = emptyList(), adventistAffiliation = "Seventh-day Adventist Member",
            yearsAsAdventist = 28, localChurch = "Pioneer Memorial Church", isBaptized = true,
            faithImportance = "Central to everything I do", churchInvolvement = "Very active",
            sabbathObservance = listOf("Church Service", "Nature Walks"),
            ministryInterests = listOf("Youth", "Evangelism"), personalBibleStudy = "Daily",
            favoriteVerse = "Jeremiah 29:11", diet = "Vegetarian", alcohol = "None / Abstain",
            smoking = "Never", wantsChildren = "Yes, definitely", hasChildren = false,
            interests = listOf("Sabbath Nature Walks", "Youth Ministry")
        )
        return repo.calculateCompatibility(myProfile, targetProfile)
    }
}
