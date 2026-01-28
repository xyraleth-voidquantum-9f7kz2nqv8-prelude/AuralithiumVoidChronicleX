####################################
# NAMA CLASS TETAP (IDENTITAS APP)
####################################
-keepnames class com.lagradost.cloudstream3.**

####################################
# R8 MODE AMAN (ANTI CLASS SAMPAH)
####################################
-allowaccessmodification
-adaptclassstrings
# MATIIN BIANG KEROK
#-overloadaggressively
#-repackageclasses ''

####################################
# BIAR LAMBDA TETAP $r8$lambda$
####################################
-keepattributes InnerClasses,EnclosingMethod

####################################
# ACTIVITY / FRAGMENT (CONSTRUCTOR ONLY)
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
# KEEP PACKAGE (BIAR GAK MUNCUL ah / bxc / alk)
####################################
-keeppackagenames \
_COROUTINE.**, \
android.**, \
androidx.**, \
app.cash.quickjs.**, \
coil3.**, \
com.**, \
go.**, \
io.github.**, \
j$.**, \
java.**, \
javax.**, \
junit.**, \
kotlin.**, \
kotlinx.**, \
me.xdrop.**, \
okhttp3.**, \
okio.**, \
org.**, \
qrcode.**, \
retrofit2.**, \
torrServer.**

####################################
# KEEP CLASS PENTING (DEX AMAN)
####################################
-keep class _COROUTINE.** { *; }
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
# BIAR R8 DIEM (NO ERROR ANEH)
####################################
-dontwarn **

####################################
# STRX / STRING ENCRYPTION
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }
