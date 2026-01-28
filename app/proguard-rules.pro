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
# JANGAN DIHAPUS, supaya dex tetap 6
####################################
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class com.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class org.** { *; }
-keep class io.github.** { *; }
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

####################################
# BIAR R8 DIEM
####################################
-dontwarn **