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

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Strip verbose debug logs in release build for smaller bytecode footprint
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}

