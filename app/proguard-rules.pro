####################################
# NAMA CLASS TETAP
####################################
-keepnames class com.lagradost.cloudstream3.**

####################################
# R8 MODE AGRESIF
####################################
-allowaccessmodification
-overloadaggressively
-repackageclasses ''
-adaptclassstrings

####################################
# BIAR LAMBDA JADI $r8$lambda$
####################################
-keepattributes InnerClasses,EnclosingMethod

####################################
# ACTIVITY / FRAGMENT (CUMA CONSTRUCTOR)
####################################
-keepclassmembers class * extends android.app.Activity { <init>(...); }
-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity { <init>(...); }
-keepclassmembers class * extends androidx.fragment.app.Fragment { <init>(...); }

####################################
# KOTLIN & ANNOTATION
####################################
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

####################################
# KEEP NAMES PENTING (DEX 6)
####################################
-keep class _COROUTINE.** { *; }
-keep class afo.hf.hqtkbbwxq.** { *; }
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class app.cash.zipline.** { *; }
-keep class bin.mt.signature.** { *; }
-keep class coil3.** { *; }
-keep class com.** { *; }
-keep class go.** { *; }
-keep class io.github.** { *; }
-keep class j$.** { *; }
-keep class java.** { *; }
-keep class javax.** { *; }
-keep class junit.** { *; }
-keep class kbp.lz.areefayil.** { *; }
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
# BIAR R8 DIEM
####################################
-dontwarn **

####################################
# STRX & SECURITY (STRING ENCRYPTION)
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }
