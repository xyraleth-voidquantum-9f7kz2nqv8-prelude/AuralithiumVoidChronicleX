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
    private const val XOR_KEY = 0x5A

    private fun d(data: IntArray): String =
        data.map { it xor XOR_KEY }.map { it.toChar() }.joinToString("")

    private val PREF_NAME = intArrayOf(57, 54, 53, 47, 62, 41, 46, 40, 63, 59)
    private val REPO_NAME = intArrayOf(31, 34, 46, 25, 54, 53, 47, 62)
    private val LOG_REPO_READY = intArrayOf(8, 63, 42, 53, 122, 31, 34, 46, 25, 54, 53, 47, 62, 122, 40, 63, 59, 62, 35)
    private val LOG_REPO_WAIT = intArrayOf(8, 63, 42, 53, 122, 56, 63, 54, 47, 55, 122, 41, 51, 59, 42)
    private val LOG_REPO_NOT_FOUND = intArrayOf(8, 63, 42, 53, 122, 46, 51, 62, 59, 49, 122, 62, 51, 62, 51, 57, 46, 47, 47, 59, 44, 122, 56, 59, 46, 59, 54, 49, 59, 46)
    private val LOG_FIRST_DONE = intArrayOf(59, 47, 46, 53, 122, 62, 53, 52, 47, 51, 59, 62, 122, 42, 63, 40, 62, 59, 53, 59)
    private val LOG_FAILED = intArrayOf(59, 47, 46, 53, 122, 61, 59, 61, 59, 54)

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences(d(PREF_NAME), Activity.MODE_PRIVATE)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                var repoUrl: String? = null
                repeat(MAX_RETRY) { attempt ->
                    val repo = RepositoryManager.getRepositories()
                        .firstOrNull { it.name == d(REPO_NAME) }
                    if (repo != null) {
                        repoUrl = repo.url
                        Log.i(TAG, d(LOG_REPO_READY) + " (${attempt + 1})")
                        return@repeat
                    }
                    Log.d(TAG, d(LOG_REPO_WAIT) + " (${attempt + 1}/$MAX_RETRY)")
                    delay(RETRY_DELAY)
                }

                val finalRepoUrl = repoUrl ?: run {
                    Log.e(TAG, d(LOG_REPO_NOT_FOUND))
                    return@launch
                }

                if (!prefs.getBoolean(DOWNLOADED, false)) {
                    PluginsViewModel.downloadAll(activity, finalRepoUrl, null)
                    prefs.edit().putBoolean(DOWNLOADED, true).apply()
                    Log.i(TAG, d(LOG_FIRST_DONE))
                }

                PluginsViewModel.downloadAll(activity, finalRepoUrl, null)

            } catch (_: Throwable) {
                Log.e(TAG, d(LOG_FAILED))
            }
        }
    }
}