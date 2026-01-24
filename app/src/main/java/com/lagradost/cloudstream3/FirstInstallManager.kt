package com.lagradost.cloudstream3

import android.app.Activity
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.PluginsViewModel
import kotlinx.coroutines.launch

object FirstInstallManager {

    private const val DONE = "first_install_done_v1"

    fun runIfNeeded(activity: Activity) {
        val prefs = activity.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)

        if (!prefs.getBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)) return
        if (prefs.getBoolean(DONE, false)) return

        activity.lifecycleScope.launch {
            try {
                // ⏳ pastikan repo ke-load
                RepositoryManager.loadRepositories()

                val repo = RepositoryManager.getRepositories()
                    .firstOrNull { it.name == "ExtCloud" }
                    ?: return@launch

                // 🔥 DOWNLOAD SEMUA PROVIDER
                PluginsViewModel.downloadAll(
                    activity,
                    repo.url,
                    null
                )

                prefs.edit()
                    .putBoolean(DONE, true)
                    .putBoolean(Initializer.NEED_AUTO_DOWNLOAD, false)
                    .apply()

            } catch (_: Throwable) {}
        }
    }
}
