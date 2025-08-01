# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Hilt components
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp

# Keep Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep Retrofit interfaces
-keep interface * {
    @retrofit2.http.* <methods>;
}

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    # lookup for plugin generated serializable classes
    *** Companion;
    *** $serializer;
    *** $$serializer;
    *** $$;
}

-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Bitcoin-related classes
-keep class org.bitcoinj.** { *; }
-keep class org.bouncycastle.** { *; }
-keep class org.web3j.** { *; }

# Keep crypto-related classes
-keep class com.cryptowallet.app.core.crypto.** { *; }
-keep class com.cryptowallet.app.core.security.** { *; }

# Keep database entities
-keep class com.cryptowallet.app.data.database.entities.** { *; }

# Keep API models
-keep class com.cryptowallet.app.data.api.** { *; }

# OkHttp platform used only on JVM and when Conscrypt dependency is available.
-dontwarn okhttp3.internal.platform.ConscryptPlatform
-dontwarn org.conscrypt.ConscryptHostnameVerifier

# Keep native libraries for crypto operations
-keep class **.R$*