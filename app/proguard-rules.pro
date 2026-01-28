####################################
# NAMA CLASS TETAP
####################################
-keepnames class com.lagradost.cloudstream3.**

####################################
# BIAR METHOD ACAK
####################################
-allowaccessmodification
-overloadaggressively
-adaptclassstrings

####################################
# LAMBDA R8
####################################
-keepattributes InnerClasses,EnclosingMethod

####################################
# STRX JANGAN DIHAPUS
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }

####################################
# ACTIVITY / FRAGMENT (AMAN)
####################################
-keepclassmembers class * extends android.app.Activity { <init>(...); }
-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity { <init>(...); }
-keepclassmembers class * extends androidx.fragment.app.Fragment { <init>(...); }

####################################
# KOTLIN
####################################
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

####################################
# DONTWARN
####################################
-dontwarn **
