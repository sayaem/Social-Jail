package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_lock_sessions")
data class DeviceLockSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED"
    val goalText: String? = null,
    val isDeviceOwnerMode: Boolean = false,
    val startElapsedRealtime: Long = 0L,
    val expectedDurationMillis: Long = 0L,
    val completedAt: Long? = null
)
