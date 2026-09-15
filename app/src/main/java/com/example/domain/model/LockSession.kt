package com.example.domain.model

import android.os.SystemClock
import com.example.data.local.entity.LockSessionEntity

data class LockSession(
    val id: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val startTime: Long,
    val endTime: Long,
    val status: SessionStatus,
    val mode: LockMode = LockMode.HARDCORE,
    val blockedPackageNames: List<String>,
    val blockedAppNames: List<String>,
    val startElapsedRealtime: Long = 0L,
    val expectedDurationMillis: Long = 0L,
    val completedAt: Long? = null,
    val goalText: String? = null,
    val profileName: String? = null,
    val escalationEnabled: Boolean = false,
    val escalationAttemptTrigger: Int = 3,
    val escalationAction: String = "DEVICE_LOCK",
    val escalationTriggered: Boolean = false,
    val goalStatus: GoalStatus? = null,
    val reviewNote: String? = null
) {
    val isDeviceLock: Boolean
        get() = profileName == "DEVICE_LOCK" || (blockedPackageNames.isEmpty() && blockedAppNames.isEmpty())

    fun remainingMillis(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        if (status == SessionStatus.COMPLETED) return 0L

        val clockRemaining = endTime - currentTimeMillis
        val currentElapsedRealtime = SystemClock.elapsedRealtime()

        // Anti-tamper verification (valid when device has not rebooted since session started)
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
        return status == SessionStatus.ACTIVE && remainingMillis(now) > 0L
    }

    fun toEntity(): LockSessionEntity {
        return LockSessionEntity(
            id = id,
            createdAt = createdAt,
            startTime = startTime,
            endTime = endTime,
            status = status.name,
            mode = mode.name,
            blockedPackageNames = blockedPackageNames.joinToString(","),
            blockedAppNames = blockedAppNames.joinToString(","),
            startElapsedRealtime = startElapsedRealtime,
            expectedDurationMillis = expectedDurationMillis,
            completedAt = completedAt,
            goalText = goalText,
            profileName = profileName,
            escalationEnabled = escalationEnabled,
            escalationAttemptTrigger = escalationAttemptTrigger,
            escalationAction = escalationAction,
            escalationTriggered = escalationTriggered,
            goalStatus = goalStatus?.name,
            reviewNote = reviewNote
        )
    }

    companion object {
        fun fromEntity(entity: LockSessionEntity): LockSession {
            val pkgs = if (entity.blockedPackageNames.isBlank()) emptyList() else entity.blockedPackageNames.split(",")
            val apps = if (entity.blockedAppNames.isBlank()) emptyList() else entity.blockedAppNames.split(",")
            val parsedStatus = try {
                SessionStatus.valueOf(entity.status)
            } catch (e: Exception) {
                SessionStatus.COMPLETED
            }
            val parsedMode = try {
                LockMode.valueOf(entity.mode)
            } catch (e: Exception) {
                LockMode.HARDCORE
            }
            val parsedGoalStatus = entity.goalStatus?.let {
                try { GoalStatus.valueOf(it) } catch (e: Exception) { null }
            }
            return LockSession(
                id = entity.id,
                createdAt = entity.createdAt,
                startTime = entity.startTime,
                endTime = entity.endTime,
                status = parsedStatus,
                mode = parsedMode,
                blockedPackageNames = pkgs,
                blockedAppNames = apps,
                startElapsedRealtime = entity.startElapsedRealtime,
                expectedDurationMillis = entity.expectedDurationMillis,
                completedAt = entity.completedAt,
                goalText = entity.goalText,
                profileName = entity.profileName,
                escalationEnabled = entity.escalationEnabled,
                escalationAttemptTrigger = entity.escalationAttemptTrigger,
                escalationAction = entity.escalationAction,
                escalationTriggered = entity.escalationTriggered,
                goalStatus = parsedGoalStatus,
                reviewNote = entity.reviewNote
            )
        }

        /**
         * Formats remaining time cleanly:
         * - Over 24 hours: 1d 04h 23m
         * - 1 hour to 24 hours: HH:MM:SS (e.g. 02:43:18)
         * - Under 1 hour: MM:SS (e.g. 42:18)
         */
        fun formatCountdown(millis: Long): String {
            if (millis <= 0L) return "00:00"
            val totalSeconds = millis / 1000
            val days = totalSeconds / 86400
            val hours = (totalSeconds % 86400) / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return when {
                days > 0 -> {
                    String.format("%dd %02dh %02dm", days, hours, minutes)
                }
                hours > 0 -> {
                    String.format("%02d:%02d:%02d", hours, minutes, seconds)
                }
                else -> {
                    String.format("%02d:%02d", minutes, seconds)
                }
            }
        }
    }
}

enum class SessionStatus {
    SCHEDULED,
    ACTIVE,
    COMPLETED
}

enum class LockMode {
    HARDCORE,
    NORMAL
}

enum class GoalStatus {
    COMPLETED,
    PARTIAL,
    NOT_COMPLETED
}

enum class CommitmentLevel {
    STANDARD,
    HARDCORE,
    DEVICE_LOCK
}
