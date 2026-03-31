package com.example.project.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PasswordEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun passwordEntryDao(): PasswordEntryDao
}
