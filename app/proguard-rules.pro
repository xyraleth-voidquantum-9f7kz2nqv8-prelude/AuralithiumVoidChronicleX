####################################
# IDENTITAS APP (NAMA TETAP)
####################################
-keepnames class com.lagradost.cloudstream3.**

####################################
# R8 MODE AMAN (JANGAN RUSAK STRING)
####################################
-allowaccessmodification
# ❌ JANGAN PAKAI adaptclassstrings
#-adaptclassstrings
#-overloadaggressively
#-repackageclasses ''

####################################
# LAMBDA & REFLECTION
####################################
-keepattributes InnerClasses,EnclosingMethod

####################################
# ACTIVITY / FRAGMENT
####################################
-keep class * extends android.app.Activity
-keep class * extends androidx.appcompat.app.AppCompatActivity
-keep class * extends androidx.fragment.app.Fragment

####################################
# CLOUDSTREAM CORE (WAJIB)
####################################
-keep class com.lagradost.cloudstream3.extractors.** { *; }
-keep class com.lagradost.cloudstream3.network.** { *; }
-keep class com.lagradost.cloudstream3.plugins.** { *; }
-keep class com.lagradost.cloudstream3.utils.** { *; }

####################################
# KOTLIN
####################################
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

####################################
# LIBRARY PENTING (NO GARBAGE)
####################################
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class app.cash.quickjs.** { *; }
-keep class coil3.** { *; }
-keep class io.github.** { *; }
-keep class j$.** { *; }
-keep class java.** { *; }
-keep class javax.** { *; }
-keep class junit.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class me.xdrop.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class org.** { *; }
-keep class retrofit2.** { *; }
-keep class torrServer.** { *; }

####################################
# STRX
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }

####################################
# BIAR R8 TENANG
####################################
-dontwarn **
