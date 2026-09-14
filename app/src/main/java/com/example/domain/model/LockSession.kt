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
    val completedAt: Long? = null
) {
    fun remainingMillis(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        if (status == SessionStatus.COMPLETED) return 0L

        // Anti-tamper verification: check if system clock was wound forward
        val clockRemaining = endTime - currentTimeMillis
        if (startElapsedRealtime > 0L && expectedDurationMillis > 0L) {
            val actualElapsed = SystemClock.elapsedRealtime() - startElapsedRealtime
            val monotonicRemaining = expectedDurationMillis - actualElapsed
            // If clock was moved forward artificially, enforce monotonic elapsed requirement
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
            completedAt = completedAt
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
                completedAt = entity.completedAt
            )
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
