package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FirestoreChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

class FirestoreChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _messages = MutableStateFlow<List<FirestoreChatMessage>>(emptyList())
    val messages: StateFlow<List<FirestoreChatMessage>> = _messages.asStateFlow()

    private val _typedMessage = MutableStateFlow("")
    val typedMessage: StateFlow<String> = _typedMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null
    private var currentChatId: String? = null

    fun onTypedMessageChanged(text: String) {
        _typedMessage.value = text
    }

    /**
     * Connect real-time Firestore listener for messages in a given chat room.
     */
    fun startListeningForMessages(chatId: String) {
        if (currentChatId == chatId && listenerRegistration != null) return

        listenerRegistration?.remove()
        currentChatId = chatId
        _isLoading.value = true
        _errorMessage.value = null

        try {
            listenerRegistration = db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    _isLoading.value = false
                    if (error != null) {
                        _errorMessage.value = error.localizedMessage ?: "Failed to listen for messages."
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val messageList = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val senderId = doc.getString("senderId") ?: ""
                            val senderName = doc.getString("senderName") ?: ""
                            val text = doc.getString("text") ?: ""
                            val ts = doc.get("timestamp")
                            val timestampLong = when (ts) {
                                is Timestamp -> ts.toDate().time
                                is Long -> ts
                                is Number -> ts.toLong()
                                else -> System.currentTimeMillis()
                            }
                            FirestoreChatMessage(
                                id = id,
                                chatId = chatId,
                                senderId = senderId,
                                senderName = senderName,
                                text = text,
                                timestamp = timestampLong
                            )
                        }
                        _messages.value = messageList
                    }
                }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = e.localizedMessage ?: "Firestore error."
        }
    }

    /**
     * Send a new message to Firestore.
     */
    fun sendMessage(senderId: String, senderName: String) {
        val textToSend = _typedMessage.value.trim()
        val chatId = currentChatId ?: return
        if (textToSend.isEmpty()) return

        _typedMessage.value = ""

        val messageData = hashMapOf(
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to textToSend,
            "timestamp" to Timestamp.now()
        )

        viewModelScope.launch {
            try {
                db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(messageData)
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Failed to send message."
            }
        }
    }

    fun stopListening() {
        listenerRegistration?.remove()
        listenerRegistration = null
        currentChatId = null
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
