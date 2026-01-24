package com.lagradost.cloudstream3

import android.content.Context
import com.lagradost.cloudstream3.plugins.RepositoryManager
import com.lagradost.cloudstream3.ui.settings.extensions.RepositoryData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Initializer {

    const val NEED_AUTO_DOWNLOAD = "need_auto_download_v1"
    private const val AUTO_REPO_FLAG = "auto_repo_added_v1"

    fun start(context: Context) {
        val prefs = context.getSharedPreferences("cloudstream", Context.MODE_PRIVATE)
        if (prefs.getBoolean(AUTO_REPO_FLAG, false)) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                RepositoryManager.addRepository(
                    RepositoryData(
                        name = "ExtCloud",
                        url = "https://raw.githubusercontent.com/duro92/ExtCloud/builds/plugins.json",
                        iconUrl = null
                    )
                )

                prefs.edit()
                    .putBoolean(AUTO_REPO_FLAG, true)
                    .putBoolean(NEED_AUTO_DOWNLOAD, true) // 🔥 SINYAL
                    .apply()

            } catch (_: Throwable) {}
        }
    }
}
