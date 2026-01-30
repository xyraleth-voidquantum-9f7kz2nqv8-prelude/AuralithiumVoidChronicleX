package com.lagradost.cloudstream3

import android.app.Activity
import android.util.Base64
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Initializer {

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1" // supaya FirstInstallManager bisa akses
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    // 🔐 Base64 + XOR + split
    private const val P1 = "CxgbBRdKQ04LAhtBEg0EBBQb"
    private const val P2 = "Fh8KBwcfAhUcDRhBFgsdQwUM"
    private const val P3 = "EQNWR0s1FBU6DwMaEUsdDQgX"
    private const val P4 = "TB4KBQteBhIWDQ=="

    private fun repoUrl(): String {
        val key = "cloudplay".toByteArray()
        val encoded = P1 + P2 + P3 + P4
        val data = Base64.decode(encoded, Base64.DEFAULT)
        val out = ByteArray(data.size)
        for (i in data.indices) {
            out[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
        return String(out)
    }

    fun start(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Activity.MODE_PRIVATE)
        val repo = RepositoryData(
            name = "ExtCloud",
            url = repoUrl(),
            iconUrl = null
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1️⃣ Tambah repo sekali saja
                if (!prefs.getBoolean(AUTO_REPO_FLAG, false)) {
                    RepositoryManager.addRepository(repo)
                    // Auto-download semua plugin pertama kali
                    RepositoryManager.downloadRepository(repo)
                    prefs.edit()
                        .putBoolean(AUTO_REPO_FLAG, true)
                        .putBoolean(NEED_AUTO_DOWNLOAD, true)
                        .apply()
                }

                // 2️⃣ Auto-download plugin baru setiap app start
                if (RepositoryManager.hasNewPlugins(repo)) {
                    RepositoryManager.downloadRepository(repo)
                }

            } catch (_: Throwable) {
                // silent, jangan crash
            }
        }
    }
}