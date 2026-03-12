# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Moshi
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Retrofit
-keep class retrofit2.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# App models
-keep class com.aiphone.agent.data.remote.models.** { *; }
-keep class com.aiphone.agent.domain.model.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# Accessibility
-keep class com.aiphone.agent.core.accessibility.** { *; }
