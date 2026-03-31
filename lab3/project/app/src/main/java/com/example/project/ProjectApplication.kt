package com.example.project

import android.app.Application
import androidx.room.Room
import com.example.project.data.AppDatabase

class ProjectApplication : Application() {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "lab3.db"
        ).build()
    }

    companion object {
        fun database(app: Application): AppDatabase = (app as ProjectApplication).database
    }
}
