package com.example.project.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordEntryDao {

    @Insert
    suspend fun insert(entry: PasswordEntry)

    @Query("SELECT * FROM password_entries ORDER BY createdAt DESC")
    fun getAllOrdered(): Flow<List<PasswordEntry>>

    @Update
    suspend fun update(entry: PasswordEntry)

    @Delete
    suspend fun delete(entry: PasswordEntry)
}
