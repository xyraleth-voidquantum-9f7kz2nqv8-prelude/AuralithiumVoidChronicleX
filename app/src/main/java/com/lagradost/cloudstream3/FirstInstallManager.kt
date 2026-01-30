package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Log
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.*

object FirstInstallManager {

    private const val TAG = "FirstInstallManager"
    private const val DOWNLOADED = "auto_provider_downloaded_v2"
    private const val MAX_RETRY = 10
    private const val RETRY_DELAY = 400L // ms

    /**
     * Jalankan auto-download plugin ExtCloud jika diperlukan.
     * @param activity Activity context
     */
    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Activity.MODE_PRIVATE)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Tunggu sampai repository ExtCloud ready
                var repoUrl: String? = null
                repeat(MAX_RETRY) { attempt ->
                    val repo = RepositoryManager.getRepositories()
                        .firstOrNull { it.name == "ExtCloud" }

                    if (repo != null) {
                        repoUrl = repo.url
                        Log.i(TAG, "Repo ExtCloud ready (attempt ${attempt + 1})")
                        return@repeat
                    }

                    Log.d(TAG, "Repo belum siap, retry ${attempt + 1}/$MAX_RETRY")
                    delay(RETRY_DELAY)
                }

                val finalRepoUrl = repoUrl ?: run {
                    Log.e(TAG, "Repo ExtCloud tidak ditemukan, auto-download dibatalkan")
                    return@launch
                }

                // ✅ Auto-download pertama kali
                if (!prefs.getBoolean(DOWNLOADED, false)) {
                    PluginsViewModel.downloadAll(activity, finalRepoUrl, null)
                    prefs.edit().putBoolean(DOWNLOADED, true).apply()
                    Log.i(TAG, "Auto-download plugin ExtCloud pertama selesai")
                }

                // ✅ Auto-download plugin baru
                val newPlugins = PluginsViewModel.hasNewPlugins(finalRepoUrl)
                if (newPlugins.isNotEmpty()) {
                    PluginsViewModel.downloadRepository(activity, finalRepoUrl, newPlugins)
                    Log.i(TAG, "Auto-download plugin baru: ${newPlugins.joinToString()}")
                }

            } catch (e: Throwable) {
                Log.e(TAG, "Auto-download plugin gagal", e)
            }
        }
    }
}