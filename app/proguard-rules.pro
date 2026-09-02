# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Room Database ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Firebase ---
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# --- Stream Chat SDK ---
-keep class io.getstream.** { *; }
-dontwarn io.getstream.**

# --- MPAndroidChart ---
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# --- Lottie ---
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# --- Kotlin Serialization ---
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# --- Data classes used with Firestore ---
-keep class com.sanket_satpute_20.ironmind.data.** { *; }
-keep class com.sanket_satpute_20.ironmind.social.SocialModels** { *; }

# --- Compose ---
-dontwarn androidx.compose.**

# --- Glance Widgets ---
-keep class com.sanket_satpute_20.ironmind.widget.** { *; }