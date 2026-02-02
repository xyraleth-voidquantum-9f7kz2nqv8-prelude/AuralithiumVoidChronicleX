####################################
# CLOUDSTREAM CORE (WAJIB & AMAN)
####################################
-keep class com.lagradost.cloudstream3.** { *; }
-keepnames class com.lagradost.cloudstream3.**

####################################
# PLUGIN SYSTEM (PALING PENTING)
####################################
-keep class com.lagradost.cloudstream3.plugins.** { *; }
-keep class com.lagradost.cloudstream3.extractors.** { *; }

####################################
# NETWORK & JS ENGINE
####################################
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keep class app.cash.quickjs.** { *; }

####################################
# KOTLIN & COROUTINE
####################################
-keep class kotlin.Metadata { *; }
-keep class kotlinx.coroutines.** { *; }
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

####################################
# REFLECTION SAFETY
####################################
-keepnames class ** implements java.io.Serializable
-keepclassmembers class * {
    @kotlin.Metadata *;
}

####################################
# STRX
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }

####################################
# R8 TENANG
####################################
-dontwarn **
