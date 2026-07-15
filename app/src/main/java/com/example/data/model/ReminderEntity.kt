package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val title: String,
    val timeStr: String, // format "HH:mm"
    val isEnabled: Boolean = true,
    val type: String // "Water", "Breakfast", "Lunch", "Dinner", "Workout"
)
