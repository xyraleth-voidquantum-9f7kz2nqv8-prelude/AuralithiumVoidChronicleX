########################################
# ProGuard Super Aman CloudStream Mod
# Langsung bisa build signed APK
########################################

# ------------------------------------------------
# 1️⃣ Jangan hancurkan kelas penting CloudStream
# Semua core, plugin, dan extractor tetap aman
# ------------------------------------------------
-keep class com.lagradost.cloudstream3.** { *; }
-dontwarn com.lagradost.cloudstream3.**

# Plugin / extractor eksternal (jika ada)
-keep class com.lagradost.cloudstream3.extractor.** { *; }

# ------------------------------------------------
# 2️⃣ Kotlin & Reflection
# Agar Kotlin, reflection, dan serialisasi aman
# ------------------------------------------------
-keepclassmembers class kotlin.Metadata { *; }
-keepclassmembers class kotlin.reflect.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# ------------------------------------------------
# 3️⃣ Room / Database (jika digunakan)
# ------------------------------------------------
-keepclassmembers class androidx.room.** { *; }

# ------------------------------------------------
# 4️⃣ Library AndroidX tambahan
# ------------------------------------------------
-keep class androidx.** { *; }
-dontwarn androidx.**

# ------------------------------------------------
# 5️⃣ WebView JS Interfaces (opsional)
# Jika aplikasi pakai WebView dengan JS, ganti nama kelasnya
# ------------------------------------------------
#-keepclassmembers class com.example.MyWebAppInterface {
#    public *;
#}

# ------------------------------------------------
# 6️⃣ Nomor baris stack trace
# Agar crash report tetap jelas
# ------------------------------------------------
-keepattributes SourceFile,LineNumberTable

# ------------------------------------------------
# 7️⃣ Sembunyikan nama file sumber (opsional)
# ------------------------------------------------
#-renamesourcefileattribute SourceFile

# ------------------------------------------------
# 8️⃣ Nonaktifkan sementara optimisasi R8 untuk aman
# Bisa dihapus kalau semua sudah stabil
# ------------------------------------------------
-dontobfuscate
-dontoptimize
-dontshrink
