package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val resumePlayback: Boolean = true,
    val subtitleFontSizeSp: Float = 18f,
    val subtitleColor: String = "White", // White, Orange, Yellow
    val subtitleBackground: Boolean = true,
    val amoledBlack: Boolean = true,
    val defaultPlaybackSpeed: Float = 1.0f
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vidoo_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        return AppSettings(
            resumePlayback = prefs.getBoolean("resume_playback", true),
            subtitleFontSizeSp = prefs.getFloat("subtitle_font_size", 18f),
            subtitleColor = prefs.getString("subtitle_color", "White") ?: "White",
            subtitleBackground = prefs.getBoolean("subtitle_bg", true),
            amoledBlack = prefs.getBoolean("amoled_black", true),
            defaultPlaybackSpeed = prefs.getFloat("default_playback_speed", 1.0f)
        )
    }

    fun setResumePlayback(enabled: Boolean) {
        prefs.edit().putBoolean("resume_playback", enabled).apply()
        _settings.value = _settings.value.copy(resumePlayback = enabled)
    }

    fun setSubtitleFontSize(fontSizeSp: Float) {
        prefs.edit().putFloat("subtitle_font_size", fontSizeSp).apply()
        _settings.value = _settings.value.copy(subtitleFontSizeSp = fontSizeSp)
    }

    fun setSubtitleColor(color: String) {
        prefs.edit().putString("subtitle_color", color).apply()
        _settings.value = _settings.value.copy(subtitleColor = color)
    }

    fun setSubtitleBackground(enabled: Boolean) {
        prefs.edit().putBoolean("subtitle_bg", enabled).apply()
        _settings.value = _settings.value.copy(subtitleBackground = enabled)
    }

    fun setAmoledBlack(enabled: Boolean) {
        prefs.edit().putBoolean("amoled_black", enabled).apply()
        _settings.value = _settings.value.copy(amoledBlack = enabled)
    }

    fun setDefaultPlaybackSpeed(speed: Float) {
        prefs.edit().putFloat("default_playback_speed", speed).apply()
        _settings.value = _settings.value.copy(defaultPlaybackSpeed = speed)
    }
}
