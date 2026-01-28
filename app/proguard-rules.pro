####################################
# NAMA CLASS TETAP
# ISI DALAM ACAK TOTAL
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
# KOTLIN & ANNOTATION
####################################
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*

####################################
# DONTWARN (BIAR R8 DIEM)
####################################
-dontwarn **
