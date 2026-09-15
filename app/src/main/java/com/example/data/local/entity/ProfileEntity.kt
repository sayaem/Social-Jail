package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconEmoji: String = "🧠",
    val packageNames: String, // Comma separated package names
    val defaultDurationMinutes: Int = 120,
    val isPredefined: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
