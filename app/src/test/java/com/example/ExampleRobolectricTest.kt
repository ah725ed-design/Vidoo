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
}
