########################################
# ProGuard khusus untuk CloudStream Mod
########################################

# ------------------------------------------------
# 1️⃣ Aturan dasar
# Semua kelas dan method di package CloudStream
# dipertahankan agar plugin & extractor aman
# ------------------------------------------------
-keep class com.lagradost.cloudstream3.** { *; }
-dontwarn com.lagradost.cloudstream3.**

# ------------------------------------------------
# 2️⃣ Kotlin & Serialization
# Agar reflection & serialisasi tidak rusak
# ------------------------------------------------
-keepclassmembers class kotlin.Metadata { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# ------------------------------------------------
# 3️⃣ Room / Database (jika ada)
# ------------------------------------------------
-keepclassmembers class androidx.room.** { *; }

# ------------------------------------------------
# 4️⃣ WebView JS Interfaces (jika digunakan)
# Ganti dengan nama kelas interface JS kamu
# ------------------------------------------------
#-keepclassmembers class com.example.MyWebAppInterface {
#    public *;
#}

# ------------------------------------------------
# 5️⃣ Nomor baris untuk debug
# Stack trace tetap menunjukkan baris kode asli
# ------------------------------------------------
-keepattributes SourceFile,LineNumberTable

# ------------------------------------------------
# 6️⃣ Sembunyikan nama file sumber (opsional)
# Hanya untuk release agar lebih aman
# ------------------------------------------------
#-renamesourcefileattribute SourceFile

# ------------------------------------------------
# 7️⃣ Tips tambahan
# Jika ada library lain yang obfuscate bermasalah,
# tambahkan aturan khusus di sini.
# ------------------------------------------------
