package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val category: String = "Persönlich", // default German category
    val colorHex: String = "#FFFFFF",    // default white/transparent card
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
