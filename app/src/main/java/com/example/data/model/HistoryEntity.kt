package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "histories")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val weight: Double,
    val height: Double,
    val age: Int,
    val gender: String,
    val bmi: Double,
    val bmr: Double,
    val tdee: Double,
    val targetCalorie: Double,
    val targetProtein: Double,
    val targetCarb: Double,
    val targetFat: Double,
    val targetWater: Double,
    val goalType: String,
    val category: String
)
