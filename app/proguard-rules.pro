# Keep stack traces useful
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature, *Annotation*, EnclosingMethod, InnerClasses

# --- Retrofit / OkHttp ---
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit

# --- Gson ---
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken
# Keep DTO classes (mark with @Keep or list packages)
-keep class com.websbaba.nitigrow.data.remote.dto.** { *; }

# --- Hilt / Dagger ---
-keep class * extends dagger.hilt.android.internal.managers.ApplicationComponentManager { *; }
-keep class dagger.hilt.** { *; }
-keep class * extends androidx.lifecycle.ViewModel
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# --- Compose ---
-keep class androidx.compose.runtime.** { *; }

# --- Timber ---
-dontwarn org.jetbrains.annotations.**

# --- Razorpay ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
-keepclasseswithmembers class * { @proguard.annotation.Keep *; }
-keepclasseswithmembers class * { @proguard.annotation.KeepClassMembers *; }
-keep class com.razorpay.** { *; }
-keep interface com.razorpay.** { *; }
-keep class proguard.annotation.** { *; }
-keep interface proguard.annotation.** { *; }
-dontwarn com.razorpay.**
-dontwarn proguard.annotation.**
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# --- Domain models (used by ViewModels/UI; keep enum names for status mapping) ---
-keep enum com.websbaba.nitigrow.domain.model.** { *; }

# --- Crashlytics ---
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# --- Firebase Messaging ---
-keep class com.google.firebase.messaging.** { *; }

# --- OkHttp/SSE ---
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# --- Required for Kotlin reflection used by Gson defaults ---
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
