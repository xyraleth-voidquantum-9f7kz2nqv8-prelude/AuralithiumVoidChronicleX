####################################
# CLOUDSTREAM CORE
####################################
-keep class com.lagradost.cloudstream3.** { *; }

####################################
# PLUGIN SYSTEM
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

-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn app.cash.quickjs.**

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
-keepclassmembers class * {
    @kotlin.Metadata *;
}

####################################
# STRX
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }
