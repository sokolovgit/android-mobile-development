package com.example.project.lab2

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.ProjectApplication
import com.example.project.R
import com.example.project.data.PasswordEntry
import com.example.project.data.PasswordEntryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class PasswordVisibilityMode {
    ShowCharacters,
    ShowAsterisks
}

data class PasswordUiState(
    val password: String = "",
    val visibilityMode: PasswordVisibilityMode = PasswordVisibilityMode.ShowAsterisks,
    val submittedPassword: String? = null
)

class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: PasswordEntryDao = ProjectApplication.database(application).passwordEntryDao()

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

    fun persistSubmissionAfterOk(onPersisted: () -> Unit) {
        val state = _uiState.value
        val pwd = state.submittedPassword ?: return
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dao.insert(
                        PasswordEntry(
                            password = pwd,
                            visibilityMode = state.visibilityMode.name,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }
                Toast.makeText(
                    getApplication(),
                    getApplication<Application>().getString(R.string.lab3_save_success),
                    Toast.LENGTH_SHORT
                ).show()
                onPersisted()
            } catch (_: Exception) {
                Toast.makeText(
                    getApplication(),
                    getApplication<Application>().getString(R.string.lab3_save_failure),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
