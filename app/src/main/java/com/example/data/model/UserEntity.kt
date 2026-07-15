package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val age: Int = 23,
    val gender: String = "Pria", // "Pria" or "Wanita"
    val height: Double = 170.0,  // in cm
    val weight: Double = 65.0,   // in kg
    val activityLevel: Double = 1.375, // multiplier: 1.2, 1.375, 1.55, 1.725, 1.9
    val goalType: String = "Menjaga Berat Badan", // "Menurunkan Berat Badan", "Menjaga Berat Badan", "Menambah Berat Badan", "Menambah Massa Otot"
    val targetWeight: Double = 65.0, // in kg
    val isDarkMode: Boolean = false,
    val avatarUri: String? = null,
    val waterIntakeMl: Int = 0,
    val loggedCalories: Int = 0,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
