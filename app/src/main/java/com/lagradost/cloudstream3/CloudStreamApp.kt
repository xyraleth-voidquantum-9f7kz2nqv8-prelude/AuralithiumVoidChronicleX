package com.lagradost.cloudstream3

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.lagradost.api.setContext
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.isLayout
import com.lagradost.cloudstream3.utils.AppContextUtils.openBrowser
import com.lagradost.cloudstream3.utils.DataStore.getKey
import com.lagradost.cloudstream3.utils.DataStore.getKeys
import com.lagradost.cloudstream3.utils.DataStore.removeKey
import com.lagradost.cloudstream3.utils.DataStore.removeKeys
import com.lagradost.cloudstream3.utils.DataStore.setKey
import com.lagradost.cloudstream3.utils.ImageLoader.buildImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.PrintStream
import java.lang.ref.WeakReference
import kotlin.system.exitProcess

/* =======================
   CRASH HANDLER
   ======================= */
class ExceptionHandler(
    private val errorFile: File,
    private val onError: (() -> Unit)
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, error: Throwable) {
        try {
            val threadId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA)
                    thread.threadId()
                else thread.id

            PrintStream(errorFile).use { ps ->
                ps.println("Currently loading extension: ${PluginManager.currentlyLoading ?: "none"}")
                ps.println("Fatal exception on thread ${thread.name} ($threadId)")
                error.printStackTrace(ps)
            }
        } catch (_: FileNotFoundException) {}

        try { onError() } catch (_: Exception) {}
        exitProcess(1)
    }
}

/* =======================
   APPLICATION (FINAL)
   ======================= */
@Prerelease
class CloudStreamApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        // 🔥 1️⃣ INIT GLOBAL CONTEXT
        context = applicationContext
        AcraApplication.context = applicationContext

        // 🔥 2️⃣ AUTO REPO + AUTO PLUGIN (PALING AMAN)
        CoroutineScope(Dispatchers.Default).launch {
            Initializer.start(applicationContext)
        }

        // 🔥 3️⃣ CRASH HANDLER
        ExceptionHandler(filesDir.resolve("last_error")) {
            val intent = packageManager
                .getLaunchIntentForPackage(packageName)
            startActivity(Intent.makeRestartActivityTask(intent!!.component))
        }.also {
            exceptionHandler = it
            Thread.setDefaultUncaughtExceptionHandler(it)
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        context = base
        AcraApplication.context = base
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return buildImageLoader(applicationContext)
    }

    companion object {
        var exceptionHandler: ExceptionHandler? = null

        private var _context: WeakReference<Context>? = null
        var context: Context?
            get() = _context?.get()
            private set(value) {
                if (value != null) {
                    _context = WeakReference(value)
                    setContext(WeakReference(value))
                }
            }

        /* ===== Helpers ===== */

        tailrec fun Context.getActivity(): Activity? = when (this) {
            is Activity -> this
            is ContextWrapper -> baseContext.getActivity()
            else -> null
        }

        fun <T : Any> setKeyClass(path: String, value: T) {
            context?.setKey(path, value)
        }

        inline fun <reified T : Any> getKey(path: String): T? {
            return context?.getKey(path)
        }

        fun getKeys(folder: String): List<String>? {
            return context?.getKeys(folder)
        }

        fun removeKey(path: String) {
            context?.removeKey(path)
        }

        fun removeKeys(folder: String): Int? {
            return context?.removeKeys(folder)
        }

        fun openBrowser(
            url: String,
            fallbackWebView: Boolean = false,
            fragment: Fragment? = null
        ) {
            context?.openBrowser(url, fallbackWebView, fragment)
        }

        fun openBrowser(url: String, activity: FragmentActivity?) {
            openBrowser(
                url,
                isLayout(TV or EMULATOR),
                activity?.supportFragmentManager?.fragments?.lastOrNull()
            )
        }
    }
}