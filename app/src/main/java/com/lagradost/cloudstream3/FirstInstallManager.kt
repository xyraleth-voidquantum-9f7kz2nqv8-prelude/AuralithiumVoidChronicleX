package com.lagradost.cloudstream3

import android.app.Activity
import android.content.Context
import android.util.Log
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.*

object FirstInstallManager {

    private const val TAG = "FirstInstallManager"
    private const val DOWNLOADED = "auto_provider_downloaded_v2"
    private const val MAX_RETRY = 10
    private const val RETRY_DELAY = 400L // ms

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)

        // 1️⃣ Tidak perlu jalan kalau tidak disinyalkan
        if (!prefs.getBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)) return

        // 2️⃣ Jangan ulang download
        if (prefs.getBoolean(DOWNLOADED, false)) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                var repoUrl: String? = null

                // 3️⃣ Tunggu sampai repository benar-benar ready
                repeat(MAX_RETRY) { attempt ->
                    val repo = RepositoryManager.getRepositories()
                        .firstOrNull { it.name == "ExtCloud" }

                    if (repo != null) {
                        repoUrl = repo.url
                        Log.i(TAG, "Repo ExtCloud ready (attempt ${attempt + 1})")
                        return@repeat
                    }

                    Log.d(TAG, "Repo belum siap, retry ${attempt + 1}/$MAX_RETRY")
                    delay(RETRY_DELAY)
                }

                // 4️⃣ Kalau masih null → stop aman
                val finalRepoUrl = repoUrl ?: run {
                    Log.e(TAG, "Repo ExtCloud tidak ditemukan, auto-download dibatalkan")
                    return@launch
                }

                // 5️⃣ Download semua plugin
                PluginsViewModel.downloadAll(
                    activity,
                    finalRepoUrl,
                    null
                )

                // 6️⃣ Tandai selesai (ANTI LOOP)
                prefs.edit()
                    .putBoolean(DOWNLOADED, true)
                    .putBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)
                    .apply()

                Log.i(TAG, "Auto download plugin ExtCloud selesai")

            } catch (e: Throwable) {
                Log.e(TAG, "Auto download plugin gagal", e)
            }
        }
    }
}
