package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val resumePlayback: Boolean = true,
    val subtitleFontSizeSp: Float = 18f,
    val subtitleColor: String = "White", // White, Yellow, Cyan, Green
    val subtitleBackground: Boolean = true,
    val amoledBlack: Boolean = true,
    val defaultPlaybackSpeed: Float = 1.0f,
    val autoLock: Boolean = false,
    val autoLockTimeoutSec: Int = 30, // 15, 30, 60, 120
    val hideShortVideos: Boolean = false,
    val shortVideoThresholdSec: Int = 30, // 30, 60, 120
    val appLanguage: String = "en" // "en" or "ar"
)

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vidoo_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val systemLang = if (java.util.Locale.getDefault().language == "ar") "ar" else "en"
        return AppSettings(
            resumePlayback = prefs.getBoolean("resume_playback", true),
            subtitleFontSizeSp = prefs.getFloat("subtitle_font_size", 18f),
            subtitleColor = prefs.getString("subtitle_color", "White") ?: "White",
            subtitleBackground = prefs.getBoolean("subtitle_bg", true),
            amoledBlack = prefs.getBoolean("amoled_black", true),
            defaultPlaybackSpeed = prefs.getFloat("default_playback_speed", 1.0f),
            autoLock = prefs.getBoolean("auto_lock", false),
            autoLockTimeoutSec = prefs.getInt("auto_lock_timeout_sec", 30),
            hideShortVideos = prefs.getBoolean("hide_short_videos", false),
            shortVideoThresholdSec = prefs.getInt("short_video_threshold_sec", 30),
            appLanguage = prefs.getString("app_language", systemLang) ?: systemLang
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

    fun setAutoLock(enabled: Boolean) {
        prefs.edit().putBoolean("auto_lock", enabled).apply()
        _settings.value = _settings.value.copy(autoLock = enabled)
    }

    fun setAutoLockTimeoutSec(seconds: Int) {
        prefs.edit().putInt("auto_lock_timeout_sec", seconds).apply()
        _settings.value = _settings.value.copy(autoLockTimeoutSec = seconds)
    }

    fun setHideShortVideos(enabled: Boolean) {
        prefs.edit().putBoolean("hide_short_videos", enabled).apply()
        _settings.value = _settings.value.copy(hideShortVideos = enabled)
    }

    fun setShortVideoThresholdSec(seconds: Int) {
        prefs.edit().putInt("short_video_threshold_sec", seconds).apply()
        _settings.value = _settings.value.copy(shortVideoThresholdSec = seconds)
    }

    fun setAppLanguage(langCode: String) {
        prefs.edit().putString("app_language", langCode).apply()
        _settings.value = _settings.value.copy(appLanguage = langCode)
    }
}
