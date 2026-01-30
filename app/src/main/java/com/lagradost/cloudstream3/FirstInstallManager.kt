package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object FirstInstallManager {
    private const val TAG = "FirstInstallManager"
    private const val DOWNLOADED = "auto_provider_downloaded_v2"
    private const val MAX_RETRY = 10
    private const val RETRY_DELAY = 400L

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Activity.MODE_PRIVATE)

        activity.lifecycleScope.launch(Dispatchers.Main) {
            try {
                // Tunggu sampai repository ExtCloud ready
                val repoUrl = waitForRepoUrl() ?: run {
                    Log.e(TAG, "Repo ExtCloud tidak ditemukan, auto-download dibatalkan")
                    return@launch
                }

                // Auto-download pertama kali
                if (!prefs.getBoolean(DOWNLOADED, false)) {
                    PluginsViewModel.downloadAll(activity, repoUrl, null)
                    prefs.edit().putBoolean(DOWNLOADED, true).apply()
                    Log.i(TAG, "Auto-download plugin ExtCloud pertama selesai")
                }

                // Auto-download plugin baru
                val newPlugins = getNewPlugins(activity, repoUrl)
                if (newPlugins.isNotEmpty()) {
                    PluginsViewModel.downloadAll(activity, repoUrl, newPlugins)
                    Log.i(TAG, "Auto-download plugin baru: ${newPlugins.joinToString()}")
                }

            } catch (e: Throwable) {
                Log.e(TAG, "Auto-download plugin gagal", e)
            }
        }
    }

    private suspend fun waitForRepoUrl(): String? {
        repeat(MAX_RETRY) { attempt ->
            val repo = RepositoryManager.getRepositories()
                .firstOrNull { it.name == "ExtCloud" }
            if (repo != null) return repo.url
            delay(RETRY_DELAY)
        }
        return null
    }

    private suspend fun getNewPlugins(activity: Activity, repoUrl: String): List<String> =
        withContext(Dispatchers.IO) {
            val all = RepositoryManager.getRepoPlugins(repoUrl)?.map { it.second.internalName } ?: emptyList()
            all.filter { name -> !PluginManager.getPluginPath(activity, name, repoUrl).exists() }
        }
}