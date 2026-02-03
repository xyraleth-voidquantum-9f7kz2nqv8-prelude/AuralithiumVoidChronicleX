####################################
# CLOUDSTREAM FINAL PROGUARD (AMAN)
####################################

####################################
# IDENTITAS APP (JANGAN DIUBAH)
####################################
-keep class com.lagradost.cloudstream3.** { *; }
-dontwarn com.lagradost.cloudstream3.**

####################################
# APPLICATION INIT (WAJIB)
####################################
-keep class com.lagradost.cloudstream3.CloudStreamApp { *; }

####################################
# ACTIVITY / FRAGMENT (REFLECTION SAFE)
####################################
-keep class * extends android.app.Activity { <init>(...); }
-keep class * extends androidx.appcompat.app.AppCompatActivity { <init>(...); }
-keep class * extends androidx.fragment.app.Fragment { <init>(...); }

####################################
# CLOUDSTREAM CORE (WAJIB HIDUP)
####################################
-keep class com.lagradost.cloudstream3.extractors.** { *; }
-keepclassmembers class com.lagradost.cloudstream3.extractors.** {
    public <init>();
}
-keep class com.lagradost.cloudstream3.network.** { *; }
-keep class com.lagradost.cloudstream3.plugins.** { *; }
-keep class com.lagradost.cloudstream3.utils.** { *; }

####################################
# PLUGIN PROVIDERS / EXTENSIONS
# Semua class implement MainAPI tetap hidup (reflection safe)
####################################
-keep class * implements com.lagradost.cloudstream3.MainAPI {
    public <init>(...);
    *;
}

####################################
# KOTLIN & REFLECTION
####################################
-keep class kotlin.Metadata { *; }
-keepattributes *Annotation*
-keepattributes InnerClasses,EnclosingMethod

####################################
# LIBRARY PENTING (DEX UTAMA)
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
# STRING ENCRYPTION (STRX)
####################################
-keep class com.lagradost.cloudstream3.security.StrX { *; }

####################################
# R8 MODE TENANG
####################################
-allowaccessmodification
-dontwarn **

####################################
# PLUGIN LOADER SAFETY TAMBAHAN
# Semua constructor di plugin utils tetap hidup
####################################
-keepclassmembers class com.lagradost.cloudstream3.utils.** {
    public <init>(...);
}
