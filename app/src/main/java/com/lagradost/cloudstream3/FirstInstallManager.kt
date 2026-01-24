package com.lagradost.cloudstream3

import android.app.Activity
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.launch

object FirstInstallManager {

    private const val DOWNLOADED = "first_install_provider_done_v1"

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)

        // flag dari Initializer (repo baru ditambahkan)
        if (!prefs.getBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)) return
        if (prefs.getBoolean(DOWNLOADED, false)) return

        activity.lifecycleScope.launch {
            try {
                // pastikan repo sudah ke-load
                RepositoryManager.loadRepositories()

                val repo = RepositoryManager.getRepositories()
                    .firstOrNull { it.name == "ExtCloud" }
                    ?: return@launch

                // download semua plugin dari repo itu
                PluginsViewModel.downloadAll(
                    activity,
                    repo.url,
                    null
                )

                prefs.edit()
                    .putBoolean(DOWNLOADED, true)
                    .putBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)
                    .apply()

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}