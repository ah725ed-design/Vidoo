# Add project specific ProGuard rules here.

# Media3 ExoPlayer ProGuard rules (Media3 AARs provide consumer rules; suppress optional missing dependencies)
-dontwarn androidx.media3.**
-dontwarn com.google.common.**

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

