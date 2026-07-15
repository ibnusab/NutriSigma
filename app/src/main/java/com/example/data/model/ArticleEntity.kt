package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Diet", "Olahraga", "Tidur", "Nutrisi", "Tips"
    val snippet: String,
    val content: String,
    val readTimeMinutes: Int = 5,
    val imageUrl: String = "" // Local asset or placeholder name
)
