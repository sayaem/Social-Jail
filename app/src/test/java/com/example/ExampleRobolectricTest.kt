package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.LockMode
import com.example.domain.model.LockSession
import com.example.domain.model.SessionStatus
import com.example.util.PackageUtils
import com.example.util.TimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Social Jail", appName)
  }

  @Test
  fun `essential apps cannot be blocked`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertTrue(PackageUtils.isEssentialApp("com.google.android.dialer", context.packageName))
    assertTrue(PackageUtils.isEssentialApp("com.android.calculator2", context.packageName))
    assertTrue(PackageUtils.isEssentialApp("com.google.android.calendar", context.packageName))
    assertTrue(PackageUtils.isEssentialApp("com.google.android.apps.messaging", context.packageName))
    assertTrue(PackageUtils.isEssentialApp(context.packageName, context.packageName))
    assertFalse(PackageUtils.isEssentialApp("com.instagram.android", context.packageName))
    assertFalse(PackageUtils.isEssentialApp("com.google.android.youtube", context.packageName))
  }

  @Test
  fun `lock session remaining time formatting`() {
    val oneHour = 60 * 60 * 1000L
    assertEquals("01:00:00", TimeUtils.formatRemaining(oneHour))
    assertEquals("1h 0m", TimeUtils.formatRemainingShort(oneHour))

    val zero = 0L
    assertEquals("00:00:00", TimeUtils.formatRemaining(zero))
    assertEquals("0m", TimeUtils.formatRemainingShort(zero))
  }

  @Test
  fun `active lock session state detection`() {
    val now = System.currentTimeMillis()
    val session = LockSession(
      id = 1,
      startTime = now - 1000L,
      endTime = now + 60000L,
      status = SessionStatus.ACTIVE,
      mode = LockMode.HARDCORE,
      blockedPackageNames = listOf("com.instagram.android"),
      blockedAppNames = listOf("Instagram")
    )
    assertTrue(session.isCurrentlyActive(now))
    assertTrue(session.remainingMillis(now) > 0)
  }

  @Test
  fun `profile presets are populated with valid essential exclusions`() {
    val presets = com.example.domain.model.Profile.getDefaultPresets()
    assertTrue(presets.isNotEmpty())
    val study = presets.first { it.name == "Study" }
    assertTrue(study.packageNames.contains("com.instagram.android"))
    assertTrue(study.isPredefined)
  }

  @Test
  fun `schedule time range format correctly`() {
    val schedule = com.example.domain.model.Schedule(
      id = 1,
      title = "Night Lock",
      daysOfWeek = setOf(1, 2, 3, 4, 5),
      startHour = 22,
      startMinute = 0,
      durationMinutes = 480,
      blockedPackageNames = listOf("com.instagram.android")
    )
    assertEquals("22:00 – 06:00", schedule.formattedTimeRange())
    assertEquals("Mon–Fri", schedule.formattedDays())
  }
}
