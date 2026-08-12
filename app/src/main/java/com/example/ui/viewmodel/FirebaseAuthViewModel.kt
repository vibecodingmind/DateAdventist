package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser, val isNewUser: Boolean = false) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class FirebaseAuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val credentialManager by lazy { CredentialManager.create(application) }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _uiState = MutableStateFlow<AuthUiState>(
        if (auth.currentUser != null) AuthUiState.Success(auth.currentUser!!) else AuthUiState.Idle
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null && _uiState.value !is AuthUiState.Success) {
            _uiState.value = AuthUiState.Success(user)
        } else if (user == null && _uiState.value !is AuthUiState.Idle) {
            _uiState.value = AuthUiState.Idle
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    /**
     * Sign in using Firebase Email and Password.
     */
    fun signInWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result: AuthResult = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user, isNewUser = false)
                } else {
                    _uiState.value = AuthUiState.Error("Sign in failed: User account not found.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Authentication failed.")
            }
        }
    }

    /**
     * Register a new user using Firebase Email and Password.
     */
    fun signUpWithEmail(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Email and password cannot be empty.")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result: AuthResult = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val user = result.user
                if (user != null) {
                    _uiState.value = AuthUiState.Success(user, isNewUser = true)
                } else {
                    _uiState.value = AuthUiState.Error("Registration failed. Please try again.")
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Registration failed.")
            }
        }
    }

    /**
     * Initiate Google Sign-In flow using Android Credential Manager.
     */
    fun signInWithGoogle(context: Context, serverClientId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    firebaseAuthWithGoogle(idToken)
                } else {
                    _uiState.value = AuthUiState.Error("Unsupported credential type received.")
                }
            } catch (e: GetCredentialException) {
                _uiState.value = AuthUiState.Error("Google Sign-In canceled or failed: ${e.message}")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Google Sign-In failed.")
            }
        }
    }

    /**
     * Authenticate with Firebase using Google ID Token.
     */
    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            if (user != null) {
                _uiState.value = AuthUiState.Success(user, isNewUser = isNewUser)
            } else {
                _uiState.value = AuthUiState.Error("Firebase authentication with Google failed.")
            }
        } catch (e: Exception) {
            _uiState.value = AuthUiState.Error(e.localizedMessage ?: "Firebase Google auth failed.")
        }
    }

    /**
     * Send a password reset email to the user.
     */
    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onComplete(false, "Please enter your email address.")
            return
        }
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email.trim()).await()
                onComplete(true, "Password reset email sent. Please check your inbox.")
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to send reset email.")
            }
        }
    }

    /**
     * Sign out current user.
     */
    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _uiState.value = AuthUiState.Idle
    }

    /**
     * Clear error state back to Idle.
     */
    fun resetError() {
        if (_uiState.value is AuthUiState.Error) {
            _uiState.value = AuthUiState.Idle
        }
    }
}
