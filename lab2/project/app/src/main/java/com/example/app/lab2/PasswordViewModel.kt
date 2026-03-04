package com.example.app.lab2

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PasswordVisibilityMode {
    ShowCharacters,
    ShowAsterisks
}

data class PasswordUiState(
    val password: String = "",
    val visibilityMode: PasswordVisibilityMode = PasswordVisibilityMode.ShowAsterisks,
    val submittedPassword: String? = null
)

class PasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordUiState())
    val uiState: StateFlow<PasswordUiState> = _uiState.asStateFlow()

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value.copy(password = value)
    }

    fun setVisibilityMode(mode: PasswordVisibilityMode) {
        _uiState.value = _uiState.value.copy(visibilityMode = mode)
    }

    fun submitPassword(): Boolean {
        val current = _uiState.value.password
        return if (current.isNotBlank()) {
            _uiState.value = _uiState.value.copy(submittedPassword = current)
            true
        } else {
            false
        }
    }

    fun cancelAndClear() {
        _uiState.value = PasswordUiState()
    }
}
