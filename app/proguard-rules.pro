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
# TO-DOO LONG NAME (DEX 6, NO Ca0…)
####################################
# Ini wajib semua biar dex tetep 6 & nama class jelas
> _COROUTINE
> afo.hf.hqtkbbwxq
> android
> androidx
> app.cash.zipline
> bin.mt.signature
> coil3
> com
> go
> io.github
> j$
> java
> javax
> junit
> kbp.lz.areefayil
> kotlin
> kotlinx
> me.xdrop
> okhttp3
> okio
> org
> qrcode
> retrofit2
> torrServer

####################################
# KEEP NAMES UNTUK SEMUA PACKAGE DI ATAS
####################################
-keepnames class _COROUTINE.** { *; }
-keepnames class afo.hf.hqtkbbwxq.** { *; }
-keepnames class android.** { *; }
-keepnames class androidx.** { *; }
-keepnames class app.cash.zipline.** { *; }
-keepnames class bin.mt.signature.** { *; }
-keepnames class coil3.** { *; }
-keepnames class com.** { *; }
-keepnames class go.** { *; }
-keepnames class io.github.** { *; }
-keepnames class j$.** { *; }
-keepnames class java.** { *; }
-keepnames class javax.** { *; }
-keepnames class junit.** { *; }
-keepnames class kbp.lz.areefayil.** { *; }
-keepnames class kotlin.** { *; }
-keepnames class kotlinx.** { *; }
-keepnames class me.xdrop.** { *; }
-keepnames class okhttp3.** { *; }
-keepnames class okio.** { *; }
-keepnames class org.** { *; }
-keepnames class qrcode.** { *; }
-keepnames class retrofit2.** { *; }
-keepnames class torrServer.** { *; }

####################################
# BIAR R8 DIEM
####################################
-dontwarn **

####################################
# STRX & SECURITY (STRING ENCRYPTION)
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }
