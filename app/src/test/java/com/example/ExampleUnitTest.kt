package com.example

import com.example.domain.model.DeviceLockSession
import com.example.domain.model.DeviceLockStats
import com.example.domain.model.DeviceLockStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun deviceLockStats_formattedHours_isCorrect() {
        val stats1 = DeviceLockStats(
            totalProtectedMillis = 7200000L // 2 hours
        )
        assertEquals("2h 0m", stats1.totalProtectedHoursFormatted)

        val stats2 = DeviceLockStats(
            totalProtectedMillis = 2700000L // 45 mins
        )
        assertEquals("45m", stats2.totalProtectedHoursFormatted)
    }

    @Test
    fun deviceLockSession_entityConversion_preservesValues() {
        val session = DeviceLockSession(
            id = 42L,
            startTime = 1000L,
            endTime = 5000L,
            durationMinutes = 60,
            status = DeviceLockStatus.ACTIVE,
            goalText = "Deep Work Focus",
            isDeviceOwnerMode = true
        )
        val entity = session.toEntity()
        val restored = DeviceLockSession.fromEntity(entity)

        assertEquals(session.id, restored.id)
        assertEquals(session.startTime, restored.startTime)
        assertEquals(session.endTime, restored.endTime)
        assertEquals(session.durationMinutes, restored.durationMinutes)
        assertEquals(session.status, restored.status)
        assertEquals(session.goalText, restored.goalText)
        assertEquals(session.isDeviceOwnerMode, restored.isDeviceOwnerMode)
    }
}
