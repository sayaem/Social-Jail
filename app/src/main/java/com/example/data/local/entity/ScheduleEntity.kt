package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_locks")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val profileId: Long? = null,
    val profileName: String? = null,
    val daysOfWeek: String, // Comma separated integers: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val startHour: Int, // 0-23
    val startMinute: Int, // 0-59
    val durationMinutes: Int,
    val blockedPackageNames: String,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
