package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LikeEntity
import com.example.data.local.MatchEntity
import com.example.data.local.MessageEntity
import com.example.data.local.ProfileEntity
import com.example.data.remote.AuthTokenManager
import com.example.data.repository.AdventHeartsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MatchWithProfile(
    val match: MatchEntity,
    val otherProfile: ProfileEntity?
)

data class LikeWithProfile(
    val like: LikeEntity,
    val profile: ProfileEntity?
)

class MatchesViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AdventHeartsRepository.getInstance(application)
    private fun uid(): String = AuthTokenManager.currentUserId ?: "usr_me"
    fun currentUserId(): String = uid()

    private val _currentMatchId = MutableStateFlow<String?>(null)
    val currentMatchId: StateFlow<String?> = _currentMatchId.asStateFlow()

    private val _typedMessage = MutableStateFlow("")
    val typedMessage: StateFlow<String> = _typedMessage.asStateFlow()

    val matchesWithProfiles: StateFlow<List<MatchWithProfile>> = repo.getMatches(uid())
        .map { matches ->
            matches.map { match ->
                val otherId = if (match.user1Id == uid()) match.user2Id else match.user1Id
                val otherProf = repo.getProfileSync(otherId)
                MatchWithProfile(match, otherProf)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val likesYouProfiles: StateFlow<List<LikeWithProfile>> = repo.getLikesReceived(uid())
        .map { likes ->
            likes.map { like ->
                val prof = repo.getProfileSync(like.fromUserId)
                LikeWithProfile(like, prof)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val currentChatMessages: StateFlow<List<MessageEntity>> = _currentMatchId.flatMapLatest { matchId ->
        if (matchId == null) flowOf(emptyList())
        else repo.getMessages(matchId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val currentChatMatchWithProfile: StateFlow<MatchWithProfile?> = _currentMatchId.flatMapLatest { matchId ->
        if (matchId == null) flowOf(null)
        else {
            val match = repo.getMatchById(matchId)
            if (match != null) {
                val otherId = if (match.user1Id == uid()) match.user2Id else match.user1Id
                val prof = repo.getProfileSync(otherId)
                flowOf(MatchWithProfile(match, prof))
            } else flowOf(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectMatch(matchId: String) {
        _currentMatchId.value = matchId
        viewModelScope.launch {
            repo.markMessagesAsRead(matchId, uid())
            repo.syncMessages(matchId)
        }
    }

    fun updateTypedMessage(text: String) {
        _typedMessage.value = text
    }

    fun sendMessage() {
        val text = _typedMessage.value.trim()
        val matchId = _currentMatchId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            val match = repo.getMatchById(matchId) ?: return@launch
            val receiverId = if (match.user1Id == uid()) match.user2Id else match.user1Id
            repo.sendMessage(matchId, uid(), receiverId, text)
            _typedMessage.value = ""
        }
    }

    fun sendPromptAsMessage(promptText: String) {
        _typedMessage.value = promptText
        sendMessage()
    }

    fun unmatch(matchId: String) {
        viewModelScope.launch {
            repo.unmatch(matchId)
            if (_currentMatchId.value == matchId) {
                _currentMatchId.value = null
            }
        }
    }
}
