package com.example.project.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "password_entries")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val password: String,
    val visibilityMode: String,
    val createdAt: Long
)
