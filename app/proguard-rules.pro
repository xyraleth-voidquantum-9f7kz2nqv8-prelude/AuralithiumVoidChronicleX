####################################
# PACKAGE UTAMA TETAP
####################################
-keepnames class com.lagradost.cloudstream3.**

####################################
# OBFUSCATE AGRESIF TAPI AMAN
####################################
-allowaccessmodification
-overloadaggressively
-adaptclassstrings
-flattenpackagehierarchy 'x'

####################################
# LAMBDA
####################################
-keepattributes InnerClasses,EnclosingMethod

####################################
# ACTIVITY / FRAGMENT
####################################
-keepclassmembers class * extends android.app.Activity {
    <init>(...);
}
-keepclassmembers class * extends androidx.appcompat.app.AppCompatActivity {
    <init>(...);
}
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    <init>(...);
}

####################################
# KOTLIN
####################################
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

####################################
# FORCE MULTI DEX SPLIT
####################################
-keep class android.** { *; }
-keep class androidx.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }
-keep class org.** { *; }

####################################
# DIEMIN WARNING
####################################
-dontwarn **
