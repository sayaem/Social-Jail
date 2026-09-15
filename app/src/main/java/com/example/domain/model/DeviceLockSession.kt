package com.example.domain.model

import android.os.SystemClock
import com.example.data.local.entity.DeviceLockSessionEntity

data class DeviceLockSession(
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val status: DeviceLockStatus = DeviceLockStatus.ACTIVE,
    val goalText: String? = null,
    val isDeviceOwnerMode: Boolean = false,
    val startElapsedRealtime: Long = 0L,
    val expectedDurationMillis: Long = 0L,
    val completedAt: Long? = null,
    val goalStatus: GoalStatus? = null,
    val reviewNote: String? = null
) {
    fun remainingMillis(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        if (status == DeviceLockStatus.COMPLETED) return 0L

        val clockRemaining = endTime - currentTimeMillis
        val currentElapsedRealtime = SystemClock.elapsedRealtime()

        // Anti-tamper verification using monotonic clock
        if (startElapsedRealtime > 0L && expectedDurationMillis > 0L && currentElapsedRealtime >= startElapsedRealtime) {
            val actualElapsed = currentElapsedRealtime - startElapsedRealtime
            val monotonicRemaining = expectedDurationMillis - actualElapsed
            if (clockRemaining <= 0 && monotonicRemaining > 5000L) {
                return monotonicRemaining.coerceAtLeast(0L)
            }
        }
        return clockRemaining.coerceAtLeast(0L)
    }

    fun isCurrentlyActive(now: Long = System.currentTimeMillis()): Boolean {
        return status == DeviceLockStatus.ACTIVE && remainingMillis(now) > 0L
    }

    fun toEntity(): DeviceLockSessionEntity {
        return DeviceLockSessionEntity(
            id = id,
            createdAt = createdAt,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            status = status.name,
            goalText = goalText,
            isDeviceOwnerMode = isDeviceOwnerMode,
            startElapsedRealtime = startElapsedRealtime,
            expectedDurationMillis = expectedDurationMillis,
            completedAt = completedAt,
            goalStatus = goalStatus?.name,
            reviewNote = reviewNote
        )
    }

    companion object {
        fun fromEntity(entity: DeviceLockSessionEntity): DeviceLockSession {
            val parsedStatus = try {
                DeviceLockStatus.valueOf(entity.status)
            } catch (e: Exception) {
                DeviceLockStatus.COMPLETED
            }
            val parsedGoalStatus = entity.goalStatus?.let {
                try { GoalStatus.valueOf(it) } catch (e: Exception) { null }
            }
            return DeviceLockSession(
                id = entity.id,
                createdAt = entity.createdAt,
                startTime = entity.startTime,
                endTime = entity.endTime,
                durationMinutes = entity.durationMinutes,
                status = parsedStatus,
                goalText = entity.goalText,
                isDeviceOwnerMode = entity.isDeviceOwnerMode,
                startElapsedRealtime = entity.startElapsedRealtime,
                expectedDurationMillis = entity.expectedDurationMillis,
                completedAt = entity.completedAt,
                goalStatus = parsedGoalStatus,
                reviewNote = entity.reviewNote
            )
        }
    }
}

enum class DeviceLockStatus {
    ACTIVE,
    COMPLETED
}
