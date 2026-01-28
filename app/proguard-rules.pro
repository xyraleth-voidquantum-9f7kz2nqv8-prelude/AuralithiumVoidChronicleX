####################################
# BIAR R8 TIDAK ERROR
####################################
-dontwarn java.beans.**
-dontwarn javax.script.**
-dontwarn org.mozilla.javascript.**
-dontwarn com.fasterxml.jackson.databind.ext.**
-dontwarn kotlin.Metadata

####################################
# JANGAN ACAK LIBRARY (BIAR RAPI)
####################################
-keep class androidx.** { *; }
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keep class org.jsoup.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class org.mozilla.javascript.** { *; }
-keep class app.cash.quickjs.** { *; }

####################################
# ACAK KODE APP (TAPI JANGAN DIHAPUS)
####################################
-keep class com.lagradost.cloudstream3.** { *; }
-keep class com.cloudplay.app.** { *; }
-allowobfuscation class com.lagradost.cloudstream3.**
-allowobfuscation class com.cloudplay.app.**

####################################
# Activity & Fragment (WAJIB LENGKAP)
####################################
-keep class * extends android.app.Activity { *; }
-keep class * extends androidx.appcompat.app.AppCompatActivity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }

####################################
# Jackson (reflection)
####################################
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonProperty <fields>;
    @com.fasterxml.jackson.annotation.JsonCreator <methods>;
}

####################################
# ENUM
####################################
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

####################################
# Annotation
####################################
-keepattributes *Annotation*
