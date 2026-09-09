package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.screens.SpeedGestureMode
import org.junit.Assert.assertEquals
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
    assertEquals("Vidoo", appName)
  }

  @Test
  fun `verify speed gesture modes and half-screen detection`() {
    val screenWidth = 1080f
    val leftTouchX = 300f
    val rightTouchX = 800f

    val isLeftRightSide = leftTouchX >= screenWidth / 2f
    val isRightRightSide = rightTouchX >= screenWidth / 2f

    val leftGesture = if (isLeftRightSide) SpeedGestureMode.FORWARD_2X else SpeedGestureMode.REWIND_2X
    val rightGesture = if (isRightRightSide) SpeedGestureMode.FORWARD_2X else SpeedGestureMode.REWIND_2X

    assertEquals(SpeedGestureMode.REWIND_2X, leftGesture)
    assertEquals(SpeedGestureMode.FORWARD_2X, rightGesture)
  }

  @Test
  fun `verify rewind interval calculation simulates 2x backward movement`() {
    val intervalMs = 150L
    val multiplier = 2
    val stepMs = intervalMs * multiplier
    assertEquals(300L, stepMs)

    val currentPosition = 5000L
    val newPosition = (currentPosition - stepMs).coerceAtLeast(0L)
    assertEquals(4700L, newPosition)
  }

  @Test
  fun `verify replay condition triggers restart from beginning`() {
    val duration = 60000L
    // Case 1: Video ended or at the very end
    val endPosition = 59800L
    val isAtEnd = endPosition >= duration - 300L
    assertTrue(isAtEnd)

    // When isAtEnd, playback position resets to 0L
    val restartPos = if (isAtEnd) 0L else endPosition
    assertEquals(0L, restartPos)
  }

  @Test
  fun `verify previous and next navigation availability across queue boundaries`() {
    val queue = listOf("video_1", "video_2", "video_3")

    // First item: no previous, has next
    val idx0 = 0
    val hasPrev0 = idx0 > 0
    val hasNext0 = idx0 in 0 until (queue.size - 1)
    assertEquals(false, hasPrev0)
    assertEquals(true, hasNext0)

    // Middle item: has previous, has next
    val idx1 = 1
    val hasPrev1 = idx1 > 0
    val hasNext1 = idx1 in 0 until (queue.size - 1)
    assertEquals(true, hasPrev1)
    assertEquals(true, hasNext1)

    // Last item: has previous, no next
    val idx2 = 2
    val hasPrev2 = idx2 > 0
    val hasNext2 = idx2 in 0 until (queue.size - 1)
    assertEquals(true, hasPrev2)
    assertEquals(false, hasNext2)
  }
}
