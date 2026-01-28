# ===============================
# Project-specific ProGuard rules
# ===============================

# Keep AndroidX classes
-keep class androidx.** { *; }
-dontwarn androidx.**

# Keep Kotlin metadata (penting untuk reflection dan coroutines)
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.** { *; }

# Keep ViewBinding classes
-keep class **Binding { *; }

# Keep all classes used in your layout XML (custom views)
-keep class * extends android.view.View { *; }

# Keep any classes annotated with @Keep
-keep @androidx.annotation.Keep class * { *; }

# Keep all your main Activities, Fragments, ViewModels
-keep class com.lagradost.cloudstream3.** { *; }

# Keep enums (jangan di-obfuscate)
-keepclassmembers enum * { *; }

# Keep classes used by libraries that use reflection
-keep class com.google.gson.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-keep class kotlinx.coroutines.** { *; }
-keep class com.lagradost.cloudstream3.ui.** { *; }

# Keep public methods of classes implementing Android interfaces (listener callbacks)
-keepclassmembers class * implements android.view.View$OnClickListener {
    public void onClick(android.view.View);
}
-keepclassmembers class * implements android.app.DialogInterface$OnClickListener {
    public void onClick(android.content.DialogInterface, int);
}

# If you use JavaScript interfaces in WebView, uncomment and update:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
#}