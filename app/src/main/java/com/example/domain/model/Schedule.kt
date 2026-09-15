package com.example.domain.model

import com.example.data.local.entity.ScheduleEntity

data class Schedule(
    val id: Long = 0,
    val title: String,
    val profileId: Long? = null,
    val profileName: String? = null,
    val daysOfWeek: Set<Int>, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val startHour: Int,
    val startMinute: Int,
    val durationMinutes: Int,
    val blockedPackageNames: List<String>,
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formattedTimeRange(): String {
        val startFormatted = String.format("%02d:%02d", startHour, startMinute)
        val endTotalMinutes = (startHour * 60 + startMinute + durationMinutes) % (24 * 60)
        val endHour = endTotalMinutes / 60
        val endMinute = endTotalMinutes % 60
        val endFormatted = String.format("%02d:%02d", endHour, endMinute)
        return "$startFormatted – $endFormatted"
    }

    fun formattedDays(): String {
        if (daysOfWeek.size == 7) return "Every day"
        if (daysOfWeek == setOf(1, 2, 3, 4, 5)) return "Mon–Fri"
        if (daysOfWeek == setOf(7, 1, 2, 3, 4)) return "Sun–Thu"
        if (daysOfWeek == setOf(6, 7)) return "Weekends"
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return daysOfWeek.sorted().mapNotNull { if (it in 1..7) dayNames[it - 1] else null }.joinToString(", ")
    }

    fun toEntity(): ScheduleEntity {
        return ScheduleEntity(
            id = id,
            title = title,
            profileId = profileId,
            profileName = profileName,
            daysOfWeek = daysOfWeek.sorted().joinToString(","),
            startHour = startHour,
            startMinute = startMinute,
            durationMinutes = durationMinutes,
            blockedPackageNames = blockedPackageNames.joinToString(","),
            isEnabled = isEnabled,
            createdAt = createdAt
        )
    }

    companion object {
        fun fromEntity(entity: ScheduleEntity): Schedule {
            val days = if (entity.daysOfWeek.isBlank()) emptySet() else entity.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            val pkgs = if (entity.blockedPackageNames.isBlank()) emptyList() else entity.blockedPackageNames.split(",")
            return Schedule(
                id = entity.id,
                title = entity.title,
                profileId = entity.profileId,
                profileName = entity.profileName,
                daysOfWeek = days,
                startHour = entity.startHour,
                startMinute = entity.startMinute,
                durationMinutes = entity.durationMinutes,
                blockedPackageNames = pkgs,
                isEnabled = entity.isEnabled,
                createdAt = entity.createdAt
            )
        }
    }
}
