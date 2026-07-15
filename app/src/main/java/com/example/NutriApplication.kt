package com.example

import android.app.Application
import com.example.data.database.AppDatabase
import com.example.data.repository.NutriRepository

class NutriApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        NutriRepository(
            database.userDao(),
            database.historyDao(),
            database.reminderDao(),
            database.articleDao(),
        )
    }

}
