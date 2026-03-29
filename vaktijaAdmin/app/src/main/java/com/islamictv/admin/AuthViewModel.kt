package com.islamictv.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User is already logged in — load their džemat
            viewModelScope.launch {
                val result = DzamatManager.loadDzamijaForCurrentUser()
                _authState.value = result.fold(
                    onSuccess = { AuthState.Authenticated(currentUser) },
                    onFailure = { AuthState.Error("Prijava uspješna, ali džemat nije pronađen: ${it.message}") }
                )
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                    ?: run {
                        _authState.value = AuthState.Error("Greška pri prijavi")
                        return@launch
                    }

                // After successful login, load which džemat this admin manages
                val dzamatResult = DzamatManager.loadDzamijaForCurrentUser()
                _authState.value = dzamatResult.fold(
                    onSuccess = { AuthState.Authenticated(user) },
                    onFailure = {
                        auth.signOut()
                        AuthState.Error("Prijava uspješna, ali vaš račun nema dodijeljen džemat. Kontaktirajte podršku.")
                    }
                )
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    when {
                        e.message?.contains("password") == true -> "Pogrešna lozinka"
                        e.message?.contains("user") == true -> "Korisnik ne postoji"
                        e.message?.contains("network") == true -> "Greška u vezi"
                        else -> "Greška: ${e.message}"
                    }
                )
            }
        }
    }

    fun signOut() {
        auth.signOut()
        DzamatManager.clear() // Clear cached džemat data
        _authState.value = AuthState.Unauthenticated
    }
}