# Add project specific ProGuard rules here.

# Media3 ExoPlayer ProGuard rules
-keep class androidx.media3.exoplayer.** { *; }
-keep class androidx.media3.common.** { *; }
-keep class androidx.media3.ui.** { *; }
-keep class androidx.media3.datasource.** { *; }
-dontwarn androidx.media3.**

# Room local database rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# App database entities and DAOs
-keep class com.example.data.db.** { *; }
-keep class com.example.data.model.** { *; }

# Coil image/thumbnail loader
-dontwarn coil.**

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

