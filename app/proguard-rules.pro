####################################
# IDENTITAS APP (NAMA TETAP)
####################################
-keepnames class com.lagradost.cloudstream3.**

####################################
# R8 MODE AMAN (ANTI CLASS SAMPAH)
####################################
-allowaccessmodification
# ❌ Jangan pakai adaptclassstrings / overloadaggressively / repackageclasses
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
-keep class * extends android.app.Activity { <init>(...); }
-keep class * extends androidx.appcompat.app.AppCompatActivity { <init>(...); }
-keep class * extends androidx.fragment.app.Fragment { <init>(...); }

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
# PACKAGE / LIBRARY PENTING (DEX UTAMA)
####################################
-keep class _COROUTINE.** { *; }
-keep class afo.hf.hqtkbbwxq.** { *; }
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class app.cash.quickjs.** { *; }
-keep class coil3.** { *; }
-keep class com.** { *; }
-keep class go.** { *; }
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
-keep class qrcode.** { *; }
-keep class retrofit2.** { *; }
-keep class torrServer.** { *; }

####################################
# JSON SAFETY
####################################
-keep class org.json.** { *; }
-dontwarn org.json.**

####################################
# STRX / STRING ENCRYPTION
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }

####################################
# TENANG R8
####################################
-dontwarn **
