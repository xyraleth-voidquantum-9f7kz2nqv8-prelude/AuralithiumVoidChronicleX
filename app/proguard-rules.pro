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
-keepclassmembers class com.lagradost.cloudstream3.plugins.** { *; }
-keepclassmembers class com.lagradost.cloudstream3.extractors.** { *; }

####################################
# NETWORK & JS ENGINE
####################################
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keep class app.cash.quickjs.** { *; }

# Rhino / JavaScript engine
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.javascript.engine.** { *; }
-dontwarn org.mozilla.javascript.**
-dontwarn java.beans.**
-dontwarn javax.script.**

####################################
# JSON SAFETY
####################################
-keep class org.json.** { *; }
-dontwarn org.json.**

####################################
# INTERNAL / MODDED CLASSES
####################################
-keep class sf.** { *; }

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
# STRX (Security)
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }

####################################
# R8 WARNING SUPPRESSION
####################################
-dontwarn **

####################################
# OPTIONAL (SAFE) SHRINK RULES
####################################
-keep class java.beans.** { *; }
-keep class javax.script.** { *; }
