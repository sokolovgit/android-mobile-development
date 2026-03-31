package com.example.project

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.data.PasswordEntry
import com.example.project.data.PasswordEntryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StorageViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: PasswordEntryDao = ProjectApplication.database(application).passwordEntryDao()

    val entries: StateFlow<List<PasswordEntry>> = dao.getAllOrdered()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun delete(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(entry)
        }
    }

    fun update(entry: PasswordEntry) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.update(entry)
        }
    }
}
