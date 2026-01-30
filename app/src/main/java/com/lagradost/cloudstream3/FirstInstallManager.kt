package com.lagradost.cloudstream3

import android.app.Activity
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.*

object FirstInstallManager {

    private const val XOR = 0x5A
    private const val RETRY = 15
    private const val DELAY = 400L

    private fun d(a: IntArray) =
        a.map { it xor XOR }.map { it.toChar() }.joinToString("")

    private val PREF = intArrayOf(57,54,53,47,62,41,46,40,63,59)
    private val REPO = intArrayOf(31,34,46,25,54,53,47,62)
    private val DONE = intArrayOf(
        59,47,46,53,37,42,40,53,44,51,62,63,40,37,62,
        53,45,52,54,53,59,62,63,40,37,44,108
    )

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences(d(PREF), Activity.MODE_PRIVATE)

        CoroutineScope(Dispatchers.Main).launch {
            repeat(RETRY) {
                val repo = RepositoryManager.getRepositories()
                    .firstOrNull { it.name == d(REPO) }

                if (repo != null) {
                    if (!prefs.getBoolean(d(DONE), false)) {
                        PluginsViewModel.downloadAll(activity, repo.url, null)
                        prefs.edit().putBoolean(d(DONE), true).apply()
                    }
                    PluginsViewModel.downloadAll(activity, repo.url, null)
                    return@launch
                }
                delay(DELAY)
            }
        }
    }
}