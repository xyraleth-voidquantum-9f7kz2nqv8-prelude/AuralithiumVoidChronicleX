# =========================
# Jangan crash karena missing Java SE classes
# =========================
-dontwarn java.beans.**
-dontwarn javax.script.**
-dontwarn org.mozilla.javascript.**
-dontwarn com.fasterxml.jackson.databind.ext.**

# =========================
# Keep library classes yang dipakai reflection / serialisasi
# =========================
-keep class org.mozilla.javascript.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class com.lagradost.** { *; }

# =========================
# Jackson: keep JsonProperty / JsonCreator
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.JsonProperty <fields>;
    @com.fasterxml.jackson.annotation.JsonCreator <methods>;
}

# =========================
# Jangan hapus enum values (penting untuk serialize / switch)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# =========================
# Keep semua Activities & Fragments (biar navigation aman)
-keep class * extends androidx.appcompat.app.AppCompatActivity
-keep class * extends androidx.fragment.app.Fragment
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    <init>();
}

# =========================
# Keep annotations biar runtime reflection & serialization aman
-keepattributes *Annotation*
