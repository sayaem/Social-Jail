package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smart_presets")
data class SmartPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String,
    val description: String,
    val durationMinutes: Int,
    val commitmentLevel: String = "HARDCORE", // "STANDARD", "HARDCORE", "DEVICE_LOCK"
    val defaultGoal: String? = null,
    val escalationEnabled: Boolean = false,
    val escalationAttemptTrigger: Int = 3,
    val blockedPackageNames: String = "",
    val profileName: String? = null,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
