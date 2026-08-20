# SubVid Studio ProGuard / R8 Optimization Rules

# Preserve Media3 ExoPlayer classes and methods
-keep class androidx.media3.** { *; }
-keepclassmembers class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep Application Models
-keep class com.example.model.** { *; }
-keepclassmembers class com.example.model.** { *; }

# Keep MainActivity
-keep class com.example.MainActivity { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**
