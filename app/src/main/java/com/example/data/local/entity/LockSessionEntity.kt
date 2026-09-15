package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lock_sessions")
data class LockSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long,
    val endTime: Long,
    val status: String, // "SCHEDULED", "ACTIVE", "COMPLETED"
    val mode: String = "HARDCORE", // "HARDCORE", "NORMAL"
    val blockedPackageNames: String, // Comma separated package names
    val blockedAppNames: String, // Comma separated app display names
    val startElapsedRealtime: Long = 0L,
    val expectedDurationMillis: Long = 0L,
    val completedAt: Long? = null,
    val goalText: String? = null,
    val profileName: String? = null
)
