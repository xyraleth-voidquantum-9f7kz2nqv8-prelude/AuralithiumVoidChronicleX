package com.lagradost.cloudstream3

import android.app.Activity
import android.content.Context
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object FirstInstallManager {

    private const val DOWNLOADED = "auto_provider_downloaded_v1"

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)

        if (!prefs.getBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)) return
        if (prefs.getBoolean(DOWNLOADED, false)) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val repo = RepositoryManager.getRepositories()
                    .firstOrNull { it.name == "ExtCloud" }
                    ?: return@launch

                PluginsViewModel.downloadAll(
                    activity,
                    repo.url,
                    null
                )

                prefs.edit()
                    .putBoolean(DOWNLOADED, true)
                    .putBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)
                    .apply()

            } catch (_: Throwable) {}
        }
    }
}
